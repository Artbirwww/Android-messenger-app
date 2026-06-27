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
            // 1. Получаем токен получателя
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

            // 2. Получаем имя отправителя
            const senderDoc = await admin.firestore().collection('users').doc(fromId).get();
            const senderName = senderDoc.exists ? (senderDoc.data().name || 'Новое сообщение') : 'Messenger';

            // 3. Формируем сообщение в формате FCM HTTP v1
            const fcmMessage = {
                token: fcmToken,
                notification: {
                    title: senderName,
                    body: message.text || 'Вложение',
                },
                data: {
                    chatId: context.params.chatId,
                    fromId: fromId,
                    type: 'chat_message'
                },
                android: {
                    priority: 'high',
                    notification: {
                        sound: 'default',
                        clickAction: 'FLUTTER_NOTIFICATION_CLICK'
                    }
                },
                apns: {
                    payload: {
                        aps: {
                            sound: 'default'
                        }
                    }
                }
            };

            // 4. Отправляем уведомление через новый метод send()
            const result = await admin.messaging().send(fcmMessage);
            console.log('Notification sent successfully:', result);
            return result;

        } catch (error) {
            console.error('Error sending notification:', error);
            return null;
        }
    });
