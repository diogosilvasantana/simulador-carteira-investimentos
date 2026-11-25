<#
.SYNOPSIS
  Sobe backend, frontend e ponte do MT5 de uma vez, cada um na sua janela.

.DESCRIPTION
  Confere Java 25 e Node, escolhe uma porta livre para o backend (a 8080 costuma estar com o
  Apache do XAMPP), espera o backend ficar de pé, sobe o frontend apontando para essa porta,
  prepara o ambiente Python da ponte e abre o navegador. Use -Stop para encerrar tudo.

.EXAMPLE
  .\dev.cmd                         # MT5 aberto e logado, todos os ativos
.EXAMPLE
  .\dev.cmd -Source synthetic       # mercado fechado: preços sintéticos com semente fixa
.EXAMPLE
  .\dev.cmd -Symbols PETR4,VALE3    # só alguns ativos
.EXAMPLE
  .\dev.cmd -Stop                   # encerra as três janelas
#>
param(
  [ValidateSet('mt5', 'synthetic')] [string] $Source = 'mt5',
  [string[]] $Symbols = @('all'),
  [int] $Port = 0,
  [switch] $NoBridge,
  [switch] $Stop
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$stateDir = Join-Path $root '.dev'
$pidFile = Join-Path $stateDir 'pids.txt'
$ingestToken = 'local-dev-token'
$symbolList = $Symbols -join ','

function Write-Step([string] $text) { Write-Host "`n> $text" -ForegroundColor Cyan }
function Write-Ok([string] $text) { Write-Host "  $text" -ForegroundColor Green }
function Fail([string] $text) { Write-Host "`n$text" -ForegroundColor Red; exit 1 }

function Stop-Dev {
  if (Test-Path $pidFile) {
    foreach ($id in Get-Content $pidFile) {
      # /T derruba a árvore inteira: a janela, o Maven, a JVM, o node...
      & taskkill.exe /PID $id /T /F 2>$null | Out-Null
    }
    Remove-Item $pidFile
  }
  Write-Host 'Backend, frontend e ponte encerrados.' -ForegroundColor Green
}

function Test-PortFree([int] $p) {
  return -not (Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction SilentlyContinue)
}

function Start-DevWindow([string] $title, [string] $dir, [string] $script) {
  $safeDir = $dir.Replace("'", "''")
  $full = "`$Host.UI.RawUI.WindowTitle = '$title'; Set-Location -LiteralPath '$safeDir'; $script"
  # Comando codificado: evita os problemas de aspas ao passar argumentos para outra janela.
  $encoded = [Convert]::ToBase64String([Text.Encoding]::Unicode.GetBytes($full))
  $process = Start-Process powershell -PassThru -ArgumentList @(
    '-NoExit', '-NoProfile', '-ExecutionPolicy', 'Bypass', '-EncodedCommand', $encoded)
  Add-Content -Path $pidFile -Value $process.Id
}

function Wait-Url([string] $url, [int] $seconds, [scriptblock] $isReady) {
  $deadline = (Get-Date).AddSeconds($seconds)
  while ((Get-Date) -lt $deadline) {
    try {
      $response = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 3
      if (& $isReady $response) { return $true }
    } catch { }
    Start-Sleep -Seconds 3
  }
  return $false
}

if ($Stop) { Stop-Dev; exit 0 }

if (Test-Path $pidFile) {
  Fail 'Já existe uma execução aberta. Rode .\dev.cmd -Stop antes de subir de novo.'
}

Write-Step 'Conferindo pré-requisitos'
if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
  Fail 'Java não encontrado. Instale o JDK 25: winget install EclipseAdoptium.Temurin.25.JDK'
}
$javaVersion = cmd /c 'java -version 2>&1' | Select-Object -First 1
if ($javaVersion -notmatch 'version "(\d+)' -or [int]$Matches[1] -lt 25) {
  Fail "É preciso Java 25 ou mais novo; encontrado: $javaVersion"
}
Write-Ok $javaVersion
if (-not (Get-Command npm -ErrorAction SilentlyContinue)) {
  Fail 'Node não encontrado. Instale: winget install OpenJS.NodeJS.LTS'
}
Write-Ok "Node $(node --version)"

if ($Port -eq 0) {
  $Port = @(8080, 8081, 8082, 8090) | Where-Object { Test-PortFree $_ } | Select-Object -First 1
  if (-not $Port) { Fail 'As portas 8080, 8081, 8082 e 8090 estão ocupadas. Use -Port <número>.' }
} elseif (-not (Test-PortFree $Port)) {
  Fail "A porta $Port está ocupada."
}
if ($Port -ne 8080) { Write-Ok "Porta 8080 ocupada; o backend vai usar a $Port" }
if (-not (Test-PortFree 4200)) {
  Fail 'A porta 4200 (frontend) está ocupada. Feche o ng serve que estiver aberto e rode de novo.'
}

New-Item -ItemType Directory -Force -Path $stateDir | Out-Null

Write-Step "Subindo o backend na porta $Port (a primeira vez baixa dependências e demora)"
Start-DevWindow 'Simulador - backend' (Join-Path $root 'backend') (
  ".\mvnw.cmd spring-boot:run `"-Dspring-boot.run.profiles=local`" " +
  "`"-Dspring-boot.run.arguments=--server.port=$Port`"")
$healthy = Wait-Url "http://localhost:$Port/actuator/health" 600 {
  param($r) $r.Content -match '"status"\s*:\s*"UP"'
}
if (-not $healthy) { Fail 'O backend não respondeu em 10 minutos. Veja a janela "Simulador - backend".' }
Write-Ok "Backend no ar: http://localhost:$Port"

Write-Step 'Subindo o frontend'
$frontend = Join-Path $root 'frontend'
$install = ''
if (-not (Test-Path (Join-Path $frontend 'node_modules'))) { $install = 'npm ci; ' }
Start-DevWindow 'Simulador - frontend' $frontend "`$env:BACKEND_PORT = '$Port'; ${install}npm start"

if (-not $NoBridge) {
  Write-Step "Ligando a ponte ($Source, ativos: $symbolList)"
  $bridge = Join-Path $root 'mt5-bridge'
  $python = Join-Path $bridge '.venv\Scripts\python.exe'
  if (-not (Test-Path $python)) {
    Write-Ok 'Criando o ambiente Python da ponte (.venv)'
    if (Get-Command py -ErrorAction SilentlyContinue) { & py -3 -m venv (Join-Path $bridge '.venv') }
    elseif (Get-Command python -ErrorAction SilentlyContinue) { & python -m venv (Join-Path $bridge '.venv') }
    else { Fail 'Python não encontrado. Instale: winget install Python.Python.3.13' }
  }
  $extras = '.'
  if ($Source -eq 'mt5') { $extras = '.[mt5]' }
  Start-DevWindow 'Simulador - ponte' $bridge (
    "& '$python' -m pip install --quiet --disable-pip-version-check -e '$extras'; " +
    "`$env:BRIDGE_INGEST_TOKEN = '$ingestToken'; " +
    "& '$python' -m mt5_bridge --source $Source --symbols $symbolList --url ws://localhost:$Port/ingest")
}

Write-Step 'Esperando o frontend compilar'
if (Wait-Url 'http://localhost:4200' 300 { param($r) $r.StatusCode -eq 200 }) {
  Start-Process "http://localhost:4200/cotacoes"
  Write-Ok 'Pronto: http://localhost:4200/cotacoes'
} else {
  Write-Host '  O frontend ainda não respondeu; veja a janela "Simulador - frontend".' -ForegroundColor Yellow
}
Write-Host "`nPara encerrar tudo: .\dev.cmd -Stop`n"
