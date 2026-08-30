$ErrorActionPreference = "Stop"
$stage = "resolving the portable folder"
$rootPath = Split-Path -Parent $PSScriptRoot
$url = "http://127.0.0.1:8080"
$javaPath = Join-Path $rootPath "runtime\bin\java.exe"
$jarPath = Join-Path $rootPath "app\dentalwave.jar"
$dataPath = Join-Path $rootPath "data"
$logsPath = Join-Path $rootPath "logs"
$backupsPath = Join-Path $rootPath "backups"
$pidPath = Join-Path $dataPath "dentalwave.pid"

function Test-DentalWaveReady {
    try {
        $response = Invoke-RestMethod -Uri "$url/api/portable/status" -TimeoutSec 2
        return $response.application -eq "DentalWave" -and $response.status -eq "ready"
    } catch {
        return $false
    }
}

function Test-DentalWaveBrowserPage {
    try {
        $page = Invoke-WebRequest -UseBasicParsing -Uri "$url/" -TimeoutSec 8
        if ($page.StatusCode -ne 200 -or $page.Content -notmatch '<div\s+id=["'']root["'']') {
            return $false
        }

        $scriptMatch = [regex]::Match(
            $page.Content,
            '<script[^>]+src=["''](?<path>/assets/[^"'']+\.js)["'']'
        )
        if (-not $scriptMatch.Success) {
            return $false
        }

        $scriptUrl = "$url$($scriptMatch.Groups['path'].Value)"
        $script = Invoke-WebRequest -UseBasicParsing -Uri $scriptUrl -TimeoutSec 8
        return $script.StatusCode -eq 200 -and $script.RawContentLength -gt 0
    } catch {
        return $false
    }
}

function Stop-UnresponsiveDentalWave {
    $controlSecretPath = Join-Path $dataPath "control.secret"
    if (-not (Test-Path -LiteralPath $controlSecretPath)) {
        return $false
    }

    try {
        $controlToken = [IO.File]::ReadAllText($controlSecretPath).Trim()
        if ([string]::IsNullOrWhiteSpace($controlToken)) {
            return $false
        }

        Invoke-RestMethod `
            -Uri "$url/api/portable/shutdown" `
            -Method Post `
            -Headers @{ "X-DentalWave-Control" = $controlToken } `
            -TimeoutSec 5 | Out-Null

        for ($attempt = 0; $attempt -lt 30; $attempt++) {
            if (-not (Test-PortInUse)) {
                return $true
            }
            Start-Sleep -Seconds 1
        }
    } catch {
        return $false
    }

    return $false
}

function Show-LauncherError([string]$Diagnostic) {
    try {
        New-Item -ItemType Directory -Force -Path $logsPath | Out-Null
        $launcherLog = Join-Path $logsPath "launcher-errors.log"
        Add-Content -LiteralPath $launcherLog -Value $Diagnostic
        $safeLauncherLog = $launcherLog.Replace('"', '')
        Start-Process -FilePath "notepad.exe" -ArgumentList ('"{0}"' -f $safeLauncherLog)
    } catch {
        # The launcher window still displays the readable message below.
    }
}

function Test-PortInUse {
    $client = New-Object Net.Sockets.TcpClient
    try {
        $connection = $client.BeginConnect("127.0.0.1", 8080, $null, $null)
        return $connection.AsyncWaitHandle.WaitOne(500, $false) -and $client.Connected
    } catch {
        return $false
    } finally {
        $client.Close()
    }
}

function Read-SecretFile([string]$Path, [int]$Bytes) {
    if (-not (Test-Path -LiteralPath $Path)) {
        $buffer = New-Object byte[] $Bytes
        $generator = [Security.Cryptography.RandomNumberGenerator]::Create()
        try {
            $generator.GetBytes($buffer)
        } finally {
            $generator.Dispose()
        }
        $value = [Convert]::ToBase64String($buffer)
        [IO.File]::WriteAllText($Path, $value, (New-Object Text.UTF8Encoding($false)))
    }

    return [IO.File]::ReadAllText($Path).Trim()
}

function Read-InitialPassword {
    while ($true) {
        Write-Host ""
        Write-Host "FIRST START SETUP"
        Write-Host "Create the shared user password. It must be at least 12 characters."
        $first = Read-Host "User password" -AsSecureString
        $second = Read-Host "Type it again" -AsSecureString
        $firstPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($first)
        $secondPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($second)
        try {
            $firstText = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($firstPointer)
            $secondText = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($secondPointer)
            if ($firstText.Length -lt 12) {
                Write-Host "The password must be at least 12 characters." -ForegroundColor Yellow
            } elseif ($firstText -cne $secondText) {
                Write-Host "The passwords did not match. Please try again." -ForegroundColor Yellow
            } else {
                return $firstText
            }
        } finally {
            [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($firstPointer)
            [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($secondPointer)
            $firstText = $null
            $secondText = $null
        }
    }
}

try {
    $stage = "checking for an existing DentalWave instance"
    if (Test-DentalWaveReady) {
        $stage = "checking the existing DentalWave browser page"
        if (Test-DentalWaveBrowserPage) {
            Write-Host "DentalWave is already running. Opening your browser..."
            Start-Process $url
            exit 0
        }

        $stage = "restarting an unresponsive DentalWave instance"
        Write-Host "DentalWave needs a quick restart. Please wait..." -ForegroundColor Yellow
        if (-not (Stop-UnresponsiveDentalWave)) {
            throw "DentalWave is running, but its browser page did not respond and the automatic restart could not stop it. Double-click CLOSE DENTALWAVE, then try again."
        }
    }

    $stage = "checking local port 8080"
    if (Test-PortInUse) {
        Write-Host "DentalWave cannot start because local port 8080 is already in use." -ForegroundColor Red
        Write-Host "Close the other local application and try again."
        exit 1
    }

    $stage = "validating the bundled runtime and application"
    if (-not (Test-Path -LiteralPath $javaPath)) {
        Write-Host "The bundled Java runtime is missing: runtime\bin\java.exe" -ForegroundColor Red
        exit 1
    }
    if (-not (Test-Path -LiteralPath $jarPath)) {
        Write-Host "The DentalWave application file is missing: app\dentalwave.jar" -ForegroundColor Red
        exit 1
    }

    $stage = "creating portable data folders"
    New-Item -ItemType Directory -Force -Path $dataPath, $logsPath, $backupsPath | Out-Null

    $stage = "loading portable security files"
    $env:JWT_SECRET = Read-SecretFile (Join-Path $dataPath "jwt.secret") 48
    $env:PORTABLE_CONTROL_TOKEN = Read-SecretFile (Join-Path $dataPath "control.secret") 32
    $env:DENTALWAVE_DATA_PATH = Join-Path $dataPath "dentalwave"
    $env:DENTALWAVE_PORTABLE_DATA_PATH = $dataPath
    $env:PORTABLE_MANAGER_USERNAME = "user"

    $stage = "checking first-start manager setup"
    $userInitialized = Test-Path -LiteralPath (Join-Path $dataPath "user-initialized.flag")
    $legacyManagerInitialized = Test-Path -LiteralPath (Join-Path $dataPath "manager-initialized.flag")
    if (-not $userInitialized -and -not $legacyManagerInitialized) {
        $env:PORTABLE_MANAGER_PASSWORD = Read-InitialPassword
    } else {
        $env:PORTABLE_MANAGER_PASSWORD = ""
    }

    $stage = "starting the bundled DentalWave application"
    $standardOutput = Join-Path $logsPath "console.log"
    $standardError = Join-Path $logsPath "startup-errors.log"
    Write-Host "Starting DentalWave..."
    $process = Start-Process -FilePath $javaPath `
        -ArgumentList @("-jar", "app\dentalwave.jar", "--spring.profiles.active=portable") `
        -WorkingDirectory $rootPath `
        -RedirectStandardOutput $standardOutput `
        -RedirectStandardError $standardError `
        -WindowStyle Hidden `
        -PassThru
    [IO.File]::WriteAllText($pidPath, [string]$process.Id, (New-Object Text.ASCIIEncoding))
    $env:PORTABLE_MANAGER_PASSWORD = ""

    $stage = "waiting for DentalWave readiness"
    for ($attempt = 0; $attempt -lt 90; $attempt++) {
        if (Test-DentalWaveReady) {
            $stage = "validating the packaged DentalWave browser page"
            if (-not (Test-DentalWaveBrowserPage)) {
                throw "The backend started, but the packaged HTML or JavaScript did not respond."
            }
            Write-Host "DentalWave is ready. Opening your browser..."
            Start-Process $url
            exit 0
        }
        if ($process.HasExited) {
            break
        }
        Start-Sleep -Seconds 1
    }

    Write-Host "DentalWave could not start." -ForegroundColor Red
    Write-Host "See logs\dentalwave.log and logs\startup-errors.log for details."
    $timeoutDiagnostic = "$(Get-Date -Format s) | Stage: $stage | Root: $rootPath | Error: DentalWave did not become ready within 90 seconds."
    Show-LauncherError $timeoutDiagnostic
    exit 1
} catch {
    $message = $_.Exception.Message
    $safeRoot = if ($rootPath) { $rootPath.Replace('"', '') } else { "unavailable" }
    $diagnostic = "$(Get-Date -Format s) | Stage: $stage | Root: $safeRoot | Error: $message"
    try {
        if ($logsPath) {
            Show-LauncherError $diagnostic
        }
    } catch {
        # The readable console message below is still available if logging fails.
    }
    Write-Host "DentalWave could not start during '$stage': $message" -ForegroundColor Red
    exit 1
} finally {
    $env:PORTABLE_MANAGER_PASSWORD = ""
    $env:JWT_SECRET = ""
    $env:PORTABLE_CONTROL_TOKEN = ""
}
