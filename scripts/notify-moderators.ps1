<#
.SYNOPSIS
    Envia un push de recordatorio a todos los moderadores con publicaciones pendientes.
    Llega al topic "moderators" (al que se suscriben users con role = ADMIN al hacer login).
    Se guarda en Firestore (notifications) como tipo REVIEW_REMINDER.

.PARAMETER PendingCount
    Cantidad de publicaciones pendientes a mencionar. Default: 5.

.EXAMPLE
    .\notify-moderators.ps1
    .\notify-moderators.ps1 -PendingCount 12
#>
param(
    [int]$PendingCount = 5
)

$OutputEncoding = [System.Text.UTF8Encoding]::new()
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new()

$topic = "moderators"
$title = "Publicaciones pendientes de revision"
$body  = "Tienes $PendingCount publicaciones esperando revision. No dejes que la comunidad espere."

Write-Host ""
Write-Host "--- Escenario 3: recordatorio a moderadores ---" -ForegroundColor Cyan
Write-Host "  Topic:           $topic"
Write-Host "  Title:           $title"
Write-Host "  Body:            $body"
Write-Host "  Type:            REVIEW_REMINDER"
Write-Host ""

node "$PSScriptRoot\send-fcm.js" topic $topic $title $body "REVIEW_REMINDER" "" "" ""
