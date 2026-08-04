variable "project_name" {
  type        = string
  description = "Name of the project, used as a prefix for resource names."
}

variable "environment" {
  type        = string
  description = "Deployment environment (e.g. local, staging, production)."
}

variable "kms_key_arn" {
  type        = string
  description = "ARN of the KMS key used to encrypt secrets."
}

variable "recovery_window_in_days" {
  type        = number
  default     = 7
  description = "Days before a deleted secret is permanently destroyed. Set to 0 in local/dev to allow immediate re-creation."
}

variable "postgres_host" {
  type        = string
  description = "Hostname of the PostgreSQL server."
}

variable "postgres_port" {
  type        = number
  default     = 5432
  description = "Port the PostgreSQL server listens on."
}

variable "postgres_db_name" {
  type        = string
  description = "Name of the PostgreSQL database."
}

variable "postgres_username" {
  type        = string
  description = "PostgreSQL login username."
}

variable "postgres_password" {
  type        = string
  sensitive   = true
  description = "PostgreSQL login password."
}

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
  description = "SMTP username for sending application emails."
}

variable "mail_password" {
  type        = string
  sensitive   = true
  description = "SMTP password (or app password) for sending application emails."
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
  description = "Email of the platform ADMIN account seeded by AdminSeeder at boot. Leave empty to create no admin at all."
}

variable "admin_password" {
  type        = string
  sensitive   = true
  default     = ""
  description = "Password for admin_email. If the account already exists it is only promoted and this is ignored, so it may be left empty for the promote-an-existing-signup flow."
}
