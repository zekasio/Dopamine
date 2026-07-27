import * as functions from 'firebase-functions/v1';
import * as admin from 'firebase-admin';
import { getMessaging } from 'firebase-admin/messaging';

admin.initializeApp();

export const sendNotification = functions.https.onCall(async (data, context) => {
    const { title, body, tokens } = data;

    if (!tokens || tokens.length === 0) {
        throw new functions.https.HttpsError('invalid-argument', 'No tokens provided');
    }

    const message = {
        notification: {
            title: title || 'Yeni Bildirim',
            body: body || '',
        },
        tokens: tokens,
    };

    try {
        const response = await getMessaging().sendEachForMulticast(message);
        return { success: true, response };
    } catch (error) {
        console.error('Error sending message:', error);
        throw new functions.https.HttpsError('internal', 'Error sending notification');
    }
});
