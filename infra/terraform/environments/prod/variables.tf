variable "project_name" {
  type        = string
  default     = "sync"
  description = "Name of the project, used as a prefix for all resource names."
}

variable "environment" {
  type        = string
  default     = "prod"
  description = "Deployment environment. Controls resource naming and behaviour (e.g. secret recovery window)."
}

variable "aws_region" {
  type        = string
  default     = "ap-northeast-2"
  description = "AWS region hosting production infrastructure."
}

variable "availability_zones" {
  type        = list(string)
  default     = ["ap-northeast-2a", "ap-northeast-2c"]
  description = "Availability zones the VPC's subnets are spread across."
}

variable "root_domain" {
  type        = string
  default     = "skkil.org"
  description = "Root domain purchased for the project. Registered and hosted in Route 53."
}

variable "app_domain" {
  type        = string
  default     = "sync.skkil.org"
  description = "Subdomain the application is served from."
}

variable "instance_type" {
  type        = string
  default     = "t3.small"
  description = "EC2 instance type hosting web, server and nginx together."
}

# --- S3 ---

variable "s3_media_bucket_name" {
  type        = string
  default     = "skkil-sync-media"
  description = "Name of the S3 bucket used to store user-uploaded media. Must match the bucket name hardcoded in MediaService."
}

# --- RDS ---

variable "rds_instance_class" {
  type        = string
  default     = "db.t4g.micro"
  description = "RDS instance class."
}

variable "rds_multi_az" {
  type        = bool
  default     = false
  description = "Whether to deploy an RDS Multi-AZ standby."
}

variable "rds_skip_final_snapshot" {
  type        = bool
  default     = false
  description = "Whether to skip the final snapshot when the RDS instance is destroyed/replaced. Keep false for real prod data; only set true in terraform.tfvars temporarily while iterating on a throwaway instance."
}

variable "postgres_db_name" {
  type        = string
  default     = "sync"
  description = "Name of the PostgreSQL database."
}

variable "postgres_username" {
  type        = string
  default     = "skkil"
  description = "PostgreSQL master username."
}

variable "postgres_password" {
  type        = string
  sensitive   = true
  description = "PostgreSQL master password."
}

# --- Application secrets ---

variable "google_client_id" {
  type        = string
  sensitive   = true
  description = "Google OAuth2 client ID for social login."
}

variable "google_client_secret" {
  type        = string
  sensitive   = true
  description = "Google OAuth2 client secret for social login."
}

variable "naver_client_id" {
  type        = string
  sensitive   = true
  description = "Naver OAuth2 client ID for social login."
}

variable "naver_client_secret" {
  type        = string
  sensitive   = true
  description = "Naver OAuth2 client secret for social login."
}

variable "mail_username" {
  type        = string
  sensitive   = true
  description = "Gmail address the app sends mail from (spring.mail.username)."
}

variable "mail_password" {
  type        = string
  sensitive   = true
  description = "Gmail app password for mail_username (Google Account > Security > App passwords — requires 2-Step Verification enabled). Not the account's login password."
}

variable "slack_webhook_url" {
  type        = string
  sensitive   = true
  default     = ""
  description = "Slack incoming webhook URL for notifications. Leave empty to disable."
}

variable "channel_talk_secret_key" {
  type        = string
  sensitive   = true
  default     = ""
  description = "Channel Talk user data encryption key (채널 설정 > 보안 및 개발), used to sign the SDK member hash. Leave empty to boot the messenger anonymously."
}

variable "channel_talk_plugin_key" {
  type        = string
  sensitive   = true
  default     = ""
  description = "Channel Talk plugin key (채널 설정 > 설치 관리), baked into the web image as NEXT_PUBLIC_CHANNEL_TALK_PLUGIN_KEY at Docker build time. Leave empty to disable the messenger widget entirely."
}

variable "captcha_secret_key" {
  type        = string
  sensitive   = true
  default     = ""
  description = "Captcha secret key (currently Google reCAPTCHA v3), used server-side to verify registration tokens. Leave empty to skip captcha verification."
}

variable "captcha_site_key" {
  type        = string
  sensitive   = true
  default     = ""
  description = "Captcha site key (currently Google reCAPTCHA v3), baked into the web image as NEXT_PUBLIC_CAPTCHA_SITE_KEY at Docker build time. Must be set whenever captcha_secret_key is set, or every registration request fails server-side verification (the server requires a token once its secret key is non-blank, but the client only executes captcha when this site key is present)."
}

variable "admin_email" {
  type        = string
  sensitive   = true
  default     = ""
  description = "Email of the platform ADMIN account. AdminSeeder creates it on boot if absent, or promotes it if that email already registered normally. Empty means no admin is created."
}

variable "admin_password" {
  type        = string
  sensitive   = true
  default     = ""
  description = "Password for admin_email, used only when the account does not exist yet. Leave empty to register through the normal signup flow and let the next boot promote that account."
}
