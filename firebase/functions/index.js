const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.onNewMessage = functions.firestore
    .document('chats/{chatId}/messages/{messageId}')
    .onCreate(async (snapshot, context) => {
        const message = snapshot.data();
        const toId = message.toId;
        const fromId = message.fromId;

        console.log(`Processing notification for message from ${fromId} to ${toId}`);

        try {
            // 1. Get recipient token
            const recipientDoc = await admin.firestore().collection('users').doc(toId).get();
            if (!recipientDoc.exists) {
                console.log('Recipient user document not found');
                return null;
            }

            const fcmToken = recipientDoc.data().fcmToken;
            if (!fcmToken) {
                console.log('Recipient has no FCM token');
                return null;
            }

            // 2. Get sender name
            const senderDoc = await admin.firestore().collection('users').doc(fromId).get();
            const senderName = senderDoc.exists ? (senderDoc.data().name || 'Новое сообщение') : 'Messenger';

            // 3. Prepare notification payload
            const payload = {
                notification: {
                    title: senderName,
                    body: message.text || 'Вложение',
                    sound: 'default',
                    clickAction: 'FLUTTER_NOTIFICATION_CLICK' // Standard for many frameworks, handled by our intent filter
                },
                data: {
                    chatId: context.params.chatId,
                    fromId: fromId,
                    type: 'chat_message'
                }
            };

            // 4. Send notification
            const options = {
                priority: 'high',
                timeToLive: 60 * 60 * 24 // 1 hour
            };

            const result = await admin.messaging().sendToDevice(fcmToken, payload, options);
            console.log('Notification sent successfully:', result);
            return result;

        } catch (error) {
            console.error('Error sending notification:', error);
            return null;
        }
    });
