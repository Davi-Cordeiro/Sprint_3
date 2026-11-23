@echo off
rem So e preciso rodar este arquivo se voce MUDAR o codigo (Java, HTML, CSS ou JS).
rem Ele compila o Java e gera o arquivo clinica.war atualizado.
rem Se o Tomcat nao estiver em C:\xampp\tomcat, rode antes: set TOMCAT=C:\caminho\do\tomcat
cd /d "%~dp0"
if "%TOMCAT%"=="" set TOMCAT=C:\xampp\tomcat

if not exist "%TOMCAT%\lib\servlet-api.jar" (
  echo ERRO: nao achei o Tomcat em %TOMCAT%
  echo Rode antes: set TOMCAT=C:\caminho\do\tomcat
  pause
  exit /b 1
)

rem 1) Compila os arquivos .java para web\WEB-INF\classes
if exist web\WEB-INF\classes rmdir /s /q web\WEB-INF\classes
mkdir web\WEB-INF\classes
dir /s /b src\*.java > arquivos-java.txt
javac -source 8 -target 8 -Xlint:-options -encoding UTF-8 -cp "%TOMCAT%\lib\servlet-api.jar" -d web\WEB-INF\classes @arquivos-java.txt
if errorlevel 1 (
  del arquivos-java.txt
  pause
  exit /b 1
)
del arquivos-java.txt

rem 2) Junta a pasta web em um arquivo so: clinica.war
if exist clinica.war del clinica.war
jar -cf clinica.war -C web .

echo Pronto! clinica.war atualizado. Copie para a pasta webapps do Tomcat.
pause
