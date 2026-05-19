<#
.SYNOPSIS
    Envia un push avisando que un usuario empezo a seguir al destinatario.
    Llega al topic user_<UserId>_inbox del usuario seguido.

.PARAMETER UserId
    ID del usuario destinatario (el que recibe el nuevo seguidor). Requerido.

.PARAMETER FollowerName
    Nombre del nuevo seguidor a mostrar en el push. Default: "Un usuario".

.PARAMETER FollowerId
    ID del nuevo seguidor (opcional). Se guarda como relatedEntityId.

.EXAMPLE
    .\notify-follower.ps1 user_1
    .\notify-follower.ps1 user_2 -FollowerName "Juan" -FollowerId user_1
#>
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string]$UserId,

    [string]$FollowerName = "Un usuario",

    [string]$FollowerId = ""
)

$OutputEncoding = [System.Text.UTF8Encoding]::new()
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new()

$topic = "user_${UserId}_inbox"
$title = "Tienes un nuevo seguidor"
$body  = "$FollowerName comenzo a seguirte."

Write-Host ""
Write-Host "--- Escenario 4: nuevo seguidor ---" -ForegroundColor Cyan
Write-Host "  Topic:           $topic"
Write-Host "  Title:           $title"
Write-Host "  Body:            $body"
Write-Host "  Type:            FOLLOWER"
Write-Host ""

node "$PSScriptRoot\send-fcm.js" topic $topic $title $body "FOLLOWER" $FollowerName "" $FollowerId
