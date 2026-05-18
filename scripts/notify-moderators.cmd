@echo off
powershell -ExecutionPolicy Bypass -File "%~dp0notify-moderators.ps1" %*
