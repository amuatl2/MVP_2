# Team Setup Guide (For Firebase)

## For Team Members: Getting Started

### What You Need

1. ✅ **Firebase Project Access** (you'll be added as a team member)
2. ✅ **Download `google-services.json`** (one-time setup)
3. ✅ **Clone the repository** (standard git workflow)

### Step 1: Accept invitation to project (sent to your GT email)

### Step 2: Download google-services.json

**After you have Firebase access:**

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select project: `home-mvp-f0aa5`
3. Click gear icon ⚙️ → **Project settings**
4. Scroll to **Your apps** section
5. Find the Android app (package: `com.example.mvp`)
6. Click **Download google-services.json**
7. Place the file in: `app/google-services.json` (same level as `app/build.gradle.kts`)

**Important:** 
- This file is **NOT** committed to git (it's in `.gitignore`)
- Each team member downloads their own copy
- The file is the same for everyone (same project, same app)

### Step 3: Clone and Set Up Repository (Skip if already done so)


### Step 4: Verify Setup

1. ✅ `app/google-services.json` exists locally
2. ✅ Gradle syncs without errors
3. ✅ App builds successfully
4. ✅ App can connect to Firebase (test login/create account)


## Troubleshooting

### "FirebaseApp not initialized"
- Make sure `google-services.json` is in `app/` directory
- Clean and rebuild project
- Check that Google Services plugin is applied in `build.gradle.kts`

### "Permission denied" in Firebase Console
- Ask project owner to add you as a team member
- Check that you accepted the email invitation

### "Cannot download google-services.json"
- Make sure you have Editor or Owner role
- Check that you're viewing the correct project

### "Authentication failed"
- Check that Email/Password authentication is enabled in Firebase Console
- Verify Firestore Security Rules are set up

## Important Notes

- ⚠️ **Never commit `google-services.json`** - it's in `.gitignore`
- ✅ **Always use `google-services.json.example`** as a reference
- 🔒 **Don't share your `google-services.json`** - each person downloads their own
- 📝 **The file is identical for everyone** - same project, same app

## Need Help?

1. Check `FIREBASE_SETUP.md` for detailed setup instructions
2. Check `FIRESTORE_SECURITY_RULES.md` for security rules
3. Ask the project owner (Nicolas) for Firebase access

