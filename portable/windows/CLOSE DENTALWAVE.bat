@echo off
setlocal EnableExtensions
cd /d "%~dp0"
title Close DentalWave

powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0OtherInfo\app\stop-dentalwave.ps1"
if errorlevel 1 (
    echo.
    echo DentalWave could not be confirmed as stopped.
    echo Do not remove the USB yet. See the message above.
    echo.
    pause
) else (
    echo.
    pause
)

endlocal
