# Releasing Kairo to Google Play

Kairo is signed with an **upload key** that only you hold. Google Play re-signs the app with its own
app signing key (Play App Signing), so if the upload key is ever lost, Google can reset it.

> Never commit the key or its passwords. `*.jks`, `*.keystore` and `keystore.properties` are git-ignored.

## 1. Create the upload key (once, on your own computer)

```bash
keytool -genkeypair -v -keystore kairo-upload.jks -alias kairo-upload -keyalg RSA -keysize 2048 -validity 10000
```

Store the `.jks` file and both passwords in a password manager. Keep a backup somewhere other than this computer.

## 2. Add the key to GitHub (once)

In the repository, open **Settings → Secrets and variables → Actions** and add:

| Secret | Value |
|---|---|
| `KAIRO_KEYSTORE_BASE64` | The key file as base64: `base64 -i kairo-upload.jks` (macOS) or `base64 -w0 kairo-upload.jks` (Linux) |
| `KAIRO_KEYSTORE_PASSWORD` | The keystore password |
| `KAIRO_KEY_ALIAS` | `kairo-upload` |
| `KAIRO_KEY_PASSWORD` | The key password |

## 3. Build a release

1. Bump `versionCode` (must always increase) and `versionName` in `app/build.gradle.kts`, through a pull request.
2. After it merges, tag the release: `git tag v1.0.0 && git push origin v1.0.0`.
   Or run **Actions → Release → Run workflow**.
3. Download the **kairo-release** artifact from the workflow run. It contains:
   - `app-release.aab`: upload this to Play Console
   - `app-release.apk`: for installing on a phone directly
   - `mapping.txt`: upload to Play Console so crash reports are readable

### Building locally instead

Create a git-ignored `keystore.properties` in the project root:

```properties
storeFile=/absolute/path/to/kairo-upload.jks
storePassword=...
keyAlias=kairo-upload
keyPassword=...
```

Then run `./gradlew bundleRelease`.

## 4. First upload to Play Console

1. Create the app: name **Kairo**, default language, *App*, *Free*.
2. Upload `app-release.aab` to an **internal testing** track first and install it from Play on your own phone.
3. Complete the listing and policy forms using [docs/play-store-listing.md](docs/play-store-listing.md).
4. Publish the [privacy policy](docs/privacy-policy.md) at a public URL (see that file) and add the URL in Play Console.
5. When internal testing looks good, promote the release to **closed testing**. New personal developer
   accounts must run a closed test with testers for a period before production access is granted;
   check Play Console for the current requirement.
