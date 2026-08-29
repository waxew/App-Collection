# App-Collection

`App-Collection` بازسازی و مدرن‌سازی کامل ۷۸ فایل قدیمی پوشه `Android Source` به ۷۸ برنامه Android مستقل با Kotlin است. هیچ ورودی اولیه حذف نشده است؛ هر منبع قدیمی یک ماژول شماره‌دار مستقل دارد و ترتیب پروژه‌ها بر اساس ارزش کاربردی/آموزشی ثبت شده است.

## وضعیت مجموعه

- **۷۸** application module مستقل.
- **۲۸** مورد با ارزش بیشتر برای تبدیل به ابزار کاربردی.
- **۲۸** نمونه آموزشی مفید Kotlin/Android.
- **۲۲** نمونه تکراری یا منسوخ که حذف نشده‌اند و نسخه مدرن مستقل دریافت کرده‌اند.
- یک ماژول مشترک `shared-ui` برای جلوگیری از تکرار کد UI/Navigation.
- یک پروژه مستقل `test-host` برای QA هر ۷۸ نمونه داخل یک APK؛ این برنامه جایگزین ۷۸ APK اصلی نیست.

فهرست کامل: [CATALOG.md](CATALOG.md)  
پیشنهاد نسخه‌های بعدی: [ROADMAP.md](ROADMAP.md)

## Toolchain ثابت

- Android Gradle Plugin: `9.3.0`
- Gradle Wrapper: `9.5.0`
- JDK: `17`
- compileSdk: `36`
- targetSdk: `36`
- minSdk: `23`
- Kotlin: Built-in Kotlin support در AGP 9.x

## Build محلی

بعد از Clone نیازی نیست Gradle را جداگانه برای این پروژه تنظیم کنید؛ Gradle Wrapper رسمی داخل مخزن نگهداری می‌شود.

Windows:

```bat
gradlew.bat assembleDebug
```

Linux / macOS:

```bash
./gradlew assembleDebug
```

برای Build یک ماژول مشخص، برای مثال GPS:

```bash
./gradlew :p01:assembleDebug
```

## ساختار UI مشترک

هر ۷۸ برنامه از `shared-ui` استفاده می‌کنند. پوسته مشترک شامل موارد زیر است:

- Hamburger در بالا-راست و Drawer از سمت راست.
- Profile در بالای Drawer با تصویر دایره‌ای محلی و نام کاربر.
- لمس تصویر Profile برای انتخاب یا حذف تصویر.
- Settings به‌صورت صفحه مستقل همراه بخش Notifications.
- Share با Android Sharesheet.
- About us به‌صورت صفحه مستقل.
- Contact us به‌صورت صفحه مستقل با `as.team.support@gmail.com`.
- About software به‌صورت صفحه مستقل که فقط توضیح کاربرپسند و Version را نشان می‌دهد؛ package/applicationId/source filename نمایش داده نمی‌شود.
- Back ابتدا Drawer را می‌بندد و سپس از Back Stack عادی Android استفاده می‌کند.

## Version و Update

هر applicationId ثابت است: `com.asteam.appcollection.p01` تا `com.asteam.appcollection.p78`.

CI هنگام Release مقدار جدید `versionCode` و `versionName` را به Build تزریق می‌کند و سپس با `apkanalyzer manifest` مقدار واقعی `applicationId`، `versionCode` و `versionName` را مستقیماً از Manifest داخل هر APK دوباره کنترل می‌کند. این روش از تولید APK با نام جدید ولی metadata قدیمی جلوگیری می‌کند و برخلاف `aapt dump badging` به resolve شدن drawableهای framework وابسته نیست.

قواعد کامل Signing/Update: [SIGNING.md](SIGNING.md)

## CI و QA

سه سطح اصلی کنترل وجود دارد:

1. `.github/workflows/build-apks.yml`  
   همه ۷۸ Debug و unsigned Release APK را Build می‌کند، تعداد خروجی‌ها را کنترل می‌کند، Debug signature را Verify می‌کند و package/version metadata واقعی همه APKها را با `apkanalyzer` می‌خواند.

2. `.github/workflows/build-test-host.yml`  
   APK میزبان QA را می‌سازد، بررسی می‌کند هر ۷۸ `MainActivity` واقعاً داخل DEX نهایی وجود داشته باشند و package/version metadata خود Host را نیز مستقیم از Manifest کنترل می‌کند.

3. `.github/workflows/emulator-smoke-test.yml`  
   Instrumentation test را روی Android Emulator اجرا می‌کند و هر ۷۸ Activity را یک بار Launch/Close می‌کند تا Crashهای زمان `onCreate` یا اولین lifecycle فقط پشت Build موفق پنهان نمانند.

## Signing

APK Debug فقط برای QA است. خروجی Release عمومی باید با Signing Key پایدار پروژه امضا شود. Private key و Password عمداً در این مخزن عمومی وجود ندارند و الگوهای Signing key در `.gitignore` مسدود شده‌اند.

برای فرآیند کامل Release: [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md)

## قانون دائمی Comment

هر سورس یا فایل متنی جدید و هر تغییر جدید باید همراه توضیحات قابل فهم باشد. Commentهای قدیمی نیز باید هنگام تغییر رفتار کد به‌روز شوند.

استاندارد کامل: [COMMENTING_STANDARD.md](COMMENTING_STANDARD.md)

## Generator اولیه

پوشه `bootstrap/` فقط برای سابقه مهاجرت اولیه نگهداری می‌شود. Generator قدیمی نباید روی سورس فعلی اجرا شود؛ جزئیات در `bootstrap/README.md` ثبت شده است.
