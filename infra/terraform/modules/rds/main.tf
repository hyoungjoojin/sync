resource "aws_security_group" "rds" {
  name        = "${var.project_name}-${var.environment}-rds"
  description = "Allow PostgreSQL access from the application instance only."
  vpc_id      = var.vpc_id

  tags = {
    Name        = "${var.project_name}-${var.environment}-rds"
    Project     = var.project_name
    Environment = var.environment
  }
}

resource "aws_vpc_security_group_ingress_rule" "postgres" {
  # Not for_each/toset: the security group IDs here are apply-time outputs
  # of other modules (e.g. module.ec2.security_group_id) and unknown at plan
  # time on a first apply, which for_each can't handle. count only needs the
  # list's length, which is statically known from the caller's literal.
  count = length(var.allowed_security_group_ids)

  security_group_id            = aws_security_group.rds.id
  referenced_security_group_id = var.allowed_security_group_ids[count.index]
  from_port                    = 5432
  to_port                      = 5432
  ip_protocol                  = "tcp"
}

resource "aws_vpc_security_group_egress_rule" "all" {
  security_group_id = aws_security_group.rds.id
  ip_protocol       = "-1"
  cidr_ipv4         = "0.0.0.0/0"
}

resource "aws_db_instance" "this" {
  identifier     = "${var.project_name}-${var.environment}"
  engine         = "postgres"
  engine_version = var.engine_version

  instance_class        = var.instance_class
  allocated_storage     = var.allocated_storage_gb
  max_allocated_storage = var.max_allocated_storage_gb
  storage_type          = "gp3"
  storage_encrypted     = true

  db_name  = var.db_name
  username = var.master_username
  password = var.master_password
  port     = 5432

  db_subnet_group_name   = var.db_subnet_group_name
  vpc_security_group_ids = [aws_security_group.rds.id]
  publicly_accessible    = false
  multi_az               = var.multi_az

  backup_retention_period = var.backup_retention_days
  backup_window           = "17:00-18:00"         # 02:00-03:00 KST
  maintenance_window      = "sat:18:00-sat:19:00" # 03:00-04:00 KST Sat

  auto_minor_version_upgrade = true
  deletion_protection        = var.deletion_protection
  skip_final_snapshot        = var.skip_final_snapshot
  final_snapshot_identifier  = var.skip_final_snapshot ? null : "${var.project_name}-${var.environment}-final"

  tags = {
    Project     = var.project_name
    Environment = var.environment
  }
}
