# چک‌لیست Release نهایی

این چک‌لیست برای هر Release جدید `App-Collection` باید از بالا به پایین اجرا شود.

## 1. Source

- دقیقاً ۷۸ پوشه شماره‌دار application وجود داشته باشد.
- هر ماژول `MainActivity.kt` و Manifest معتبر داشته باشد.
- `shared-ui` در همه ماژول‌ها استفاده شود.
- Commentهای سورس و مستندات مرتبط با آخرین تغییر هماهنگ باشند.
- TODO/FIXME ضروری برای Release باقی نمانده باشد.
- هیچ Signing Key یا Secret در Git ثبت نشده باشد.

## 2. Navigation و UI مشترک

- Hamburger در بالا-راست باز شود.
- Drawer از سمت راست نمایش داده شود.
- Profile image قابل انتخاب/حذف باشد و انتخاب محلی حفظ شود.
- نام کاربر در Profile نمایش داده شود.
- Settings صفحه مستقل داشته باشد و بخش Notifications وجود داشته باشد.
- Share از Android Sharesheet استفاده کند.
- About us، Contact us و About software صفحه مستقل داشته باشند.
- About software فقط توضیح کاربرپسند و Version را نشان دهد و package/internal identifier نمایش ندهد.
- Back ابتدا Drawer را ببندد و سپس به صفحه قبلی برگردد.

## 3. Build

- JDK 17.
- Gradle Wrapper 9.5.0.
- Android Gradle Plugin 9.3.0.
- compileSdk/targetSdk = 36.
- minSdk = 23.
- Build هر ۷۸ Debug APK موفق باشد.
- Build هر ۷۸ unsigned Release APK موفق باشد.
- Build میزبان QA ۷۸در۱ موفق باشد.

## 4. Version و Update

- `applicationId` هیچ ماژول منتشرشده‌ای تغییر نکرده باشد.
- versionCode از Release قبلی بزرگ‌تر باشد.
- versionName مطابق Release موردنظر باشد.
- `apkanalyzer manifest application-id` package واقعی تمام APKها را تأیید کند.
- `apkanalyzer manifest version-code` و `version-name` مقدار واقعی Version داخل تمام APKها را تأیید کنند.
- برای metadata verification از `aapt dump badging` استفاده نشود؛ روی API 36 ممکن است resolve شدن resourceهای framework خطای کاذب ایجاد کند.

## 5. QA خودکار

- DEX میزبان QA شامل تمام `p01..p78 MainActivity` باشد.
- Instrumentation smoke test هر ۷۸ Activity را روی Emulator اجرا کند.
- خطای Runtime/Crash در شروع Activity وجود نداشته باشد.
- گزارش تست به Artifact CI اضافه شود.

## 6. Signing

- فقط Release APKها با Signing Key پایدار پروژه امضا شوند.
- `zipalign` قبل از Signing اجرا شود.
- `apksigner verify` روی هر APK موفق باشد.
- SHA-256 Certificate با Fingerprint ثبت‌شده در `SIGNING.md` یکسان باشد.
- Key/Password هرگز در GitHub عمومی قرار نگیرد.

## 7. تحویل

بسته کامل تحویل شامل موارد زیر باشد:

- ZIP سورس کامل همان Commit نهایی.
- ۷۸ APK Release امضاشده.
- ۷۸ APK Debug برای QA.
- APK میزبان تست ۷۸در۱.
- فایل SHA-256 همه خروجی‌های اصلی.
- گزارش Signature/Version/Package verification.
- `info.txt` با Commit، Version، Toolchain، Package range و وضعیت تست‌ها.
- Signing Recovery به‌صورت فایل خصوصی جداگانه؛ نه داخل GitHub عمومی.

## 8. بعد از Release

- برای نسخه بعدی applicationId و Signing Key ثابت بمانند.
- versionCode افزایش پیدا کند.
- تغییرات جدید همراه Comment و Documentation اضافه شوند.
- همان Build/Smoke-test/Signing/Verification دوباره اجرا شود.
