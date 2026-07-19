#!/usr/bin/env bash
#
# Triggers infra/ops/certbot.sh on the production EC2 instance via SSM
# (no SSH). Run this once, after prod-deploy.sh has deployed at least once
# and the app_domain DNS record has propagated to the instance's Elastic IP.
#
# Required environment variables:
#   LETSENCRYPT_EMAIL  Contact address for Let's Encrypt expiry notices.
#
# Optional:
#   AWS_REGION    Defaults to ap-northeast-2.
#   PROJECT_NAME  Defaults to sync.
#   ENVIRONMENT   Defaults to prod.
#   APP_DOMAIN    Defaults to sync.skkil.org.

export STAGE=prod
source .envrc

set -euo pipefail

export AWS_PAGER=""

: "${LETSENCRYPT_EMAIL:?LETSENCRYPT_EMAIL is required}"

AWS_REGION="${AWS_REGION:-ap-northeast-2}"
PROJECT_NAME="${PROJECT_NAME:-sync}"
ENVIRONMENT="${ENVIRONMENT:-prod}"
APP_DOMAIN="${APP_DOMAIN:-sync.skkil.org}"

info() { echo "[INFO] $1"; }
error() { echo "[ERROR] $1" >&2; exit 1; }

info "Resolving production instance..."
INSTANCE_ID=$(aws ec2 describe-instances \
    --filters "Name=tag:Project,Values=${PROJECT_NAME}" "Name=tag:Environment,Values=${ENVIRONMENT}" "Name=instance-state-name,Values=running" \
    --query "Reservations[0].Instances[0].InstanceId" --output text)
[[ -n "$INSTANCE_ID" && "$INSTANCE_ID" != "None" ]] || error "Could not find a running ${ENVIRONMENT} instance"
info "Target instance: ${INSTANCE_ID}"

SCRIPT_B64=$(base64 -w0 "$PROJECT_ROOT_DIR/infra/ops/certbot.sh")

info "Sending certbot bootstrap command via SSM..."
COMMAND_ID=$(aws ssm send-command \
    --instance-ids "$INSTANCE_ID" \
    --document-name "AWS-RunShellScript" \
    --comment "sync ${ENVIRONMENT} certbot bootstrap" \
    --parameters "{\"commands\":[\"echo ${SCRIPT_B64} | base64 -d > /tmp/certbot-bootstrap.sh && chmod +x /tmp/certbot-bootstrap.sh\",\"LETSENCRYPT_EMAIL=${LETSENCRYPT_EMAIL} APP_DOMAIN=${APP_DOMAIN} /tmp/certbot-bootstrap.sh\"]}" \
    --query "Command.CommandId" --output text)

info "Waiting for command ${COMMAND_ID} to finish..."
aws ssm wait command-executed --command-id "$COMMAND_ID" --instance-id "$INSTANCE_ID" || true

STATUS=$(aws ssm get-command-invocation --command-id "$COMMAND_ID" --instance-id "$INSTANCE_ID" --query "Status" --output text)
aws ssm get-command-invocation --command-id "$COMMAND_ID" --instance-id "$INSTANCE_ID" --query "StandardOutputContent" --output text
aws ssm get-command-invocation --command-id "$COMMAND_ID" --instance-id "$INSTANCE_ID" --query "StandardErrorContent" --output text >&2

[[ "$STATUS" == "Success" ]] || error "certbot bootstrap finished with status: $STATUS"
info "Real TLS certificate issued for ${APP_DOMAIN}."
