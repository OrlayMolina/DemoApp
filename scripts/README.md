# FCM sender scripts

Manual notification dispatcher for DemoApp's FCM topics. Useful for the class demo since
Firebase Console removed Topic targeting from the Notifications composer UI.

## Setup (una sola vez)

### 1. Service account JSON

1. Abre [Firebase Console](https://console.firebase.google.com) → proyecto **demoapp-5c75e**
2. ⚙️ **Project Settings** → pestaña **Service accounts**
3. Asegúrate de que esté seleccionado **Node.js**
4. Click **Generate new private key** → confirma → se descarga un JSON
5. Renómbralo a `service-account.json` y guárdalo en esta carpeta (`scripts/`)

⚠️ Este archivo da control total del proyecto Firebase — **NO lo subas al repo**.
Ya está agregado a `.gitignore`.

### 2. Node.js

```powershell
node --version
```

Si dice `command not found` o algo así, instálalo desde [nodejs.org](https://nodejs.org)
(versión LTS).

### 3. Instalar dependencias

Desde esta carpeta (`scripts/`):

```powershell
npm install
```

Esto crea `node_modules/` con `firebase-admin`. La carpeta está en `.gitignore`.

## Uso

### Atajos para los 3 escenarios del demo (recomendado)

Cada escenario tiene su wrapper en PowerShell que ya trae el topic, título y body armados.

**1. Notif de nueva publicación a seguidores** (topic `user_<AuthorId>_publications`):

```powershell
.\notify-publication.ps1 user_1
.\notify-publication.ps1 user_1 -AuthorName "Juan" -PlaceTitle "Mirador del Café"
```

**2. Notif de aprobación/rechazo al autor** (topic `user_<UserId>_inbox`):

```powershell
.\notify-approval.ps1 7b93da9e-6164-45eb-852d-c70d21cd5e68
.\notify-approval.ps1 7b93da9e-6164-45eb-852d-c70d21cd5e68 -Mode reject -PostTitle "Mirador del Café"
```

**3. Recordatorio a moderadores** (topic `moderators`):

```powershell
.\notify-moderators.ps1
.\notify-moderators.ps1 -PendingCount 12
```

> Si PowerShell se queja con `cannot be loaded because running scripts is disabled`, abre una PowerShell **como administrador** y corre una sola vez:
> `Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser`

### Comando crudo (sin atajos)

Si necesitas mandar algo custom:

```
node send-fcm.js <topic|token> <valor> <título> <cuerpo>
```

Ejemplos:

```powershell
node send-fcm.js topic user_user_1_publications "Custom" "Mensaje a seguidores de user_1"
node send-fcm.js topic moderators "Aviso" "Texto cualquiera"
```

### Probar a un device específico (con token)

Para verificar end-to-end que el FCM funciona en tu device:

```powershell
# Toma el fcmToken del doc del user en Firestore Console
node send-fcm.js token "eAbXXX...token_completo..." "Test" "Funciona!"
```

## Troubleshooting

| Error | Causa | Fix |
|---|---|---|
| `No se encontró service-account.json` | Falta el JSON o está en otro lugar | Confirma que está en `scripts/service-account.json` |
| `messaging/registration-token-not-registered` | El token del device caducó | Reinstala la app o vuelve a hacer login para refrescar |
| `messaging/invalid-argument` (topic) | Nombre de topic con caracteres inválidos | Topics solo aceptan `[a-zA-Z0-9-_.~%]+` |
| Notif no llega | Device en modo Doze / batería optimizada | Desactiva optimización de batería para la app durante el demo |
