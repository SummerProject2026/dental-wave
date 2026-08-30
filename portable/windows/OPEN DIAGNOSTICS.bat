@echo off
setlocal EnableExtensions
cd /d "%~dp0"
title DentalWave Diagnostics

powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0app\diagnose-dentalwave.ps1"
if errorlevel 1 (
    echo.
    echo DentalWave diagnostics could not be saved.
    echo.
    pause
)

endlocal
