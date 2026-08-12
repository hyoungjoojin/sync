resource "aws_secretsmanager_secret" "postgres" {
  name                    = "${var.project_name}/${var.environment}/postgres/password"
  description             = "PostgreSQL credentials."
  kms_key_id              = var.kms_key_arn
  recovery_window_in_days = var.recovery_window_in_days

  tags = {
    Project     = var.project_name
    Environment = var.environment
  }
}

resource "aws_secretsmanager_secret_version" "postgres" {
  secret_id = aws_secretsmanager_secret.postgres.id
  secret_string = jsonencode({
    username = var.postgres_username
    password = var.postgres_password
    engine   = "postgres"
    host     = var.postgres_host
    port     = var.postgres_port
    dbname   = var.postgres_db_name
    jdbc_url = "jdbc:postgresql://${var.postgres_host}:${var.postgres_port}/${var.postgres_db_name}"
  })
}

resource "aws_secretsmanager_secret" "server_app" {
  name                    = "${var.project_name}/${var.environment}/server/app"
  description             = "Server application secrets (OAuth2, email, Slack, platform admin)."
  kms_key_id              = var.kms_key_arn
  recovery_window_in_days = var.recovery_window_in_days

  tags = {
    Project     = var.project_name
    Environment = var.environment
  }
}

resource "aws_secretsmanager_secret_version" "server_app" {
  secret_id = aws_secretsmanager_secret.server_app.id
  secret_string = jsonencode({
    OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID     = var.google_client_id
    OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_SECRET = var.google_client_secret
    OAUTH2_CLIENT_REGISTRATION_GITHUB_CLIENT_ID     = var.github_client_id
    OAUTH2_CLIENT_REGISTRATION_GITHUB_CLIENT_SECRET = var.github_client_secret
    MAIL_USERNAME                                   = var.mail_username
    MAIL_PASSWORD                                   = var.mail_password
    SLACK_WEBHOOK_URL                               = var.slack_webhook_url
    CHANNEL_TALK_SECRET_KEY                         = var.channel_talk_secret_key
    CAPTCHA_SECRET_KEY                              = var.captcha_secret_key
    OPENAI_API_KEY                                  = var.openai_api_key
    ADMIN_EMAIL                                     = var.admin_email
    ADMIN_PASSWORD                                  = var.admin_password
    APP_AGENT_RSA_PRIVATE_KEY                       = var.agent_rsa_private_key
    APP_AGENT_RSA_PUBLIC_KEY                        = var.agent_rsa_public_key
    APP_AGENT_CHATGPT_REDIRECT_URI                  = var.agent_chatgpt_redirect_uri
  })
}

resource "aws_secretsmanager_secret" "web_app" {
  name                    = "${var.project_name}/${var.environment}/web/app"
  description             = "Web application build-time secrets, baked into the Next.js image as NEXT_PUBLIC_* env vars by scripts/cd/prod-deploy.sh."
  kms_key_id              = var.kms_key_arn
  recovery_window_in_days = var.recovery_window_in_days

  tags = {
    Project     = var.project_name
    Environment = var.environment
  }
}

resource "aws_secretsmanager_secret_version" "web_app" {
  secret_id = aws_secretsmanager_secret.web_app.id
  secret_string = jsonencode({
    CHANNEL_TALK_PLUGIN_KEY = var.channel_talk_plugin_key
    CAPTCHA_SITE_KEY        = var.captcha_site_key
  })
}
