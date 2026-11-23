#!/usr/bin/env bash
# Só é preciso rodar este arquivo se você MUDAR o código (Java, HTML, CSS ou JS).
# Ele compila o Java e gera o arquivo clinica.war atualizado.
# Uso: ./compilar.sh /caminho/do/tomcat
cd "$(dirname "$0")" || exit 1
TOMCAT="${1:-$TOMCAT}"

if [ ! -f "$TOMCAT/lib/servlet-api.jar" ]; then
  echo "ERRO: informe a pasta do Tomcat. Exemplo: ./compilar.sh /opt/apache-tomcat-9.0.98"
  exit 1
fi

# 1) Compila os arquivos .java para web/WEB-INF/classes
rm -rf web/WEB-INF/classes
mkdir -p web/WEB-INF/classes
javac -source 8 -target 8 -Xlint:-options -encoding UTF-8 \
      -cp "$TOMCAT/lib/servlet-api.jar" \
      -d web/WEB-INF/classes \
      $(find src -name "*.java") || exit 1

# 2) Junta a pasta web em um arquivo só: clinica.war
rm -f clinica.war
jar -cf clinica.war -C web . || exit 1

echo "Pronto! clinica.war atualizado. Copie para a pasta webapps do Tomcat."
