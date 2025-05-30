// This code runs in a trusted server environment using Firebase Admin SDK

const admin = require('firebase-admin');

// Assume admin SDK is already initialized
// admin.initializeApp();

/**
 * Sets a custom claim on a user to make them an admin.
 * @param {string} uid The user's Firebase Authentication UID.
 */
async function makeUserAdmin(uid) {
  try {
    await admin.auth().setCustomUserClaims(uid, { admin: true, worker: true }); // Admins might also need worker access
    console.log(`User ${uid} is now an admin.`);
    // You might want to notify the user or log this event
  } catch (error) {
    console.error('Error setting custom claims:', error);
  }
}

/**
 * Sets a custom claim on a user to make them a worker (but not admin).
 * @param {string} uid The user's Firebase Authentication UID.
 */
async function makeUserWorker(uid) {
  try {
    await admin.auth().setCustomUserClaims(uid, { worker: true }); // Just worker
    console.log(`User ${uid} is now a worker.`);
  } catch (error) {
    console.error('Error setting custom claims:', error);
  }
}

// Example usage (you would call these functions based on your app's logic)
// const someUserId = '...' // Get this from your database or request
// makeUserAdmin(someUserId);
// makeUserWorker(someUserId);

