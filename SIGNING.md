# Signing و Update امن APK

این مخزن عمومی است؛ بنابراین Signing Key خصوصی، رمزها و فایل‌های Recovery نباید داخل GitHub Commit شوند.

## قانون ثابت Update

Android فقط زمانی APK جدید را به‌عنوان Update نسخه نصب‌شده می‌پذیرد که حداقل این شرایط برقرار باشند:

1. `applicationId` همان برنامه بدون تغییر باقی بماند.
2. APK جدید با همان Release Signing Key قبلی امضا شود.
3. `versionCode` نسخه جدید از نسخه نصب‌شده بزرگ‌تر باشد.

در این پروژه `applicationId` هر برنامه از `com.asteam.appcollection.p01` تا `com.asteam.appcollection.p78` ثابت نگه داشته می‌شود.

## Versioning در CI

Workflow اصلی Build مقدار واقعی `versionCode` و `versionName` را با Gradle property تزریق می‌کند. سپس با `apkanalyzer manifest` دوباره `applicationId`، `versionCode` و `versionName` را مستقیماً از Manifest داخل خود APK می‌خواند. به این ترتیب فقط نام Artifact ملاک نیست و خطای نسخه‌گذاری قبل از تحویل شناسایی می‌شود.

از `aapt dump badging` برای این کنترل استفاده نمی‌شود، چون در Android 16 / API 36 ممکن است هنگام resolve کردن بعضی resourceهای framework مانند drawableهای سیستمی خطای کاذب بدهد، درحالی‌که خود APK و Manifest معتبر هستند.

## Signing Key

Releaseهای عمومی باید همیشه با Signing Key پایدار پروژه امضا شوند. نسخه Recovery این کلید باید فقط در محل خصوصی امن نگهداری شود. فایل‌های `*.jks`، `*.keystore` و سایر فرمت‌های کلید در `.gitignore` مسدود شده‌اند.

اثر انگشت Certificate فعلی پروژه برای تطبیق Releaseها:

`SHA-256: 06:74:C2:B5:FA:EC:17:2E:D0:7B:07:49:D4:77:11:6C:F4:30:12:65:E4:65:AF:6B:B4:8F:9F:63:49:26:B5:47`

Alias ثبت‌شده برای Recovery خصوصی: `app-collection-release`.

رمز Key/Keystore عمداً در این مخزن نوشته نشده است.

## ترتیب صحیح Release Signing

1. `assembleRelease` نسخه unsigned را تولید می‌کند.
2. قبل از Signing، `apkanalyzer manifest` مقدار واقعی package/version را کنترل می‌کند.
3. `zipalign` روی APK اجرا می‌شود.
4. `apksigner sign` با Signing Key پایدار اجرا می‌شود.
5. `apksigner verify --verbose --print-certs` امضا و Certificate را کنترل می‌کند.
6. SHA-256 فایل نهایی ثبت می‌شود.
7. Certificate SHA-256 با Fingerprint ثبت‌شده همین سند تطبیق داده می‌شود.

نمونه دستورات با placeholder:

```bash
apkanalyzer manifest application-id app-release-unsigned.apk
apkanalyzer manifest version-code app-release-unsigned.apk
apkanalyzer manifest version-name app-release-unsigned.apk
zipalign -f -p 4 app-release-unsigned.apk app-release-aligned.apk
apksigner sign --ks /PRIVATE/PATH/release-key.jks --ks-key-alias app-collection-release --out app-release.apk app-release-aligned.apk
apksigner verify --verbose --print-certs app-release.apk
sha256sum app-release.apk
```

اطلاعات محرمانه مانند Password نباید در Command History، Log عمومی CI یا فایل Commit‌شده قرار گیرد.

## Debug APK

Debug APK فقط برای QA و توسعه است. Debug key جایگزین Release key نیست و APK Debug نباید به‌عنوان نسخه Publish/Production توزیع شود.
