# Kế Hoạch Đại Tu Toàn Diện UI/UX: Minimalist, Smart Solid Masking & One-Hand Friendly

## 1. Xử Lý Triệt Để Lỗi Chữ Đè Nhau (Smart Solid & Backdrop Masking — Mặc Định 90%)
- **Nguyên tắc hiển thị bắt buộc (Zero Text Overlap):**
  - Trên cả **Video AI Studio (Live Preview)** và **Live HUD (Màn hình Game/App)**, tuyệt đối không vẽ thêm một lớp chữ tiếng Trung rời rạc nằm lộ liễu phía dưới hoặc chồng chéo với chữ tiếng Việt.
  - Khi chế độ dịch hoạt động, hệ thống tự động tạo một **Hộp Che Nền Đặc (Smart Solid Mask Box)** hoặc **Dải Kính Mờ (Backdrop Blur Pill)** bao phủ khít toàn bộ vùng phụ đề gốc (`Bounding Box` / `Hardsub ROI`) với độ che phủ mặc định **90% (`maskOpacity = 0.90f`)**, che khuất hoàn toàn chữ tiếng Trung bên dưới trước khi in chữ tiếng Việt sắc nét lên trên.
  - Chữ tiếng Trung gốc chỉ hiển thị khi người dùng chủ động nhấn giữ nút **"Nhìn bản gốc (Hold-to-Peek)"** hoặc kéo thanh trượt độ mờ nền về `0%`.
- **Thanh trượt "Độ mờ nền chữ (Background Opacity)":**
  - Đặt ngay trong ngăn kéo điều khiển dưới đáy với giá trị mặc định **90%** (`0% ➔ 100%`), cập nhật tức thì độ đục của lớp nền che chữ gốc trên cả Video Preview, Live HUD và mã xuất `.ASS` (`BackColour` alpha).

---

## 2. Phong Cách Thiết Kế Tối Giản (Linear / Vercel / iOS Modern Dark Mode)
- **Bảng màu Slate & Blue-500 / Indigo Pastel (`Color.kt` & `Theme.kt`):**
  - Loại bỏ hoàn toàn viền xanh lá neon (`#10B981`) kiểu Terminal/Hacker cũ.
  - **Nền chính (`Slate950`):** `#020617` / `#090D16` trầm dịu mắt.
  - **Bề mặt thẻ (`Slate900` & `Slate800`):** `#0F172A` với viền mảnh tinh tế `#1E293B` (`1.dp`).
  - **Nút hành động chính (`Primary Blue-500` & `Soft Indigo`):** `#3B82F6` (Blue-500) và `#6366F1` (Indigo-500) kết hợp chữ trắng tinh khiết (`#F8FAFC`).
  - **Chữ mô tả phụ (`Slate400`):** `#94A3B8` dễ đọc, độ tương phản chuẩn WCAG AA.
- **Typography chuẩn mực (`Type.kt`):**
  - Loại bỏ hoàn toàn kiểu viết **CHỮ IN HOA TOÀN BỘ** ở các tiêu đề dài và đoạn mô tả.
  - Sử dụng chữ viết hoa chữ cái đầu tự nhiên (Sentence case / Title case), kết hợp font không chân hiện đại (`Be Vietnam Pro` & `Space Grotesk`) với phân cấp kích thước rõ ràng.
- **Phân cấp nút bấm & biểu tượng trực quan:**
  - Nút quan trọng nhất (**"Tải video lên"**, **"Dịch AI ngay"**, **"Xuất video"**) dùng nút đặc màu `Blue-500` nổi bật kèm icon trực quan.
  - Các tùy chọn phụ (`Hardsub OCR`, `Audio ASR`, `Kép OCR + ASR`) gom thành các **Chip nhỏ gọn** có thể cuộn ngang mượt mà.
- **Thanh điều hướng dưới đáy (`NavigationBar`):**
  - Mở mặc định vào phân hệ **Video AI** khi khởi động ứng dụng.
  - Căn chỉnh 5 mục ngắn gọn, không bị cắt chữ: **`Video AI`**, **`Live HUD`**, **`Hiệu năng`**, **`Ngữ cảnh`**, **`Mã nguồn`** kèm khoảng đệm `WindowInsets.navigationBars` để không dính sát mép dưới điện thoại.

---

## 3. Tính Năng "❓ Hướng Dẫn" (Interactive 3-Step Onboarding Modal)
- **Nút `"❓ Hướng dẫn"` trên thanh TopAppBar:**
  - Nằm ở góc phải trên cùng với thiết kế Pill tinh tế, dễ bấm (`>= 48dp`).
- **Tự động hiển thị khi mới vào ứng dụng & khi bấm nút:**
  - Hiển thị một **Modal (Bảng nổi bo góc `24.dp`)** trực quan gồm 3 bước kèm biểu tượng minh họa sắc nét:
    1. **Bước 1 — Tải video cần dịch:** Chọn file video từ điện thoại hoặc dùng ngay bộ 4 tập phim ngắn mẫu có sẵn.
    2. **Bước 2 — Bấm Dịch AI (Tự động che chữ gốc):** Chọn quét phụ đề hình ảnh (`OCR`) hoặc âm thanh (`ASR`). AI sẽ tự động phủ lớp nền đặc 90% che kín chữ tiếng Trung và thay bằng tiếng Việt.
    3. **Bước 3 — Kéo thả & Xuất file / Lưu Cloud:** Dùng một ngón tay kéo phụ đề đến vị trí ưng ý, chỉnh độ mờ nền chữ và bấm **Xuất Lossless** hoặc **Lưu Cloud** để xem trên mọi thiết bị.
  - Có nút **"Bắt đầu trải nghiệm ngay"** (màu `Blue-500`) và tùy chọn chuyển nhanh giữa các bước.

---

## 4. Bố Cục "One-Hand Friendly" Với Ngăn Kéo Dưới Đáy (Bottom Sheet)
- **Thanh chuyển Tab con vuốt ngang mượt mà (Swipeable Pill Tabs):**
  - Thay thế các chip dài bị cắt chữ bằng dải **Segmented Pill Tabs** gọn gàng: `Xem & Sửa` • `Ghép Lô AI` • `Xuất Video` • `Kho Cloud` • `Tài Khoản AI`.
- **Cố định vùng xem Video / Live HUD ở nửa trên màn hình:**
  - Khung phát Video (và màn hình Live HUD) luôn nằm gọn ở nửa trên màn hình với thanh điều khiển phát/tua tối giản.
- **Ngăn kéo điều khiển dưới đáy (Collapsible Tabbed Bottom Sheet):**
  - Đặt ở nửa dưới màn hình trong tầm với của ngón tay cái, có nút **Thu gọn / Mở rộng** và 3 thẻ chức năng rõ ràng:
    1. **`Dịch & Tập phim`:** Nút **"Tải video lên"**, **"Dịch AI ngay"**, chọn tập phim và chế độ quét (`OCR` / `ASR`).
    2. **`Kiểu chữ & Nền che (90%)`:** Thanh trượt **"Độ mờ nền chữ (Background Opacity)"** (mặc định `90%`), thanh trượt cỡ chữ, chế độ nền che (`Nền đặc Solid Box` / `Làm mờ Blur`) và chọn màu chữ.
    3. **`Lời thoại (Timeline)`:** Danh sách câu thoại theo mốc thời gian mili-giây để chạm nhảy khung hình hoặc sửa nhanh từ ngữ.
