#!/usr/bin/env bash
set -euo pipefail

# =============================
# Configuracoes do projeto
# =============================
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$SCRIPT_DIR"
PROJECT_NAME="Memes"
TOMCAT_WEBAPPS="/var/lib/tomcat10/webapps"
WAR_NAME="Memes-1.0-SNAPSHOT.war"
WAR_PATH="$PROJECT_DIR/target/$WAR_NAME"
APP_CHECK_URL="http://localhost:8080/login"

# Modo padrao: rapido (sem clean)
USE_CLEAN=false
STATUS_ONLY=false
BACKUP_DIR=""
PREVIOUS_ROOT_WAR_BACKUP=""
PREVIOUS_ROOT_DIR_BACKUP=""

show_help() {
  echo "Uso: ./deploy.sh [--clean] [--status] [--help]"
  echo
  echo "  --clean   Executa 'mvn clean package' (mais lento, mais completo)"
  echo "  --status  Apenas verifica status do Tomcat e endpoint"
  echo "  --help    Mostra esta ajuda"
}

check_tomcat_status() {
  if sudo service tomcat10 status --no-pager >/dev/null 2>&1; then
    echo "Tomcat: ativo"
  else
    echo "Tomcat: inativo ou com erro"
    return 1
  fi
}

check_endpoint() {
  if command -v curl >/dev/null 2>&1; then
    local attempt=1
    local max_attempts=30

    while [ "$attempt" -le "$max_attempts" ]; do
      if curl -fsS "$APP_CHECK_URL" >/dev/null; then
        echo "Endpoint: OK ($APP_CHECK_URL)"
        return 0
      fi

      echo "Aguardando aplicacao subir... ($attempt/$max_attempts)"
      sleep 2
      attempt=$((attempt + 1))
    done

    echo "Endpoint: FALHOU ($APP_CHECK_URL)"
    return 1
  else
    echo "Aviso: curl nao encontrado; validacao de endpoint ignorada."
  fi
}

rollback() {
  echo "[ROLLBACK] Falha detectada. Restaurando versao anterior..."
  sudo service tomcat10 stop || true
  sudo rm -rf "$TOMCAT_WEBAPPS/ROOT" "$TOMCAT_WEBAPPS/ROOT.war"

  if [ -n "$PREVIOUS_ROOT_WAR_BACKUP" ] && [ -f "$PREVIOUS_ROOT_WAR_BACKUP" ]; then
    sudo cp "$PREVIOUS_ROOT_WAR_BACKUP" "$TOMCAT_WEBAPPS/ROOT.war"
    sudo chown tomcat:tomcat "$TOMCAT_WEBAPPS/ROOT.war"
  fi

  if [ -n "$PREVIOUS_ROOT_DIR_BACKUP" ] && [ -d "$PREVIOUS_ROOT_DIR_BACKUP" ]; then
    sudo cp -a "$PREVIOUS_ROOT_DIR_BACKUP" "$TOMCAT_WEBAPPS/ROOT"
    sudo chown -R tomcat:tomcat "$TOMCAT_WEBAPPS/ROOT"
  fi

  sudo service tomcat10 start || true
  echo "[ROLLBACK] Concluido."
}

for arg in "$@"; do
  case "$arg" in
    --clean)
      USE_CLEAN=true
      ;;
    --status)
      STATUS_ONLY=true
      ;;
    --help|-h)
      show_help
      exit 0
      ;;
    *)
      echo "Opcao invalida: $arg"
      show_help
      exit 1
      ;;
  esac
done

echo "=== Deploy: $PROJECT_NAME ==="
echo "Script: $SCRIPT_DIR/deploy.sh"
echo "Projeto: $PROJECT_DIR"
echo

if [ "$STATUS_ONLY" = true ]; then
  echo "[STATUS] Verificando Tomcat e endpoint..."
  check_tomcat_status
  check_endpoint
  exit 0
fi

BACKUP_DIR="/tmp/memes-deploy-backup-$(date +%Y%m%d-%H%M%S)"
PREVIOUS_ROOT_WAR_BACKUP="$BACKUP_DIR/ROOT.war"
PREVIOUS_ROOT_DIR_BACKUP="$BACKUP_DIR/ROOT"

echo "[0/8] Gerando backup do deploy atual..."
mkdir -p "$BACKUP_DIR"
if [ -f "$TOMCAT_WEBAPPS/ROOT.war" ]; then
  sudo cp "$TOMCAT_WEBAPPS/ROOT.war" "$PREVIOUS_ROOT_WAR_BACKUP"
fi
if [ -d "$TOMCAT_WEBAPPS/ROOT" ]; then
  sudo cp -a "$TOMCAT_WEBAPPS/ROOT" "$PREVIOUS_ROOT_DIR_BACKUP"
fi

echo "[1/8] Parando o Tomcat 10..."
sudo service tomcat10 stop

echo "[2/8] Build Maven..."
cd "$PROJECT_DIR"
if [ "$USE_CLEAN" = true ]; then
  echo "Modo build: clean package"
  mvn clean package
else
  echo "Modo build: package (sem clean)"
  mvn package
fi

echo "[3/8] Validando artefato..."
if [ ! -f "$WAR_PATH" ]; then
  echo "WAR nao encontrado: $WAR_PATH"
  rollback
  exit 1
fi

echo "[4/8] Limpando deploy anterior..."
sudo rm -rf "$TOMCAT_WEBAPPS/ROOT" "$TOMCAT_WEBAPPS/ROOT.war"

echo "[5/8] Copiando novo ROOT.war..."
sudo cp "$WAR_PATH" "$TOMCAT_WEBAPPS/ROOT.war"
sudo chown tomcat:tomcat "$TOMCAT_WEBAPPS/ROOT.war"

echo "[6/8] Iniciando Tomcat..."
if ! sudo service tomcat10 start; then
  rollback
  exit 1
fi

echo "[7/8] Verificando status do Tomcat..."
if ! check_tomcat_status; then
  rollback
  exit 1
fi

echo "[8/8] Verificando endpoint da aplicacao..."
if ! check_endpoint; then
  rollback
  exit 1
fi

echo "Deploy concluido com sucesso!"
echo "Acesse: $APP_CHECK_URL"
