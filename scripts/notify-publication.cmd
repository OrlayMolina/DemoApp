@echo off
powershell -ExecutionPolicy Bypass -File "%~dp0notify-publication.ps1" %*
