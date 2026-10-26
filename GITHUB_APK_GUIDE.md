# Hướng Dẫn Đẩy Mã Nguồn Lên GitHub & Tải File `.APK` Tự Động Bằng GitHub Actions

Tài liệu này hướng dẫn từng bước cách đẩy mã nguồn **HánViệt Studio** lên GitHub và tải file cài đặt `.apk` được biên dịch tự động trên đám mây.

---

## Phần 1: Các File Cấu Hình Đã Được Tạo Sẵn Trong Dự Án

1. `capacitor.config.json` & `capacitor.config.ts`: Cấu hình định danh ứng dụng (`com.aistudio.hanvietflashhud.kxmpzq`), tên hiển thị (`HánViệt FlashHUD`) và thư mục web build (`dist`).
2. `package.json`: Đã tích hợp sẵn `@capacitor/core`, `@capacitor/cli`, `@capacitor/android` cùng các lệnh `npm run build` và `npm run cap:sync`.
3. `.github/workflows/android-build.yml`: Quy trình CI/CD tự động cài Node.js 20, Java JDK 17, Android SDK, chạy `npx cap add android` / `npx cap sync`, biên dịch Gradle và tải file `.apk` lên **GitHub Artifacts**.

---

## Phần 2: Cách Đẩy Dự Án Lên GitHub (3 Bước Ngắn Gọn)

### Cách A: Đẩy trực tiếp từ AI Studio (Nhanh nhất)
1. Nhấn vào biểu tượng **GitHub / Export** ở thanh công cụ phía trên của Google AI Studio.
2. Đăng nhập tài khoản GitHub của bạn, đặt tên Repository (ví dụ: `hanviet-studio-apk`) và nhấn **Push to GitHub**.

### Cách B: Đẩy bằng dòng lệnh Git trên máy tính (Sau khi tải file ZIP về)
Mở **Terminal** (hoặc **Git Bash / PowerShell**) tại thư mục giải nén dự án và chạy 5 lệnh sau:

```bash
git init
git add .
git commit -m "Initialize HanViet Studio with Capacitor & GitHub Actions APK workflow"
git branch -M main
git remote add origin https://github.com/<TEN_TAI_KHOAN_CUA_BAN>/<TEN_REPO_CUA_BAN>.git
git push -u origin main
```

---

## Phần 3: Vị Trí Nút Tải File `.APK` Trong Tab "Actions" Trên GitHub

Ngay sau khi đẩy code lên GitHub, quá trình build `.apk` sẽ tự động chạy (mất khoảng **3 – 5 phút**). Bạn lấy file `.apk` theo đúng sơ đồ sau:

```
┌─────────────────────────────────────────────────────────────────────────┐
│  TRANG GITHUB REPOSITORY CỦA BẠN                                        │
│  [ <> Code ]   [ Issues ]   [ Pull requests ]   👉 [ ▶️ Actions ]       │
└─────────────────────────────────────────────────────────┬───────────────┘
                                                          │
   (1) Bấm vào tab "Actions" trên thanh menu ngang        ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  All workflows                                                          │
│  🟢 Build Android APK (Capacitor + Native Gradle)  ← (2) Bấm vào dòng   │
│     Commit #1: Initialize HanViet Studio...              build có tích  │
│                                                          xanh 🟢        │
└─────────────────────────────────────────────────────────┬───────────────┘
                                                          │
   (3) Cuộn xuống cuối trang "Summary" tới mục Artifacts  ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  Artifacts (Ở góc dưới cùng của trang kết quả chạy Workflow)            │
│  ┌───────────────────────────────────────────────────┬───────────────┐  │
│  │ 📦 HanViet-Studio-Debug-APK                       │  ⬇️ [Tải về]  │  │
│  │    (Khuyên dùng: Giải nén .zip lấy app-debug.apk  │               │  │
│  │     cài đặt trực tiếp lên mọi điện thoại Android) │               │  │
│  ├───────────────────────────────────────────────────┼───────────────┤  │
│  │ 📦 HanViet-Studio-Release-APK                     │  ⬇️ [Tải về]  │  │
│  │    (Bản Release đã ký sẵn: app-release.apk)       │               │  │
│  └───────────────────────────────────────────────────┴───────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
```

### Lưu ý khi cài đặt trên điện thoại:
- File tải về từ mục **Artifacts** của GitHub sẽ có định dạng `.zip` (ví dụ `HanViet-Studio-Debug-APK.zip`).
- Bạn chỉ cần **giải nén file `.zip` đó** sẽ thấy ngay file **`app-debug.apk`** bên trong, gửi qua Zalo / Google Drive / cáp USB vào điện thoại và nhấn cài đặt.
