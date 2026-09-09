@echo off
setlocal EnableExtensions

REM Initialize Playwright E2E dependencies for the Drill4J ecommerce demo.
REM Usage:
REM   init.cmd
REM   init.cmd --insecure
REM   init.cmd --with-deps

cd /d "%~dp0"

set "INSECURE=0"
set "WITH_DEPS=0"

:parse
if "%~1"=="" goto parsed
if /I "%~1"=="--insecure" set "INSECURE=1" & shift & goto parse
if /I "%~1"=="--with-deps" set "WITH_DEPS=1" & shift & goto parse
if /I "%~1"=="-h" goto help
if /I "%~1"=="--help" goto help
echo Unknown option: %~1
exit /b 1

:help
echo Usage: init.cmd [--insecure] [--with-deps]
exit /b 0

:parsed
if "%INSECURE%"=="1" (
  set "NODE_TLS_REJECT_UNAUTHORIZED=0"
  set "npm_config_strict_ssl=false"
  echo Warning: TLS certificate verification is disabled for this run.
)

where node >nul 2>&1 || (
  echo Missing required command: node
  exit /b 1
)
where npm >nul 2>&1 || (
  echo Missing required command: npm
  exit /b 1
)

for /f "tokens=1 delims=." %%A in ('node -p "process.versions.node"') do set "NODE_MAJOR=%%A"
if %NODE_MAJOR% LSS 18 (
  echo Node.js 18+ is required.
  node -v
  exit /b 1
)

echo ==^> Node & node -v
echo ==^> npm & npm -v
echo ==^> Installing npm packages
call npm install
if errorlevel 1 exit /b %ERRORLEVEL%

if not exist ".env" if exist ".env.example" (
  copy /Y ".env.example" ".env" >nul
  echo ==^> Created .env from .env.example
)

echo ==^> Installing Playwright Chromium
if "%WITH_DEPS%"=="1" (
  call npx playwright install --with-deps chromium
) else (
  call npx playwright install chromium
)
if errorlevel 1 exit /b %ERRORLEVEL%

echo.
echo Done. With docker/ecommerce running, execute:
echo   npm test
exit /b 0
