# Contributing to PDFly

Thank you for your interest in contributing to **PDFly — PDF Editor & Toolkit** (`https://github.com/CodeByArnob/PDFly`)!

PDFly is built around a strict commitment to **user privacy**, **100% offline execution**, and **zero feature paywalls**.

---

## 1. Non-Negotiable Product Principles

Before submitting a pull request, please ensure your changes respect PDFly's core guarantees:
- **100% Free & Open Source**: Never introduce paywalls, locked features, trial limits, subscriptions, or advertising SDKs.
- **100% Local On-Device Execution**: Do not add `android.permission.INTERNET` or external network calls to the Android app. All PDF processing must happen on-device.
- **Least-Privilege Permissions**: Use Android Storage Access Framework (SAF) and Photo Picker for file operations. Never add broad storage permissions (`READ_EXTERNAL_STORAGE`, `MANAGE_EXTERNAL_STORAGE`).
- **Accessible & High-Contrast UI**: Maintain WCAG 2.2 AA+ contrast across both the Editorial Light Theme and Obsidian Dark Theme, and respect `prefers-reduced-motion` / Android Animator Duration Scale.

---

## 2. Repository Structure

- `/app` — Native Android application (Kotlin, Jetpack Compose, Material 3, Room, DataStore, CameraX, `PdfRenderer` / `IsoPdf14Codec`).
- `/website` — Official static marketing & documentation website (React 18, TypeScript, Vite, Tailwind CSS, Framer Motion).
- `/docs/screenshots` — Vector SVG app screen previews used in documentation.
- `/.github/workflows` — Separate GitHub Actions pipelines for Android CI (`android-ci.yml`) and Website GitHub Pages deployment (`deploy-website.yml`).

---

## 3. Local Development & Verification

### Android Application (`/app`)
1. Open the root project in **Android Studio Ladybug (or newer)** with **JDK 17**.
2. Run the unit and Robolectric test suites:
   ```bash
   ./gradlew :app:testDebugUnitTest
   ```
3. Assemble the debug APK:
   ```bash
   ./gradlew :app:assembleDebug
   ```

### Official Website (`/website`)
1. Navigate to `/website` and install dependencies:
   ```bash
   cd website
   npm install
   ```
2. Start the local development server:
   ```bash
   npm run dev
   ```
3. Verify TypeScript compilation and production static output:
   ```bash
   npm run build
   ```

---

## 4. Pull Request Checklist

- [ ] Code compiles cleanly without warnings or errors.
- [ ] All existing Robolectric and unit tests pass (`./gradlew :app:testDebugUnitTest`).
- [ ] If modifying `/website`, `npm run build` succeeds inside `/website`.
- [ ] No API keys, keystores, `.env` files, or personal tokens are included in the commit.
