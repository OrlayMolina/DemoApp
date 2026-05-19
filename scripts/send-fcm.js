/**
 * FCM sender script using firebase-admin.
 *
 * Usage:
 *   node send-fcm.js <topic|token> <value> <title> <body> [type] [userName] [publicationTitle] [relatedEntityId]
 *
 * - Si pasas <type> (uno de NotificationType: VERIFIED, REJECTED, NEW_PUBLICATION,
 *   REVIEW_REMINDER, LIKE, COMMENT, FOLLOWER), manda payload tipo data (high priority).
 *   La app dispara onMessageReceived y guarda la notificacion en Firestore para que
 *   aparezca en el Notifications screen.
 *
 * - Si no pasas <type>, manda payload tipo notification y la app solo muestra la system
 *   notification (no se guarda en Firestore).
 *
 * Examples:
 *   node send-fcm.js token eAbXXX... "Test" "Funciona!"
 *   node send-fcm.js topic moderators "Pendientes" "Tienes 5" REVIEW_REMINDER
 *   node send-fcm.js topic user_user_1_publications "Juan publico" "Nuevo lugar" NEW_PUBLICATION Juan "Mirador del Cafe" 1
 */

const path = require('path');
const admin = require('firebase-admin');

const SERVICE_ACCOUNT_PATH = path.join(__dirname, 'service-account.json');

let serviceAccount;
try {
  serviceAccount = require(SERVICE_ACCOUNT_PATH);
} catch (err) {
  console.error(`✗ No se encontró service-account.json en ${SERVICE_ACCOUNT_PATH}`);
  console.error('  Descárgalo desde Firebase Console → Project Settings → Service accounts → Generate new private key.');
  process.exit(1);
}

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

const [
  ,
  ,
  targetType,
  targetValue,
  title,
  body,
  type,
  userName,
  publicationTitle,
  relatedEntityId,
] = process.argv;

if (!targetType || !targetValue || !title || !body) {
  console.error('Uso: node send-fcm.js <topic|token> <value> <title> <body> [type] [userName] [publicationTitle] [relatedEntityId]');
  process.exit(1);
}

const message = {};

if (type) {
  // Data-only con high priority: onMessageReceived siempre dispara en la app
  // -> se construye la system notification manualmente Y se guarda en Firestore
  message.data = {
    title,
    body,
    type,
    userName: userName || '',
    publicationTitle: publicationTitle || '',
    relatedEntityId: relatedEntityId || '',
  };
  message.android = { priority: 'high' };
} else {
  // Notification-only: el sistema muestra la notif automaticamente, no guarda en Firestore
  message.notification = { title, body };
}

if (targetType === 'topic') {
  message.topic = targetValue;
} else if (targetType === 'token') {
  message.token = targetValue;
} else {
  console.error(`✗ Primer argumento debe ser "topic" o "token", recibido: "${targetType}"`);
  process.exit(1);
}

admin
  .messaging()
  .send(message)
  .then((response) => {
    const mode = type ? `data (${type})` : 'notification';
    console.log(`✓ Enviado a ${targetType}="${targetValue}" como ${mode}`);
    console.log(`  Message ID: ${response}`);
  })
  .catch((error) => {
    console.error(`✗ Error enviando a ${targetType}="${targetValue}":`);
    console.error(`  ${error.code || ''} ${error.message}`);
    process.exit(1);
  });
