<#
.SYNOPSIS
    Envia un push al autor del post avisando que fue aprobado o rechazado.
    Llega al topic user_<UserId>_inbox (al que el user se suscribe al hacer login).
    Se guarda en Firestore (notifications) como tipo VERIFIED (approve) o REJECTED.

.PARAMETER UserId
    ID del usuario destinatario (autor del post). Requerido.

.PARAMETER Mode
    "approve" o "reject". Default: "approve".

.PARAMETER PostTitle
    Titulo del post. Default: "Tu publicacion".

.PARAMETER PostId
    Id del post (opcional). Se guarda como relatedEntityId.

.EXAMPLE
    .\notify-approval.ps1 7b93da9e-6164-45eb-852d-c70d21cd5e68
    .\notify-approval.ps1 user_2 -Mode reject -PostTitle "Mirador del Cafe" -PostId 5
#>
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string]$UserId,

    [ValidateSet("approve", "reject")]
    [string]$Mode = "approve",

    [string]$PostTitle = "Tu publicacion",

    [string]$PostId = ""
)

$OutputEncoding = [System.Text.UTF8Encoding]::new()
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new()

$topic = "user_${UserId}_inbox"

if ($Mode -eq "approve") {
    $title = "Publicacion aprobada"
    $body  = "$PostTitle ya esta visible para la comunidad."
    $type  = "VERIFIED"
} else {
    $title = "Publicacion rechazada"
    $body  = "$PostTitle no cumple los criterios. Revisa los detalles en la app."
    $type  = "REJECTED"
}

Write-Host ""
Write-Host "--- Escenario 2: $Mode ---" -ForegroundColor Cyan
Write-Host "  Topic:           $topic"
Write-Host "  Title:           $title"
Write-Host "  Body:            $body"
Write-Host "  Type:            $type"
Write-Host ""

node "$PSScriptRoot\send-fcm.js" topic $topic $title $body $type "" $PostTitle $PostId
