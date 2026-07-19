#!/usr/bin/env bash
#
# Boot-time provisioning for the production EC2 instance (cloud-init user_data).
# Intentionally minimal: it only installs the OS-level runtime the box needs.
# Everything app/TLS-specific (env files, docker-compose.yml, dummy + real
# certs, the renewal timer) is shipped on demand by the SSM deploy scripts
# (infra/ops/deploy.sh, infra/ops/certbot.sh) so those can change
# without replacing this instance.
set -euo pipefail

APP_DIR="/opt/sync"

dnf install -y docker jq
systemctl enable --now docker
usermod -aG docker ec2-user

mkdir -p /usr/local/lib/docker/cli-plugins
curl -sSL "https://github.com/docker/compose/releases/download/v2.32.4/docker-compose-linux-x86_64" \
  -o /usr/local/lib/docker/cli-plugins/docker-compose
chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

# App working dir + the certbot webroot nginx mounts. The domain-specific cert
# lineage under certbot/conf is created by infra/ops/deploy.sh at first deploy.
mkdir -p "$APP_DIR/certbot/www"
