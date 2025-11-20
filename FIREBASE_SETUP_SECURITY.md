# Firebase Security - Important!

## ⚠️ Security Alert

The `google-services.json` file contains sensitive API keys and should **NEVER** be committed to a public repository.

## What to Do Now

### 1. **Revoke the Exposed API Key** (URGENT)
   - Go to [Google Cloud Console](https://console.cloud.google.com/)
   - Navigate to: **APIs & Services** → **Credentials**
   - Find the API key: `AIzaSyBXs2oHf8kuwUWdJmzOCwKPyYZN6WMQtOg`
   - **Delete or restrict** this key immediately
   - Create a new API key with proper restrictions

### 2. **Set Up Firebase Security Rules**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Select your project: `home-mvp-f0aa5`
   - Go to **Firestore Database** → **Rules**
   - Ensure proper authentication rules are set
   - Go to **Storage** → **Rules** (if using Storage)
   - Set up proper access controls

### 3. **For Team Members**
   - Each team member should download their own `google-services.json` from Firebase Console
   - Place it in `app/google-services.json` (this file is now in `.gitignore`)
   - **Never commit this file**

### 4. **For CI/CD (if applicable)**
   - Store `google-services.json` as a secret in your CI/CD platform
   - Use environment variables or secure file injection during build

## File Structure

- ✅ `app/google-services.json.example` - Template (safe to commit)
- ❌ `app/google-services.json` - Actual config (in `.gitignore`, never commit)

## How to Get Your Own google-services.json

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Click the gear icon ⚙️ → **Project settings**
4. Scroll to **Your apps** section
5. Click on the Android app (or add one if needed)
6. Download `google-services.json`
7. Place it in `app/google-services.json`

## API Key Restrictions (Recommended)

When creating a new API key, restrict it to:
- **Application restrictions**: Android apps
- **Package name**: `com.example.mvp`
- **API restrictions**: Only Firebase services you use

