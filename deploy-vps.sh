#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$SCRIPT_DIR"
VPS_HOST="${VPS_HOST:-hermes-agent}"
TOMCAT_WEBAPPS="/var/lib/tomcat10/webapps"
WAR_NAME="Memes-1.0-SNAPSHOT.war"
WAR_PATH="$PROJECT_DIR/target/$WAR_NAME"
REMOTE_WAR="/tmp/$WAR_NAME"
APP_URL="${APP_URL:-http://memes.137.131.137.227.sslip.io/login}"

echo "=== Deploy Memes na VPS ($VPS_HOST) ==="
echo

echo "[1/4] Maven clean package..."
cd "$PROJECT_DIR"
mvn clean package

if [ ! -f "$WAR_PATH" ]; then
  echo "ERRO: WAR nao gerado em $WAR_PATH"
  exit 1
fi

echo "[2/4] Enviando WAR para a VPS..."
scp "$WAR_PATH" "$VPS_HOST:$REMOTE_WAR"

echo "[3/4] Publicando no Tomcat da VPS..."
ssh "$VPS_HOST" "sudo service tomcat10 stop && sudo rm -rf '$TOMCAT_WEBAPPS/ROOT' '$TOMCAT_WEBAPPS/ROOT.war' && sudo cp '$REMOTE_WAR' '$TOMCAT_WEBAPPS/ROOT.war' && sudo chown tomcat:tomcat '$TOMCAT_WEBAPPS/ROOT.war' && rm -f '$REMOTE_WAR' && sudo service tomcat10 start"

echo "[4/4] Aguardando aplicacao..."
for i in $(seq 1 30); do
  if curl -fsS "$APP_URL" >/dev/null 2>&1; then
    echo "Pronto! Acesse: $APP_URL"
    exit 0
  fi
  echo "Aguardando aplicacao subir... ($i/30)"
  sleep 2
done

echo "Tomcat da VPS subiu, mas o endpoint ainda nao respondeu: $APP_URL"
echo "Verifique os logs: ssh $VPS_HOST sudo journalctl -u tomcat10 -n 50"
exit 1
