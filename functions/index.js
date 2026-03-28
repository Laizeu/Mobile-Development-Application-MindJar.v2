const { onCall, HttpsError } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");

admin.initializeApp();

exports.deleteAccount = onCall({
    region: "us-central1",
    cors: true
}, async (request) => {

    if (!request.auth) {
        throw new HttpsError(
            "unauthenticated",
            "Must be signed in to delete account."
        );
    }

    const uid = request.auth.uid;

    try {
        await admin.auth().deleteUser(uid);
        return { success: true };
    } catch (error) {
        throw new HttpsError(
            "internal",
            "Failed to delete account: " + error.message
        );
    }
});