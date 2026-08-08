terraform {
  required_version = ">= 1.6"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # State is real infrastructure here (RDS, EC2, DNS) — unlike the local
  # environment's `backend "local"`, losing this state is expensive to
  # recover from. Create the bucket once by hand (or via a one-off bootstrap
  # apply with `backend "local"`) before switching this block on:
  #
  #   aws s3api create-bucket --bucket skkil-sync-terraform-state \
  #     --region ap-northeast-2 \
  #     --create-bucket-configuration LocationConstraint=ap-northeast-2
  #   aws s3api put-bucket-versioning --bucket skkil-sync-terraform-state \
  #     --versioning-configuration Status=Enabled
  #
  backend "s3" {
    bucket       = "skkil-sync-terraform-state"
    key          = "prod/terraform.tfstate"
    region       = "ap-northeast-2"
    encrypt      = true
    use_lockfile = true
  }
}

provider "aws" {
  region = var.aws_region
}

data "aws_caller_identity" "current" {}

locals {
  # Referencing module.secrets' resource ARNs directly here would create a
  # cycle: ec2 -> secrets -> rds (postgres_host) -> ec2 (security group).
  # Secrets Manager ARNs are deterministic apart from a random suffix AWS
  # appends, so a wildcarded ARN lets the ec2 module's IAM policy be built
  # without depending on the secrets module at all.
  secret_arn_prefix = "arn:aws:secretsmanager:${var.aws_region}:${data.aws_caller_identity.current.account_id}:secret:${var.project_name}/${var.environment}"
  secret_arns = [
    "${local.secret_arn_prefix}/postgres/password-*",
    "${local.secret_arn_prefix}/server/app-*",
    "${local.secret_arn_prefix}/web/app-*",
  ]
}

module "kms" {
  source       = "../../modules/kms"
  project_name = var.project_name
  environment  = var.environment
}

module "s3" {
  source               = "../../modules/s3"
  project_name         = var.project_name
  environment          = var.environment
  bucket_name          = var.s3_media_bucket_name
  cors_allowed_origins = ["https://${var.app_domain}"]
}

module "vpc" {
  source             = "../../modules/vpc"
  project_name       = var.project_name
  environment        = var.environment
  availability_zones = var.availability_zones
}

module "ecr" {
  source       = "../../modules/ecr"
  project_name = var.project_name
  environment  = var.environment
}

module "ec2" {
  source        = "../../modules/ec2"
  project_name  = var.project_name
  environment   = var.environment
  vpc_id        = module.vpc.vpc_id
  subnet_id     = module.vpc.public_subnet_id
  instance_type = var.instance_type

  media_bucket_arn    = module.s3.bucket_arn
  secret_arns         = local.secret_arns
  ecr_repository_arns = values(module.ecr.repository_arns)
  kms_key_arn         = module.kms.key_arn
}

module "rds" {
  source                     = "../../modules/rds"
  project_name               = var.project_name
  environment                = var.environment
  vpc_id                     = module.vpc.vpc_id
  db_subnet_group_name       = module.vpc.db_subnet_group_name
  allowed_security_group_ids = [module.ec2.security_group_id]
  instance_class             = var.rds_instance_class
  multi_az                   = var.rds_multi_az
  skip_final_snapshot        = var.rds_skip_final_snapshot
  db_name                    = var.postgres_db_name
  master_username            = var.postgres_username
  master_password            = var.postgres_password
}

module "secrets" {
  source                  = "../../modules/secrets"
  project_name            = var.project_name
  environment             = var.environment
  kms_key_arn             = module.kms.key_arn
  recovery_window_in_days = 30

  postgres_host     = module.rds.endpoint
  postgres_port     = module.rds.port
  postgres_db_name  = var.postgres_db_name
  postgres_username = var.postgres_username
  postgres_password = var.postgres_password

  google_client_id     = var.google_client_id
  google_client_secret = var.google_client_secret
  naver_client_id      = var.naver_client_id
  naver_client_secret  = var.naver_client_secret
  github_client_id     = var.github_client_id
  github_client_secret = var.github_client_secret
  mail_username        = var.mail_username
  mail_password        = var.mail_password
  slack_webhook_url    = var.slack_webhook_url

  channel_talk_secret_key = var.channel_talk_secret_key
  channel_talk_plugin_key = var.channel_talk_plugin_key
  captcha_secret_key      = var.captcha_secret_key
  captcha_site_key        = var.captcha_site_key
  admin_email             = var.admin_email
  admin_password          = var.admin_password
  openai_api_key          = var.openai_api_key

  agent_rsa_private_key = var.agent_rsa_private_key
  agent_rsa_public_key  = var.agent_rsa_public_key
}
