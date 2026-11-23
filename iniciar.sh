#!/usr/bin/env bash
# Inicia o site da clínica (Linux/Mac). Para parar: Ctrl+C
cd "$(dirname "$0")" || exit 1
PORTA="${PORTA:-8080}"
export PORTA

# 1) Java (JDK 17 ou mais novo)
if ! command -v java >/dev/null 2>&1 || ! command -v javac >/dev/null 2>&1; then
  echo "ERRO: Java (JDK) não encontrado."
  echo "  Ubuntu/Debian: sudo apt install openjdk-21-jdk"
  echo "  Mac:           brew install openjdk@21"
  echo "  Outros:        https://adoptium.net"
  exit 1
fi
VERSAO_JAVA=$(java -version 2>&1 | awk -F'"' '/version/ {print $2}' | cut -d. -f1)
if [ "${VERSAO_JAVA:-0}" -lt 17 ] 2>/dev/null; then
  echo "ERRO: o Java instalado é a versão $VERSAO_JAVA. Precisa ser 17 ou mais novo."
  exit 1
fi

# 2) MySQL rodando
MYSQL_HOST=$(grep -E '^mysql.host=' mysql.properties 2>/dev/null | cut -d= -f2 | tr -d '[:space:]')
MYSQL_PORTA=$(grep -E '^mysql.porta=' mysql.properties 2>/dev/null | cut -d= -f2 | tr -d '[:space:]')
MYSQL_HOST=${MYSQL_HOST:-localhost}
MYSQL_PORTA=${MYSQL_PORTA:-3306}
if ! (exec 3<>"/dev/tcp/$MYSQL_HOST/$MYSQL_PORTA") 2>/dev/null; then
  echo "ERRO: o MySQL não está rodando em $MYSQL_HOST:$MYSQL_PORTA."
  echo "  Ubuntu/Debian: sudo apt install mysql-server   (ou: sudo systemctl start mysql)"
  echo "  Mac:           brew install mysql && brew services start mysql"
  exit 1
fi

# 3) Abre o navegador quando o site estiver no ar
(
  for _ in $(seq 1 300); do
    if (exec 3<>"/dev/tcp/localhost/$PORTA") 2>/dev/null; then
      sleep 1
      echo
      echo "==> Site no ar: http://localhost:$PORTA   (Ctrl+C para parar)"
      if [ -z "$SEM_NAVEGADOR" ]; then
        if command -v xdg-open >/dev/null 2>&1; then xdg-open "http://localhost:$PORTA" >/dev/null 2>&1
        elif command -v open >/dev/null 2>&1; then open "http://localhost:$PORTA"; fi
      fi
      exit 0
    fi
    sleep 2
  done
) &
ABRIR_NAVEGADOR=$!
trap 'kill $ABRIR_NAVEGADOR 2>/dev/null' EXIT

echo "==> Iniciando o site (a primeira vez baixa as dependências e demora alguns minutos)..."
chmod +x mvnw 2>/dev/null
./mvnw -q spring-boot:run
