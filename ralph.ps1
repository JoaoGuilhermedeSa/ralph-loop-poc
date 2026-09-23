<#
.SYNOPSIS
  The Ralph loop: feed one unchanging prompt to a fresh agent, over and over,
  until the oracle goes green or the iteration budget runs out.

.EXAMPLE
  .\ralph.ps1 -Iterations 12
  .\ralph.ps1 -Iterations 20 -Model claude-sonnet-5 -MaxBudgetUsd 5
  .\ralph.ps1 -Reset
#>
[CmdletBinding()]
param(
    [int]$Iterations = 12,
    [string]$Model = '',
    [double]$MaxBudgetUsd = 0,
    [switch]$Reset,
    [switch]$Yes,
    [switch]$Force
)

$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$logDir = Join-Path $PSScriptRoot '.ralph\logs'
# The agent's JSON carries its final message verbatim; read it as UTF-8.
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

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
    # The lock records the hashes, so it must not move either: rewriting it
    # after editing a test would otherwise pass the check above.
    git diff --quiet ralph-start -- .ralph/oracle.lock
    if ($LASTEXITCODE -ne 0) {
        Write-Host 'Halting: .ralph/oracle.lock differs from ralph-start.' -ForegroundColor Red
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
    Write-Host 'Reset to the start state. .ralph/logs/run.csv keeps the history of earlier runs.' -ForegroundColor Yellow
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

# Pin the model and the spend when asked, and say what ran: a recording should show it.
$agentArgs = @('-p', '--dangerously-skip-permissions', '--output-format', 'json')
if ($Model) { $agentArgs += @('--model', $Model) }
if ($MaxBudgetUsd -gt 0) { $agentArgs += @('--max-budget-usd', $MaxBudgetUsd.ToString([Globalization.CultureInfo]::InvariantCulture)) }
$modelLabel = if ($Model) { $Model } else { 'Claude Code default' }
$budgetLabel = if ($MaxBudgetUsd -gt 0) { '${0:0.00} per iteration' -f $MaxBudgetUsd } else { 'no spend cap' }
$runCsv = Join-Path $logDir 'run.csv'
if (-not (Test-Path $runCsv)) {
    'started,iteration,seconds,passed,total,backend,frontend,commit,models,cost_usd,turns,tokens_in,tokens_out,subagents,outcome' |
        Set-Content -Encoding utf8 $runCsv
}
$runStarted = Get-Date
$totalCost = 0.0

Write-Host ''
Write-Host "ralph: starting at $($start.passed)/$($start.total), budget $Iterations iterations"
Write-Host "model: $modelLabel, $budgetLabel   ($(& claude --version))"

for ($i = 1; $i -le $Iterations; $i++) {
    Write-Rule "iteration $i of $Iterations"
    Test-Oracle
    $headBefore = (git rev-parse HEAD)
    $log = Join-Path $logDir ("iter-{0:d2}.log" -f $i)

    # A fresh context window every iteration. The repo is the only memory.
    $iterStarted = Get-Date
    Write-Host ("  agent working since {0:HH:mm:ss} ..." -f $iterStarted) -ForegroundColor DarkGray
    $out = $prompt | & claude @agentArgs
    ($out -join "`n") | Set-Content -Encoding utf8 $log
    $seconds = [int]((Get-Date) - $iterStarted).TotalSeconds
    & python tools\iteration_report.py $log
    $agent = (& python tools\iteration_report.py $log --fields)
    $cost = 0.0
    [void][double]::TryParse(($agent -split ',')[1], [Globalization.NumberStyles]::Float,
        [Globalization.CultureInfo]::InvariantCulture, [ref]$cost)
    $totalCost += $cost

    Test-Oracle
    $score = Get-Score
    $delta = $score.passed - $previous

    $colour = 'Gray'
    if ($delta -gt 0) { $colour = 'Green' }
    if ($delta -lt 0) { $colour = 'Red' }
    $sign = ''
    if ($delta -ge 0) { $sign = '+' }
    Write-Host ("  score {0}/{1} ({2}{3})   backend {4}   frontend {5}   {6}m{7:d2}s" -f `
            $score.passed, $score.total, $sign, $delta,
        $score.backend_passed, $score.frontend_passed,
        [int][math]::Floor($seconds / 60), ($seconds % 60)) -ForegroundColor $colour
    ('{0:s},{1},{2},{3},{4},{5},{6},{7},{8}' -f $runStarted, $i, $seconds,
        $score.passed, $score.total, $score.backend_passed, $score.frontend_passed,
        (git rev-parse --short HEAD), $agent) | Add-Content -Encoding utf8 $runCsv

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
Write-Host ("model {0}, wall clock {1:hh\:mm\:ss}, agent cost `${2:0.00}" -f `
        $modelLabel, ((Get-Date) - $runStarted), $totalCost)
& python verify.py
Write-Host ''
git --no-pager log --oneline ralph-start..HEAD
