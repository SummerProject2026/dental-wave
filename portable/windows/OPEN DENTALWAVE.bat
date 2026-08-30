@echo off
setlocal EnableExtensions
cd /d "%~dp0"
title DentalWave Launcher

powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0OtherInfo\app\start-dentalwave.ps1"
if errorlevel 1 (
    echo.
    echo DentalWave could not start.
    echo See OtherInfo\logs for details.
    echo.
    pause
)

endlocal
