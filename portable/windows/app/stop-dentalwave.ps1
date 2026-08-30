$ErrorActionPreference = "Stop"
$rootPath = Split-Path -Parent $PSScriptRoot
$url = "http://127.0.0.1:8080"
$dataPath = Join-Path $rootPath "data"
$pidPath = Join-Path $dataPath "dentalwave.pid"
$controlPath = Join-Path $dataPath "control.secret"
$backupScript = Join-Path $rootPath "app\backup-dentalwave.ps1"

function Test-DentalWaveReady {
    try {
        $response = Invoke-RestMethod -Uri "$url/api/portable/status" -TimeoutSec 2
        return $response.application -eq "DentalWave" -and $response.status -eq "ready"
    } catch {
        return $false
    }
}

function Get-VerifiedDentalWaveProcess {
    try {
        $matches = @(
            Get-CimInstance Win32_Process `
                -Filter "Name = 'java.exe' OR Name = 'javaw.exe'" |
                Where-Object {
                    $_.CommandLine -like "*dentalwave.jar*" -and
                    $_.CommandLine -like "*--spring.profiles.active=portable*"
                }
        )

        if ($matches.Count -eq 1) {
            return $matches[0]
        }
    } catch {
        return $null
    }

    return $null
}

function Stop-VerifiedDentalWaveProcess {
    $dentalWaveProcess = Get-VerifiedDentalWaveProcess
    if ($null -eq $dentalWaveProcess) {
        return $false
    }

    Write-Host "Found the older DentalWave process. Closing it..." -ForegroundColor Yellow
    Stop-Process -Id $dentalWaveProcess.ProcessId -Force
    for ($attempt = 0; $attempt -lt 30; $attempt++) {
        if (-not (Get-Process -Id $dentalWaveProcess.ProcessId -ErrorAction SilentlyContinue)) {
            return $true
        }
        Start-Sleep -Seconds 1
    }

    return $false
}

try {
    if (-not (Test-DentalWaveReady)) {
        if (Test-Path -LiteralPath $pidPath) {
            $recordedPid = [int]([IO.File]::ReadAllText($pidPath).Trim())
            if (Get-Process -Id $recordedPid -ErrorAction SilentlyContinue) {
                Write-Host "A process is recorded for DentalWave, but its identity cannot be verified." -ForegroundColor Red
                Write-Host "Do not remove the USB. Try CLOSE DENTALWAVE again in a moment."
                exit 1
            }
            Remove-Item -LiteralPath $pidPath -Force
        }
        Write-Host "DentalWave is already stopped."
        & $backupScript
        exit $LASTEXITCODE
    }

    if (-not (Test-Path -LiteralPath $controlPath) -or -not (Test-Path -LiteralPath $pidPath)) {
        Write-Host "DentalWave is running from an older portable location." -ForegroundColor Yellow
        if (Stop-VerifiedDentalWaveProcess) {
            Write-Host "DentalWave has stopped."
            & $backupScript
            if ($LASTEXITCODE -ne 0) {
                exit $LASTEXITCODE
            }
            Write-Host "You may now safely eject the USB drive."
            exit 0
        }

        Write-Host "DentalWave could not safely identify exactly one matching process." -ForegroundColor Red
        Write-Host "For safety, no unrelated Java process was stopped."
        exit 1
    }

    $recordedPid = [int]([IO.File]::ReadAllText($pidPath).Trim())
    $controlToken = [IO.File]::ReadAllText($controlPath).Trim()
    Write-Host "Closing DentalWave safely..."
    Invoke-RestMethod -Method Post -Uri "$url/api/portable/shutdown" `
        -Headers @{ "X-DentalWave-Control" = $controlToken } -TimeoutSec 5 | Out-Null

    for ($attempt = 0; $attempt -lt 30; $attempt++) {
        if (-not (Get-Process -Id $recordedPid -ErrorAction SilentlyContinue)) {
            Remove-Item -LiteralPath $pidPath -Force -ErrorAction SilentlyContinue
            Write-Host "DentalWave has stopped."
            & $backupScript
            if ($LASTEXITCODE -ne 0) {
                exit $LASTEXITCODE
            }
            Write-Host "You may now safely eject the USB drive."
            exit 0
        }
        Start-Sleep -Seconds 1
    }

    Write-Host "DentalWave did not stop within 30 seconds." -ForegroundColor Red
    Write-Host "Do not remove the USB. No unrelated Java process was terminated."
    exit 1
} catch {
    Write-Host "DentalWave could not be stopped safely: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
