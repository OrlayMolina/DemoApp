<#
.SYNOPSIS
    Envia un push notificando que un autor publico algo nuevo.
    Llega a los seguidores suscritos al topic user_<AuthorId>_publications.
    Se guarda en Firestore (notifications) como tipo NEW_PUBLICATION para aparecer en
    la pantalla Notifications de la app.

.PARAMETER AuthorId
    ID del autor (ej. user_1, user_2 o un UUID). Requerido.

.PARAMETER AuthorName
    Nombre que se muestra en el push. Default: "Un usuario".

.PARAMETER PlaceTitle
    Titulo del lugar publicado. Default: "un nuevo lugar".

.PARAMETER PostId
    Id del post (opcional). Se guarda como relatedEntityId.

.EXAMPLE
    .\notify-publication.ps1 user_1
    .\notify-publication.ps1 user_1 -AuthorName "Juan" -PlaceTitle "Mirador del Cafe" -PostId 1
#>
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string]$AuthorId,

    [string]$AuthorName = "Un usuario",

    [string]$PlaceTitle = "un nuevo lugar",

    [string]$PostId = ""
)

$OutputEncoding = [System.Text.UTF8Encoding]::new()
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new()

$topic = "user_${AuthorId}_publications"
$title = "Nueva publicacion de $AuthorName"
$body  = "$AuthorName compartio: $PlaceTitle"

Write-Host ""
Write-Host "--- Escenario 1: nueva publicacion ---" -ForegroundColor Cyan
Write-Host "  Topic:           $topic"
Write-Host "  Title:           $title"
Write-Host "  Body:            $body"
Write-Host "  Type:            NEW_PUBLICATION"
Write-Host ""

node "$PSScriptRoot\send-fcm.js" topic $topic $title $body "NEW_PUBLICATION" $AuthorName $PlaceTitle $PostId
