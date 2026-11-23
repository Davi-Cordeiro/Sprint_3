@echo off
chcp 65001 >nul
REM Inicia o site da clinica (Windows). Para parar: Ctrl+C ou feche esta janela.
cd /d "%~dp0"
if "%PORTA%"=="" set PORTA=8080

REM 1) Java (JDK 17 ou mais novo)
where javac >nul 2>&1
if errorlevel 1 (
  echo ERRO: Java ^(JDK^) nao encontrado.
  echo Instale o JDK 21 em https://adoptium.net ^(marque a opcao "Set JAVA_HOME"^) e abra este arquivo de novo.
  pause
  exit /b 1
)

REM 2) MySQL rodando
set MYSQL_HOST=localhost
set MYSQL_PORTA=3306
for /f "tokens=2 delims==" %%a in ('findstr /b "mysql.host=" mysql.properties 2^>nul') do set MYSQL_HOST=%%a
for /f "tokens=2 delims==" %%a in ('findstr /b "mysql.porta=" mysql.properties 2^>nul') do set MYSQL_PORTA=%%a
powershell -NoProfile -Command "try { (New-Object Net.Sockets.TcpClient('%MYSQL_HOST%', %MYSQL_PORTA%)).Close(); exit 0 } catch { exit 1 }"
if errorlevel 1 (
  echo ERRO: o MySQL nao esta rodando em %MYSQL_HOST%:%MYSQL_PORTA%.
  echo Instale o MySQL Server em https://dev.mysql.com/downloads/installer/ ou inicie o servico "MySQL80".
  pause
  exit /b 1
)

REM 3) Abre o navegador quando o site estiver no ar
start "" /b powershell -NoProfile -WindowStyle Hidden -Command "for ($i = 0; $i -lt 300; $i++) { try { (New-Object Net.Sockets.TcpClient('localhost', %PORTA%)).Close(); Start-Sleep 1; Start-Process 'http://localhost:%PORTA%'; break } catch { Start-Sleep 2 } }"

echo ==^> Iniciando o site (a primeira vez baixa as dependencias e demora alguns minutos)...
echo ==^> Quando terminar, acesse http://localhost:%PORTA%
call mvnw.cmd -q spring-boot:run
pause
