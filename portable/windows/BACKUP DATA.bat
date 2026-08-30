@echo off
setlocal EnableExtensions
cd /d "%~dp0"
title Back Up DentalWave

powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0app\backup-dentalwave.ps1"
if errorlevel 1 (
    echo.
    echo The backup was not created. See the message above.
    echo.
    pause
) else (
    echo.
    pause
)

endlocal
