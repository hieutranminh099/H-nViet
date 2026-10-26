# Tích Hợp Capacitor & GitHub Actions Tự Động Build File `.APK` (CI/CD Pipeline)

Giải pháp trọn gói giúp đẩy mã nguồn ứng dụng **HánViệt Studio (FlashHUD & AI Video Translator)** lên GitHub và tự động biên dịch ra file cài đặt Android (`.apk`) thông qua **GitHub Actions** (hỗ trợ đồng thời cả dự án **Capacitor Web-to-Android** lẫn **Native Android Gradle**), kèm trung tâm sao chép cấu hình và hướng dẫn trực quan ngay trong tab **"Mã nguồn"**.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> Các quyết định cấu hình dưới đây đã được xác nhận theo lựa chọn của bạn ở bước làm rõ yêu cầu:

- **Confirmed Decision 1 — Quy trình GitHub Actions Đa Chế Độ (Dual-Mode Capacitor + Native Android Gradle)**: File `.github/workflows/android-build.yml` sẽ tự động cài đặt Node.js, Java/Android SDK, kiểm tra và chạy lệnh build Web + đồng bộ Capacitor (`npx cap add android` / `npx cap sync`) khi phát hiện nhánh Web/Capacitor, đồng thời tự động giải mã `debug.keystore` và chạy Gradle để biên dịch trực tiếp dự án Android Native hiện tại.
- **Confirmed Decision 2 — Xuất đồng thời cả `app-debug.apk` và `app-release.apk` lên GitHub Artifacts**: Quy trình CI/CD tự động tạo khóa ký dự phòng nếu chưa cấu hình GitHub Secrets, biên dịch cả bản **Debug APK** (cài đặt ngay trên mọi điện thoại Android không cần cấu hình phức tạp) và **Release APK**, sau đó tải lên **GitHub Artifacts** để tải về chỉ với 1 cú nhấp chuột.
- **Confirmed Decision 3 — Lưu trực tiếp vào thư mục gốc Repository & Tích hợp vào tab "Mã nguồn"**: Toàn bộ các tệp cấu hình (`capacitor.config.json`, `capacitor.config.ts`, `package.json`, `.github/workflows/android-build.yml`, `GITHUB_APK_GUIDE.md`) vừa được tạo sẵn ở thư mục gốc của dự án để sẵn sàng đẩy lên GitHub, vừa hiển thị trực tiếp trong tab **"Mã nguồn"** kèm nút **Sao chép 1 chạm** và **Sơ đồ vị trí nút tải APK trong tab Actions**.

---

## 1. Overview & Core Concept

- **What It Does**:
  1. Cung cấp đầy đủ bộ tệp cấu hình **Capacitor 6** (`capacitor.config.json`, `capacitor.config.ts`, `package.json` chứa `@capacitor/core`, `@capacitor/cli`, `@capacitor/android`, `@capacitor/filesystem`, `@capacitor/share`, `@capacitor/preferences`).
  2. Thiết lập quy trình tự động hóa **GitHub Actions** (`.github/workflows/android-build.yml`) kích hoạt mỗi khi `git push` lên nhánh `main`/`master` hoặc bấm nút chạy thủ công (`workflow_dispatch`).
  3. Bổ sung giao diện **"Hướng dẫn Đẩy GitHub & Tải File APK từ GitHub Actions"** ngay trong tab **Mã nguồn** của ứng dụng để người dùng có thể tra cứu các lệnh Git và vị trí nút tải file `.apk` bất cứ lúc nào.
- **Target Audience / Persona**: Người dùng và nhà phát triển muốn đóng gói ứng dụng thành file `.apk` cài đặt lên điện thoại thật hoàn toàn miễn phí trên máy chủ đám mây của GitHub mà không cần tự cài đặt Android Studio nặng nề trên máy tính cá nhân.
- **Key Value**: Chỉ cần đẩy code lên GitHub, hệ thống tự động cài môi trường, đồng bộ Capacitor & Gradle, đóng gói file `.apk` và xuất đường dẫn tải về tại mục **Artifacts**.

---

## 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Truy cập Tab "Mã nguồn" trên thanh điều hướng dưới cùng**:
     - Người dùng thấy ngay thẻ **"GitHub Actions & Capacitor APK Builder"** ở đầu màn hình với hướng dẫn 3 bước ngắn gọn: *(1) Đẩy code lên GitHub ➔ (2) Mở tab Actions chờ tích xanh ➔ (3) Cuộn xuống mục Artifacts bấm tải `HanViet-Studio-Debug-APK`*.
  2. **Chọn và Sao chép từng file cấu hình**:
     - Các chip chọn file gồm: `.github/workflows/android-build.yml`, `capacitor.config.json`, `capacitor.config.ts`, `package.json`, `GITHUB_APK_GUIDE.md` cùng các mã nguồn hiện có.
     - Nút **"Sao chép"** và **"Chia sẻ"** giúp xuất nhanh từng đoạn mã hoặc sao chép trọn bộ câu lệnh `git init / git push`.
- **Visual Identity & Theme**:
  - Giữ vững phong cách **Minimalist Modern Dark Mode (Linear / Vercel / iOS Dark)**:
    - Nền chính `--background`: `Slate-950` (`#020617`)
    - Thẻ nổi `--card`: `Slate-900` (`#0F172A`) với đường viền mảnh `1.dp` `Slate-800` (`#1E293B`)
    - Điểm nhấn hành động chính `--primary`: `PrimaryBlue` (`#3B82F6`) và `SoftIndigo` (`#818CF8`)
  - Phông chữ **Be Vietnam Pro** rõ nét cho phần hướng dẫn từng bước và khối mã nguồn trình bày gọn gàng, có thanh cuộn ngang chống tràn màn hình nhỏ.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Quy trình GitHub Actions Tự Động Hóa Kép (Capacitor Sync + Native Android Gradle)**
  - *Chosen Approach*: Trong `.github/workflows/android-build.yml`, thiết lập đầy đủ các bước:
    1. `actions/checkout@v4`
    2. `actions/setup-node@v4` (Node.js 20) & `actions/setup-java@v4` (JDK 17 Temurin) + `android-actions/setup-android@v3`
    3. Cài đặt dependencies & chạy build Web (`npm install && npm run build`), đồng bộ sang Android bằng Capacitor (`npx cap add android || true` và `npx cap sync android`) nếu dùng cấu trúc Web, hoặc biên dịch trực tiếp thư mục `app/` hiện có.
    4. Tự động khôi phục `debug.keystore` từ `debug.keystore.base64` (hoặc tạo bằng `keytool`) và tạo keystore dự phòng cho bản Release để Gradle không bao giờ báo lỗi thiếu khóa ký trên máy chủ GitHub.
    5. Thực thi `./gradlew assembleDebug assembleRelease` (hoặc `gradle assembleDebug assembleRelease`) và dùng `actions/upload-artifact@v4` đẩy cả `HanViet-Studio-Debug-APK` và `HanViet-Studio-Release-APK` lên giao diện GitHub.
  - *Why*: Đảm bảo dù người dùng đẩy kho mã nguồn hiện tại hay kết hợp thêm giao diện Web/Capacitor, GitHub Actions đều chạy thành công 100% và sinh ra file `.apk` tải được ngay.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & CI/CD Pipeline Diagram

```
┌───────────────────────────────────────────────────────────────────────────┐
│                    MÁY TÍNH CÁ NHÂN / AI STUDIO EXPORT                    │
│  git init ➔ git add . ➔ git commit ➔ git push -u origin main              │
└─────────────────────────────────────┬─────────────────────────────────────┘
                                      │
                                      ▼
┌───────────────────────────────────────────────────────────────────────────┐
│              GITHUB ACTIONS RUNNER (ubuntu-latest Cloud VM)               │
│  ┌─────────────────────────────────────────────────────────────────────┐  │
│  │ Bước 1: Setup Node.js 20 + Java JDK 17 (Temurin) + Android SDK      │  │
│  └──────────────────────────────────┬──────────────────────────────────┘  │
│                                     ▼                                     │
│  ┌─────────────────────────────────────────────────────────────────────┐  │
│  │ Bước 2: Capacitor Pipeline                                          │  │
│  │  • npm install && npm run build                                     │  │
│  │  • npx cap add android (nếu chưa có) & npx cap sync android         │  │
│  └──────────────────────────────────┬──────────────────────────────────┘  │
│                                     ▼                                     │
│  ┌─────────────────────────────────────────────────────────────────────┐  │
│  │ Bước 3: Khôi phục Keystore & Biên dịch Gradle                       │  │
│  │  • Giải mã debug.keystore & khởi tạo release keystore dự phòng      │  │
│  │  • Chạy gradle assembleDebug & gradle assembleRelease               │  │
│  └──────────────────────────────────┬──────────────────────────────────┘  │
│                                     ▼                                     │
│  ┌─────────────────────────────────────────────────────────────────────┐  │
│  │ Bước 4: Upload GitHub Artifacts (actions/upload-artifact@v4)        │  │
│  │  • 📦 HanViet-Studio-Debug-APK (app-debug.apk — Cài đặt ngay)       │  │
│  │  • 📦 HanViet-Studio-Release-APK (app-release.apk)                  │  │
│  └─────────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────────────────┘
```

### Interactive Component & State Mapping

- **Kho Mã Nguồn (`NativeCodeCatalog`)**:
  - Bổ sung 4 mục tài liệu & cấu hình mới ở đầu danh sách:
    1. `.github/workflows/android-build.yml` (GitHub Actions Workflow hoàn chỉnh).
    2. `capacitor.config.json` & `capacitor.config.ts` (Cấu hình Capacitor chuẩn với `appId: "com.aistudio.hanvietflashhud.kxmpzq"` và `appName: "HánViệt FlashHUD"`).
    3. `package.json` (Khai báo đầy đủ `@capacitor/core`, `@capacitor/cli`, `@capacitor/android`, scripts `build`, `cap:init`, `cap:sync`, `apk:debug`).
    4. `GITHUB_APK_GUIDE.md` (Hướng dẫn từng bước đẩy code lên GitHub và sơ đồ vị trí nút tải file `.apk` trong tab **Actions -> Artifacts**).
- **Màn hình Xuất Mã Nguồn (`NativeCodeExporterScreen`)**:
  - Thêm thẻ trực quan **"Hướng dẫn nhanh: Đẩy GitHub & Tải APK (3 Bước)"** kèm nút sao chép nhanh cụm lệnh `git push` để người dùng thực hiện ngay lập tức.
