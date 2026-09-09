<#
.SYNOPSIS
  The Ralph loop: feed one unchanging prompt to a fresh agent, over and over,
  until the oracle goes green or the iteration budget runs out.

.EXAMPLE
  .\ralph.ps1 -Iterations 12
  .\ralph.ps1 -Reset
#>
[CmdletBinding()]
param(
    [int]$Iterations = 12,
    [switch]$Reset,
    [switch]$Yes,
    [switch]$Force
)

$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$logDir = Join-Path $PSScriptRoot '.ralph\logs'

function Write-Rule($text) {
    Write-Host ''
    Write-Host ("-- $text ").PadRight(72, '-') -ForegroundColor DarkCyan
}

function Get-Score {
    $raw = & python verify.py --json
    return $raw | ConvertFrom-Json
}

function Test-Oracle {
    & python tools\oracle_lock.py check
    if ($LASTEXITCODE -ne 0) {
        Write-Host 'Halting: the files Ralph may not edit have changed.' -ForegroundColor Red
        exit 1
    }
}

if ($Reset) {
    $dirty = git status --porcelain
    if ($dirty -and -not $Force) {
        Write-Host '-Reset would discard uncommitted changes:' -ForegroundColor Red
        git status --short
        Write-Host 'Commit them first, or re-run with -Reset -Force.'
        exit 1
    }
    git reset --hard ralph-start --quiet
    git clean -fdxq backend/src/main/java frontend/src backend/src/test/java/ots/charcreate/unit frontend/tests/unit
    Remove-Item (Join-Path $logDir '*.log') -ErrorAction SilentlyContinue
    Write-Host 'Reset to the start state.' -ForegroundColor Yellow
    & python verify.py
    exit 0
}

if (-not $Yes) {
    Write-Host 'This runs the agent unattended with --dangerously-skip-permissions.' -ForegroundColor Yellow
    Write-Host 'It will edit files and commit on its own.'
    $answer = Read-Host 'Continue? [y/N]'
    if ($answer -ne 'y') { exit 0 }
}

New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$prompt = Get-Content (Join-Path $PSScriptRoot 'PROMPT.md') -Raw
$start = Get-Score
$previous = $start.passed
$stalls = 0

Write-Host ''
Write-Host "ralph: starting at $($start.passed)/$($start.total), budget $Iterations iterations"

for ($i = 1; $i -le $Iterations; $i++) {
    Write-Rule "iteration $i of $Iterations"
    Test-Oracle
    $headBefore = (git rev-parse HEAD)
    $log = Join-Path $logDir ("iter-{0:d2}.log" -f $i)

    # A fresh context window every iteration. The repo is the only memory.
    $prompt | & claude -p --dangerously-skip-permissions 2>&1 | Tee-Object -FilePath $log

    Test-Oracle
    $score = Get-Score
    $delta = $score.passed - $previous

    $colour = 'Gray'
    if ($delta -gt 0) { $colour = 'Green' }
    if ($delta -lt 0) { $colour = 'Red' }
    $sign = ''
    if ($delta -ge 0) { $sign = '+' }
    Write-Host ("  score {0}/{1} ({2}{3})   backend {4}   frontend {5}" -f `
            $score.passed, $score.total, $sign, $delta,
        $score.backend_passed, $score.frontend_passed) -ForegroundColor $colour

    if ($delta -lt 0) {
        Write-Host '  regression; the next iteration must fix it before taking new work.' -ForegroundColor Red
    }

    if ((git rev-parse HEAD) -eq $headBefore) {
        $stalls++
        Write-Host "  no commit this iteration (stall $stalls of 3)" -ForegroundColor Yellow
        if ($stalls -ge 3) {
            Write-Host 'Halting: three iterations with nothing committed. Go read fix_plan.md.' -ForegroundColor Red
            break
        }
    }
    else {
        $stalls = 0
    }

    $previous = $score.passed
    $remaining = (Select-String -Path fix_plan.md -Pattern '^\s*- \[ \]' -AllMatches).Count
    if ($score.green -and $remaining -eq 0) {
        Write-Rule 'done'
        Write-Host "Green, and the backlog is empty, after $i iterations." -ForegroundColor Green
        break
    }
}

Write-Rule 'summary'
& python verify.py
Write-Host ''
git --no-pager log --oneline ralph-start..HEAD
