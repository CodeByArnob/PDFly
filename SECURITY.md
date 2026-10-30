# Security Policy

## Supported Versions

**PDFly** is an actively maintained, offline-first open-source Android application. Security updates are applied to the latest `main` branch and published via GitHub Releases.

| Version | Supported |
| :--- | :--- |
| `1.0.x` (`main`) | :white_check_mark: Supported |
| `< 1.0.0` | :x: Unsupported |

---

## Core Security & Privacy Architecture

1. **Zero Network Attack Surface**:
   - PDFly does **not** declare `android.permission.INTERNET` in `app/src/main/AndroidManifest.xml`.
   - Zero PDF documents, annotations, signatures, or telemetry ever leave the user's device.
2. **Least-Privilege Storage Access**:
   - PDFly uses Android's **Storage Access Framework (`ActivityResultContracts.OpenDocument` / `CreateDocument`)** and the **Android Photo Picker (`PickMultipleVisualMedia`)**.
   - It never requests `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, or `MANAGE_EXTERNAL_STORAGE`.
3. **Path Traversal & File Sanitization**:
   - All imported filenames and exported document paths are sanitized in `LocalFileManager.sanitizeFileName()` and verified against canonical app storage directories to prevent path-traversal (`../`) vulnerabilities.
4. **Memory-Safe Bitmap & PDF Stream Decoding**:
   - Large images and PDF pages are decoded using sampled `BitmapFactory.Options` (`inSampleSize`) and recycled immediately after rendering to prevent Out-Of-Memory (`OutOfMemoryError`) denial-of-service crashes.

---

## Reporting a Vulnerability

If you discover a security or privacy vulnerability in PDFly:

1. **Do not open a public GitHub issue** if the vulnerability could put end users at risk before a patch is available.
2. Report the vulnerability privately via [GitHub Security Advisories](https://github.com/CodeByArnob/PDFly/security/advisories/new) on the `CodeByArnob/PDFly` repository.
3. Include steps to reproduce, affected Android API levels, and a minimal proof-of-concept PDF or image file if applicable.
