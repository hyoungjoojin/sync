#!/usr/bin/env bash

# Required environment variables:
#   LETSENCRYPT_EMAIL  Contact address for Let's Encrypt expiry notices.
#
# Optional:
#   APP_DOMAIN  Defaults to sync.skkil.org.

set -euo pipefail

: "${LETSENCRYPT_EMAIL:?LETSENCRYPT_EMAIL is required}"

APP_DOMAIN="${APP_DOMAIN:-sync.skkil.org}"
APP_DIR="/opt/sync"

cd "$APP_DIR"

echo "[INFO] Starting web/server/nginx so nginx can serve the ACME challenge..."
docker compose up -d web server nginx

# The dummy self-signed cert was written to this path by infra/ops/deploy.sh
# (via openssl, not certbot), so there's no matching certbot renewal-lineage
# record for it. certbot refuses to touch a live/<domain> directory it
# doesn't recognize as its own — clear it so certonly can create a real one.
echo "[INFO] Removing dummy self-signed cert..."
rm -rf \
    "certbot/conf/live/${APP_DOMAIN}" \
    "certbot/conf/archive/${APP_DOMAIN}" \
    "certbot/conf/renewal/${APP_DOMAIN}.conf"

echo "[INFO] Requesting certificate for ${APP_DOMAIN}..."
docker compose --profile certbot run --rm certbot certonly \
    --webroot -w /var/www/certbot \
    -d "$APP_DOMAIN" \
    --email "$LETSENCRYPT_EMAIL" \
    --agree-tos \
    --no-eff-email \
    --non-interactive

echo "[INFO] Reloading nginx with the new certificate..."
docker compose exec nginx nginx -s reload

# Install the renewal timer now that a real cert exists. Lives here (not in the
# instance's boot provisioning) because renewal is a certbot concern and only
# becomes meaningful once certonly above has issued a lineage to renew.
echo "[INFO] Installing certbot renewal timer..."
cat > /etc/systemd/system/certbot-renew.service <<SYSTEMD_SERVICE_EOF
[Unit]
Description=Renew Let's Encrypt certificate for ${APP_DOMAIN}

[Service]
Type=oneshot
WorkingDirectory=${APP_DIR}
ExecStart=/usr/bin/docker compose --profile certbot run --rm certbot renew --quiet
ExecStartPost=/usr/bin/docker compose exec nginx nginx -s reload
SYSTEMD_SERVICE_EOF

cat > /etc/systemd/system/certbot-renew.timer <<'SYSTEMD_TIMER_EOF'
[Unit]
Description=Daily Let's Encrypt renewal check

[Timer]
OnCalendar=daily
RandomizedDelaySec=1h
Persistent=true

[Install]
WantedBy=timers.target
SYSTEMD_TIMER_EOF

systemctl daemon-reload
systemctl enable --now certbot-renew.timer

echo "[INFO] Done. certbot-renew.timer handles renewal going forward."
