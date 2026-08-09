# `most_recent` re-resolves on every plan, and `ami` forces replacement on
# aws_instance — so this data source will silently propose destroying the box
# whenever Amazon publishes a new AL2023 image, on an apply that changed nothing.
# The `ignore_changes = [ami]` on aws_instance below is what makes that safe;
# do not remove one without the other.
#
# The old pattern here was `al2023-ami-*-x86_64`, which matches every AL2023
# flavour Amazon ships (minimal, ecs, ecs-gpu, ecs-neuron, ...) and lets
# `most_recent` pick whichever variant was published last. It had already landed
# on `al2023-ami-ecs-neuron-*`, the ECS build for Inferentia/Trainium
# accelerators. This pattern pins the flavour to the plain base image; only the
# date still floats.
data "aws_ami" "al2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-2023.*-kernel-6.1-x86_64"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

resource "aws_security_group" "app" {
  name        = "${var.project_name}-${var.environment}-app"
  description = "Public HTTP/HTTPS access for the nginx reverse proxy. No SSH ingress; management is via SSM Session Manager."
  vpc_id      = var.vpc_id

  tags = {
    Name        = "${var.project_name}-${var.environment}-app"
    Project     = var.project_name
    Environment = var.environment
  }
}

resource "aws_vpc_security_group_ingress_rule" "http" {
  security_group_id = aws_security_group.app.id
  cidr_ipv4         = "0.0.0.0/0"
  from_port         = 80
  to_port           = 80
  ip_protocol       = "tcp"
}

resource "aws_vpc_security_group_ingress_rule" "https" {
  security_group_id = aws_security_group.app.id
  cidr_ipv4         = "0.0.0.0/0"
  from_port         = 443
  to_port           = 443
  ip_protocol       = "tcp"
}

resource "aws_vpc_security_group_egress_rule" "all" {
  security_group_id = aws_security_group.app.id
  ip_protocol       = "-1"
  cidr_ipv4         = "0.0.0.0/0"
}

data "aws_iam_policy_document" "assume_role" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "instance" {
  name               = "${var.project_name}-${var.environment}-ec2"
  assume_role_policy = data.aws_iam_policy_document.assume_role.json

  tags = {
    Project     = var.project_name
    Environment = var.environment
  }
}

resource "aws_iam_role_policy_attachment" "ssm" {
  role       = aws_iam_role.instance.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

data "aws_iam_policy_document" "app" {
  statement {
    sid       = "MediaBucketReadWrite"
    actions   = ["s3:GetObject", "s3:PutObject", "s3:DeleteObject"]
    resources = ["${var.media_bucket_arn}/*"]
  }

  statement {
    sid       = "MediaBucketList"
    actions   = ["s3:ListBucket"]
    resources = [var.media_bucket_arn]
  }

  statement {
    sid       = "ReadSecrets"
    actions   = ["secretsmanager:GetSecretValue"]
    resources = var.secret_arns
  }

  statement {
    sid       = "DecryptSecrets"
    actions   = ["kms:Decrypt"]
    resources = [var.kms_key_arn]
  }

  statement {
    sid       = "EcrAuth"
    actions   = ["ecr:GetAuthorizationToken"]
    resources = ["*"]
  }

  statement {
    sid = "EcrPull"
    actions = [
      "ecr:BatchGetImage",
      "ecr:GetDownloadUrlForLayer",
      "ecr:BatchCheckLayerAvailability",
    ]
    resources = var.ecr_repository_arns
  }
}

resource "aws_iam_role_policy" "app" {
  name   = "${var.project_name}-${var.environment}-app"
  role   = aws_iam_role.instance.id
  policy = data.aws_iam_policy_document.app.json
}

resource "aws_iam_instance_profile" "instance" {
  name = "${var.project_name}-${var.environment}-ec2"
  role = aws_iam_role.instance.name
}

resource "aws_instance" "app" {
  ami                    = data.aws_ami.al2023.id
  instance_type          = var.instance_type
  subnet_id              = var.subnet_id
  vpc_security_group_ids = [aws_security_group.app.id]
  iam_instance_profile   = aws_iam_instance_profile.instance.name

  root_block_device {
    volume_size = var.root_volume_size_gb
    volume_type = "gp3"
    encrypted   = true
  }

  metadata_options {
    http_tokens = "required"
  }

  user_data                   = file("${path.module}/files/provision.sh")
  user_data_replace_on_change = true

  tags = {
    Name        = "${var.project_name}-${var.environment}"
    Project     = var.project_name
    Environment = var.environment
  }

  # This box is a pet, not cattle: there is no autoscaling, no load balancer, and
  # the Let's Encrypt cert lineage lives on the root volume under /opt/sync,
  # which `delete_on_termination = true` destroys with the instance. An
  # unattended replacement therefore means downtime plus a cert re-issue, against
  # a Let's Encrypt limit of 5 duplicate certs per hostname per 168 hours.
  #
  # So AMI upgrades are a deliberate, scheduled rebuild, never a side effect of
  # an unrelated apply. To actually take a new AMI: remove this line (or run
  # `terraform apply -replace=module.ec2.aws_instance.app`) at a time you have
  # chosen, and re-run infra/ops/deploy.sh + certbot.sh straight after.
  lifecycle {
    ignore_changes = [ami]
  }
}

resource "aws_eip" "app" {
  instance = aws_instance.app.id
  domain   = "vpc"

  tags = {
    Name        = "${var.project_name}-${var.environment}"
    Project     = var.project_name
    Environment = var.environment
  }
}
