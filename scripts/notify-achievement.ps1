<#
.SYNOPSIS
    Envia un push al usuario avisandole que desbloqueo un logro.
    Llega al topic user_<UserId>_inbox y se guarda en Firestore con tipo ACHIEVEMENT.

.PARAMETER UserId
    ID del usuario que obtuvo el logro. Requerido.

.PARAMETER AchievementTitle
    Titulo del logro. Default: "Nuevo logro".

.PARAMETER AchievementId
    ID del logro (opcional). Se guarda como relatedEntityId.

.EXAMPLE
    .\notify-achievement.ps1 7b93da9e-6164-45eb-852d-c70d21cd5e68
    .\notify-achievement.ps1 user_1 -AchievementTitle "Explorador Novato" -AchievementId 1
#>
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string]$UserId,

    [string]$AchievementTitle = "Nuevo logro",

    [string]$AchievementId = ""
)

$OutputEncoding = [System.Text.UTF8Encoding]::new()
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new()

$topic = "user_${UserId}_inbox"
$title = "Logro desbloqueado"
$body  = "Has desbloqueado el logro: $AchievementTitle"

Write-Host ""
Write-Host "--- Escenario 5: logro desbloqueado ---" -ForegroundColor Cyan
Write-Host "  Topic:           $topic"
Write-Host "  Title:           $title"
Write-Host "  Body:            $body"
Write-Host "  Type:            ACHIEVEMENT"
Write-Host ""

node "$PSScriptRoot\send-fcm.js" topic $topic $title $body "ACHIEVEMENT" "" $AchievementTitle $AchievementId
