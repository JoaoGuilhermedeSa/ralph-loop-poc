<#
.SYNOPSIS
  Run what the loop built: backend on in-memory H2 with a demo account, and the
  React page on the Vite dev server. No database server needed.

.EXAMPLE
  powershell -ExecutionPolicy Bypass -File .\demo.ps1
  powershell -ExecutionPolicy Bypass -File .\demo.ps1 -BackendPort 8082 -NoBrowser

  Ctrl+C stops both. The backend logs to backend\target\demo-backend.log.
#>
[CmdletBinding()]
param(
    [int]$BackendPort = 8081,   # not 8080: other software often holds it
    [switch]$NoBrowser
)

$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

function Test-PortFree($port) {
    $listener = $null
    try {
        $listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback, $port)
        $listener.Start()
        return $true
    }
    catch { return $false }
    finally { if ($listener) { $listener.Stop() } }
}

foreach ($port in @($BackendPort, 5173)) {
    if (-not (Test-PortFree $port)) {
        Write-Host "Port $port is already in use. Free it, or pick another with -BackendPort." -ForegroundColor Red
        exit 1
    }
}

if (-not (Test-Path 'frontend\node_modules')) {
    Write-Host 'Installing frontend dependencies (once)...' -ForegroundColor DarkGray
    & npm install --prefix frontend
}

# The backend runs without a window and logs to a file, so it does not bury the
# dev server's output. cmd.exe runs mvn: a nested PowerShell would mangle the
# -Dname.with.dots=value arguments.
$env:DEMO_BACKEND_PORT = "$BackendPort"
$backendLog = Join-Path $PSScriptRoot 'backend\target\demo-backend.log'
New-Item -ItemType Directory -Force -Path (Split-Path $backendLog) | Out-Null
$psi = [System.Diagnostics.ProcessStartInfo]::new('cmd.exe',
    "/c mvn -q spring-boot:run -Dspring-boot.run.useTestClasspath=true -Dspring-boot.run.profiles=demo > `"$backendLog`" 2>&1")
$psi.WorkingDirectory = Join-Path $PSScriptRoot 'backend'
$psi.UseShellExecute = $false
$psi.CreateNoWindow = $true
$backend = [System.Diagnostics.Process]::Start($psi)

try {
    Write-Host "Starting the backend on :$BackendPort (first start compiles, ~30 s)..." -NoNewline
    $api = "http://localhost:$BackendPort/api/towns"
    $up = $false
    for ($i = 0; $i -lt 90 -and -not $up; $i++) {
        Start-Sleep -Seconds 2
        Write-Host '.' -NoNewline
        try { $up = (Invoke-WebRequest $api -UseBasicParsing -TimeoutSec 2).StatusCode -eq 200 } catch { }
        if ($backend.HasExited) { break }
    }
    Write-Host ''
    if (-not $up) {
        Write-Host "The backend did not come up. Last lines of $backendLog :" -ForegroundColor Red
        Get-Content $backendLog -Tail 15 -ErrorAction SilentlyContinue
        exit 1
    }

    Write-Host "Backend ready (log: backend\target\demo-backend.log)." -ForegroundColor Green
    Write-Host "Page: http://localhost:5173   (Ctrl+C stops both)" -ForegroundColor Green
    if (-not $NoBrowser) { Start-Process 'http://localhost:5173' }

    $env:API_URL = "http://localhost:$BackendPort"
    # vite directly: PowerShell swallows the `--` that `npm run dev -- ...` needs.
    Push-Location frontend
    try { & npx vite --strictPort } finally { Pop-Location }
}
finally {
    # mvn forks the application JVM; stop the whole tree, not just the window.
    if (-not $backend.HasExited) {
        & taskkill /T /F /PID $backend.Id | Out-Null
    }
    Write-Host 'Demo stopped.'
}
