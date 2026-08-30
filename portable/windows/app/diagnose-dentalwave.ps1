$ErrorActionPreference = "Continue"
$rootPath = Split-Path -Parent $PSScriptRoot
$url = "http://127.0.0.1:8080"
$logsPath = Join-Path $rootPath "logs"
$reportPath = Join-Path $rootPath "DENTALWAVE DIAGNOSTICS.txt"

$lines = New-Object System.Collections.Generic.List[string]
function Add-Report([string]$Line) {
    [void]$lines.Add($Line)
}

function Test-Page([string]$Name, [string]$Address) {
    try {
        $response = Invoke-WebRequest -UseBasicParsing -Uri $Address -TimeoutSec 8
        Add-Report "$Name : HTTP $($response.StatusCode), $($response.RawContentLength) bytes"
        return $response
    } catch {
        Add-Report "$Name : FAILED - $($_.Exception.Message)"
        return $null
    }
}

Add-Report "DENTALWAVE PORTABLE DIAGNOSTICS"
Add-Report "Generated: $(Get-Date -Format s)"
Add-Report "Portable folder: $rootPath"
Add-Report "Windows: $([Environment]::OSVersion.VersionString)"
Add-Report "PowerShell: $($PSVersionTable.PSVersion)"
Add-Report ""
Add-Report "FILES"
foreach ($relativePath in @("runtime\bin\java.exe", "app\dentalwave.jar", "data\dentalwave.mv.db")) {
    $fullPath = Join-Path $rootPath $relativePath
    if (Test-Path -LiteralPath $fullPath) {
        $item = Get-Item -LiteralPath $fullPath
        Add-Report "$relativePath : present, $($item.Length) bytes"
    } else {
        Add-Report "$relativePath : MISSING"
    }
}

Add-Report ""
Add-Report "LOCAL WEB CHECKS"
$status = Test-Page "Backend status" "$url/api/portable/status"
$page = Test-Page "Application page" "$url/"
if ($page -and $page.Content) {
    $scriptMatch = [regex]::Match(
        $page.Content,
        '<script[^>]+src=["''](?<path>/assets/[^"'']+\.js)["'']'
    )
    if ($scriptMatch.Success) {
        Test-Page "JavaScript bundle" "$url$($scriptMatch.Groups['path'].Value)" | Out-Null
    } else {
        Add-Report "JavaScript bundle : FAILED - no packaged script was listed in the HTML"
    }
}

Add-Report ""
Add-Report "RECENT LOG OUTPUT"
foreach ($logName in @("launcher-errors.log", "startup-errors.log", "dentalwave.log", "console.log")) {
    $logPath = Join-Path $logsPath $logName
    Add-Report ""
    Add-Report "--- $logName ---"
    if (Test-Path -LiteralPath $logPath) {
        Get-Content -LiteralPath $logPath -Tail 80 | ForEach-Object { Add-Report $_ }
    } else {
        Add-Report "No file found."
    }
}

try {
    [IO.File]::WriteAllLines($reportPath, $lines, (New-Object Text.UTF8Encoding($false)))
    $safeReportPath = $reportPath.Replace('"', '')
    Start-Process -FilePath "notepad.exe" -ArgumentList ('"{0}"' -f $safeReportPath)
    Write-Host "Diagnostics were saved to:"
    Write-Host $reportPath
} catch {
    Write-Host "Could not save the diagnostics report: $($_.Exception.Message)" -ForegroundColor Red
    $lines | ForEach-Object { Write-Host $_ }
    exit 1
}
