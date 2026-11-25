@echo off
rem Sobe backend, frontend e ponte do MT5 de uma vez. Opcoes: veja scripts\dev.ps1.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\dev.ps1" %*
