# Firestore Security Rules

## ⚠️ CRITICAL: Set These Rules Before Going Live!

Copy and paste these rules into Firebase Console → Firestore Database → Rules

## Security Rules for MVP App

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Helper function: Check if user is authenticated
    function isAuthenticated() {
      return request.auth != null;
    }
    
    // Helper function: Check if user owns the document
    function isOwner(userId) {
      return isAuthenticated() && request.auth.uid == userId;
    }
    
    // Users collection - users can only read/write their own data
    match /users/{userId} {
      allow read: if isAuthenticated();
      allow write: if isOwner(userId);
    }
    
    // Tickets collection
    match /tickets/{ticketId} {
      // All authenticated users can read tickets
      allow read: if isAuthenticated();
      
      // Only authenticated users can create tickets
      allow create: if isAuthenticated();
      
      // Only authenticated users can update tickets
      allow update: if isAuthenticated();
      
      // No deletes for MVP (prevent accidental data loss)
      allow delete: if false;
    }
    
    // Jobs collection
    match /jobs/{jobId} {
      // All authenticated users can read jobs
      allow read: if isAuthenticated();
      
      // Only authenticated users can create/update jobs
      allow create: if isAuthenticated();
      allow update: if isAuthenticated();
      
      // No deletes for MVP
      allow delete: if false;
    }
    
    // Contractors collection - read-only for all authenticated users
    match /contractors/{contractorId} {
      // All authenticated users can read contractors
      allow read: if isAuthenticated();
      
      // No writes (managed by admin only, or through app logic)
      allow write: if false;
    }
    
    // Deny all other collections
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

## How to Apply These Rules

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select project: `home-mvp-f0aa5`
3. Navigate to: **Firestore Database** → **Rules** tab
4. Copy the rules above
5. Paste into the rules editor
6. Click **Publish**

## What These Rules Do

- ✅ **Requires Authentication**: All operations require a logged-in user
- ✅ **User Data Protection**: Users can only modify their own user document
- ✅ **Shared Tickets**: All authenticated users can read tickets (as needed for landlords/contractors)
- ✅ **Job Access**: All authenticated users can read jobs
- ✅ **Contractor List**: Read-only access to contractor data
- ✅ **No Deletes**: Prevents accidental data loss in MVP

## Testing the Rules

After publishing:
1. Try accessing Firestore from your app
2. Check Firebase Console → Firestore Database → Usage tab for any denied requests
3. Verify authenticated users can read/write as expected

## Important Notes

- These rules require **authentication** for all operations
- If you need unauthenticated access for testing, temporarily use test mode (NOT recommended for production)
- Always test rules after publishing

