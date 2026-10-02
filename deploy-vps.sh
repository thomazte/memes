#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$SCRIPT_DIR"
VPS_HOST="${VPS_HOST:-hermes-agent}"
TOMCAT_WEBAPPS="/var/lib/tomcat10/webapps"
WAR_NAME="Memes-1.0-SNAPSHOT.war"
WAR_PATH="$PROJECT_DIR/target/$WAR_NAME"
REMOTE_WAR="/tmp/$WAR_NAME"
DOMAIN="${DOMAIN:-memes.137.131.137.227.sslip.io}"
APP_URL="${APP_URL:-https://${DOMAIN}/login}"

echo "=== Deploy Memes na VPS ($VPS_HOST) ==="
echo

echo "[1/5] Garantindo HTTPS..."
if ! ssh "$VPS_HOST" "sudo test -f /etc/letsencrypt/live/${DOMAIN}/fullchain.pem"; then
  ssh "$VPS_HOST" 'bash -s' < "$SCRIPT_DIR/setup-https.sh"
fi

echo "[2/5] Maven clean package..."
cd "$PROJECT_DIR"
mvn clean package

if [ ! -f "$WAR_PATH" ]; then
  echo "ERRO: WAR nao gerado em $WAR_PATH"
  exit 1
fi

echo "[3/5] Enviando WAR para a VPS..."
scp "$WAR_PATH" "$VPS_HOST:$REMOTE_WAR"

echo "[4/5] Publicando no Tomcat da VPS..."
ssh "$VPS_HOST" "sudo service tomcat10 stop && sudo rm -rf '$TOMCAT_WEBAPPS/ROOT' '$TOMCAT_WEBAPPS/ROOT.war' && sudo cp '$REMOTE_WAR' '$TOMCAT_WEBAPPS/ROOT.war' && sudo chown tomcat:tomcat '$TOMCAT_WEBAPPS/ROOT.war' && rm -f '$REMOTE_WAR' && sudo service tomcat10 start"

echo "[5/5] Aguardando aplicacao..."
if ssh "$VPS_HOST" 'bash -s' <<'EOF'
for i in $(seq 1 30); do
  if curl -fsS --max-time 5 http://127.0.0.1:8080/login >/dev/null 2>&1; then
    exit 0
  fi
  echo "Aguardando aplicacao subir... ($i/30)"
  sleep 2
done
exit 1
EOF
then
  if curl -4 -fsS --connect-timeout 3 --max-time 8 "$APP_URL" >/dev/null 2>&1; then
    echo "Pronto! Acesse: $APP_URL"
  else
    echo "Aplicacao no ar no Tomcat da VPS."
    echo "A URL publica nao respondeu: $APP_URL"
    echo "Libere TCP 443 na lista de seguranca da Oracle Cloud."
  fi
  exit 0
fi

echo "Tomcat da VPS subiu, mas o endpoint ainda nao respondeu: http://127.0.0.1:8080/login"
echo "Verifique os logs: ssh $VPS_HOST sudo journalctl -u tomcat10 -n 50"
exit 1
