#!/usr/bin/env bash
# Rode na VPS (ou deixe o deploy-vps.sh chamar este script).
# O nginx termina o TLS; o Tomcat continua em 127.0.0.1:8080.
set -euo pipefail

DOMAIN="${DOMAIN:-memes.137.131.137.227.sslip.io}"
EMAIL="${CERTBOT_EMAIL:-thmzarthur@gmail.com}"
SITE="/etc/nginx/sites-available/memes"
ENABLED="/etc/nginx/sites-enabled/memes"
SERVER_XML="/etc/tomcat10/server.xml"

echo "=== HTTPS: $DOMAIN ==="

if ! command -v certbot >/dev/null 2>&1; then
  echo "Instalando certbot..."
  sudo DEBIAN_FRONTEND=noninteractive apt-get update
  sudo DEBIAN_FRONTEND=noninteractive apt-get install -y certbot python3-certbot-nginx
fi

if [ ! -f "$SITE" ]; then
  echo "Criando site nginx..."
  sudo tee "$SITE" >/dev/null <<EOF
server {
    listen 80;
    server_name ${DOMAIN};

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}
EOF
fi

if [ ! -e "$ENABLED" ]; then
  sudo ln -s "$SITE" "$ENABLED"
fi

sudo nginx -t
sudo systemctl reload nginx

if ! sudo iptables -C INPUT -p tcp -m state --state NEW -m tcp --dport 443 -j ACCEPT >/dev/null 2>&1; then
  echo "Liberando a porta 443 no firewall..."
  sudo iptables -I INPUT -p tcp -m state --state NEW -m tcp --dport 443 -j ACCEPT
  if [ -d /etc/iptables ]; then
    sudo iptables-save | sudo tee /etc/iptables/rules.v4 >/dev/null
  fi
fi

echo "Emitindo certificado e redirecionando HTTP para HTTPS..."
sudo certbot --nginx -d "$DOMAIN" \
  --non-interactive --agree-tos --redirect \
  -m "$EMAIL"

if ! sudo grep -q 'RemoteIpValve' "$SERVER_XML"; then
  echo "Ativando RemoteIpValve no Tomcat..."
  sudo cp "$SERVER_XML" "${SERVER_XML}.bak-https"
  py="$(mktemp)"
  cat > "$py" <<'PY'
import pathlib, sys
path = pathlib.Path(sys.argv[1])
text = path.read_text()
valve = """        <Valve className="org.apache.catalina.valves.RemoteIpValve"
               remoteIpHeader="x-forwarded-for"
               protocolHeader="x-forwarded-proto"
               protocolHeaderHttpsValue="https" />
"""
needle = '        <Valve className="org.apache.catalina.valves.AccessLogValve"'
if needle not in text:
    raise SystemExit("AccessLogValve nao encontrado em server.xml")
path.write_text(text.replace(needle, valve + needle, 1))
PY
  sudo python3 "$py" "$SERVER_XML"
  rm -f "$py"
  sudo systemctl restart tomcat10
fi

echo "HTTPS pronto: https://${DOMAIN}/login"
echo "Na Oracle Cloud, a lista de seguranca da VCN precisa liberar TCP 443 (origem 0.0.0.0/0)."
