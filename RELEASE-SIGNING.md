# Release signing policy

این فایل فقط اطلاعات **عمومی و غیرمحرمانه** گواهی انتشار مجموعه را ثبت می‌کند. فایل keystore و رمزها نباید در این مخزن عمومی قرار بگیرند.

## قانون ثابت انتشار

- تمام APKهای نهایی App-Collection باید با یک کلید Release ثابت امضا شوند.
- Buildهای `debug` فقط برای توسعه و QA هستند و خروجی انتشار محسوب نمی‌شوند.
- نسخه جدید هر برنامه باید همان `applicationId` قبلی، `versionCode` بالاتر و همین کلید Release را حفظ کند تا Android آن را به عنوان Update نسخه نصب‌شده بپذیرد.
- از keystore و اطلاعات بازیابی حداقل دو نسخه پشتیبان آفلاین و امن نگهداری شود.
- keystore، store password و key password هرگز commit نشوند.

## گواهی عمومی Release فعلی

- Alias: `app-collection-release`
- Subject: `CN=AS Team App Collection, OU=Android, O=AS Team, L=Berlin, C=DE`
- Key: RSA 4096-bit
- Certificate SHA-256: `0674c2b5faec172ed07b0749d477116cf4301265e465af6bb48f9f634926b547`
- Certificate SHA-1: `fc5b8dcdff245d7bc921723a077db3743d3dfdcd`

این fingerprintها برای کنترل اینکه نسخه‌های بعدی با همان گواهی صحیح امضا شده‌اند استفاده می‌شوند.

## CI

Workflow `.github/workflows/build-apks.yml` دو نوع خروجی می‌سازد:

1. Debug test APKs برای تست توسعه.
2. Unsigned Release APKs برای مرحله خصوصی امضا.

کلید Release عمداً داخل GitHub Actions عمومی ذخیره نشده است. امضای نهایی در محیط خصوصی انجام می‌شود و سپس خروجی با `apksigner verify` کنترل می‌شود.
