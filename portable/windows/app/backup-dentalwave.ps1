$ErrorActionPreference = "Stop"
$rootPath = Split-Path -Parent $PSScriptRoot
$url = "http://127.0.0.1:8080"
$source = Join-Path $rootPath "data\dentalwave.mv.db"
$backupPath = Join-Path $rootPath "backups"

try {
    try {
        $response = Invoke-RestMethod -Uri "$url/api/portable/status" -TimeoutSec 2
        if ($response.application -eq "DentalWave" -and $response.status -eq "ready") {
            Write-Host "Close DentalWave before creating a backup." -ForegroundColor Yellow
            exit 1
        }
    } catch {
        # No verified DentalWave server is running; an offline file copy is safe.
    }

    if (-not (Test-Path -LiteralPath $source)) {
        Write-Host "No DentalWave data exists yet, so no backup was needed."
        exit 0
    }

    New-Item -ItemType Directory -Force -Path $backupPath | Out-Null
    $stamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $destination = Join-Path $backupPath "dentalwave-$stamp.mv.db"
    Copy-Item -LiteralPath $source -Destination $destination

    Get-ChildItem -LiteralPath $backupPath -Filter "dentalwave-*.mv.db" -File |
        Sort-Object LastWriteTime -Descending |
        Select-Object -Skip 7 |
        Remove-Item -Force

    Write-Host "Backup created: backups\$(Split-Path -Leaf $destination)"
    exit 0
} catch {
    Write-Host "Backup failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
