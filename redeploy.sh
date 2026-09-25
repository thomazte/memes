#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$SCRIPT_DIR"
TOMCAT_WEBAPPS="/var/lib/tomcat10/webapps"
WAR_NAME="Memes-1.0-SNAPSHOT.war"
WAR_PATH="$PROJECT_DIR/target/$WAR_NAME"
APP_URL="http://localhost:8080/login"

echo "=== Redeploy Memes ==="
echo

echo "[1/5] Parando Tomcat..."
sudo service tomcat10 stop

echo "[2/5] Maven clean package..."
cd "$PROJECT_DIR"
mvn clean package

if [ ! -f "$WAR_PATH" ]; then
  echo "ERRO: WAR nao gerado em $WAR_PATH"
  exit 1
fi

echo "[3/5] Removendo deploy antigo (ROOT e ROOT.war)..."
sudo rm -rf "$TOMCAT_WEBAPPS/ROOT" "$TOMCAT_WEBAPPS/ROOT.war"

echo "[4/5] Copiando WAR atualizado..."
sudo cp "$WAR_PATH" "$TOMCAT_WEBAPPS/ROOT.war"
sudo chown tomcat:tomcat "$TOMCAT_WEBAPPS/ROOT.war"

echo "[5/5] Iniciando Tomcat..."
sudo service tomcat10 start

echo
echo "Aguardando aplicacao..."
for i in $(seq 1 30); do
  if curl -fsS "$APP_URL" >/dev/null 2>&1; then
    echo "Pronto! Acesse: $APP_URL"
    exit 0
  fi
  sleep 2
done

echo "Tomcat subiu, mas o endpoint ainda nao respondeu: $APP_URL"
echo "Verifique os logs: sudo journalctl -u tomcat10 -n 50"
exit 1
