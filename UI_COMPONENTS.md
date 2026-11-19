# Thành Phần Giao Diện & Hệ Thống Thiết Kế (UI_COMPONENTS.md)
## Princess Puzzle for Girls and Toddlers (Design System & UI Components)

---

### 1. Bảng Màu Truyện Cổ Tích (Storybook Color Palette)

Ứng dụng sử dụng bảng màu pastel ấm áp, bão hòa dịu nhẹ, tạo cảm giác thân thiện, an toàn và lôi cuốn cho các bé gái và trẻ mầm non:

| Tên Màu | Mã Hex | Giá Trị RGB | Ứng Dụng Thực Tế Trong Giao Diện |
|:---|:---:|:---:|:---|
| **Princess Rose** | `#F48FB1` | `(244, 143, 177)` | Màu chủ đạo của vương miện, nút Play, viền huy hiệu nổi bật |
| **Deep Rose** | `#D81B60` | `(216, 27, 96)` | Bóng đổ nút bấm 3D, viền điểm nhấn, chữ tiêu đề chính |
| **Sky Lavender** | `#CE93D8` | `(206, 147, 216)` | Màu nền bầu trời cổ tích, ruy băng Magic Pack |
| **Buttercup Gold**| `#FFD54F` | `(255, 213, 79)` | Ngôi sao chiến thắng, vương miện hoàng gia, viền khung tranh |
| **Warm Gold** | `#FFA000` | `(255, 160, 0)` | Lõi sao vàng, tia hào quang nam châm hút mảnh ghép |
| **Meadow Mint** | `#81C784` | `(129, 199, 132)` | Đồi cỏ nền, nút Next Level chiến thắng, huy hiệu FREE |
| **Forest Sage** | `#388E3C` | `(56, 142, 60)` | Bóng đổ nút xanh lá, lá cây hoàng gia |
| **Pastel Cream** | `#FFF9C4` | `(255, 249, 196)` | Nền khay chứa mảnh ghép, thẻ thông tin phụ huynh |
| **Cloud White** | `#FFFFFF` | `(255, 255, 255)` | Thân mảnh ghép, đám mây bay nền, viền hào quang phát sáng |
| **Soft Charcoal**| `#37474F` | `(55, 71, 79)` | Đường viền nét vẽ minh họa (stroke), chữ dễ đọc chống mỏi mắt |

---

### 2. Thành Phần Nút Bấm Công Thái Học Trẻ Nhỏ (Toddler Controls)

#### 2.1 Nút bấm 3D dạng ngọc (Storybook 3D Chunky Buttons)
- **Kích thước tối thiểu**:
  - Nút Play chính: Chiều cao `88dp`, chiều rộng tối thiểu `220dp`.
  - Nút Next Level / Play Again: Chiều cao `72dp`.
  - Nút điều hướng góc (Back, Grown-Ups, Hint): Bán kính tối thiểu `56dp – 64dp`.
- **Cấu trúc hiệu ứng 3D**:
  - Lớp bề mặt (Face): Màu tươi sáng với gradient chéo nhẹ từ trên xuống.
  - Lớp gờ bóng đổ (Bottom Bevel): Dày `6dp` với tông màu đậm hơn ở đáy, tạo cảm giác nút thật có thể ấn lún xuống.
  - Phản hồi khi ấn (Press Feedback): Khi ngón tay chạm vào, bề mặt dịch chuyển xuống `4dp` và gờ bóng thu nhỏ còn `2dp`, kết hợp âm thanh click nhẹ vui tai.

#### 2.2 Nút Gợi Ý Đũa Thần (Magic Wand Hint Button)
- Đặt ở góc phải thanh công cụ.
- Khi trẻ không thao tác trong 5 giây hoặc khi bấm vào đũa thần:
  - Một mảnh ghép chưa ghép trong khay sẽ nhấp nháy phát sáng (shimmer pulse).
  - Vị trí ô tương ứng trên khung tranh cũng đồng thời phát hào quang vàng kim chỉ điểm.

#### 2.3 Nút Góc Phụ Huynh (Grown-Ups Lock Button)
- Thiết kế hình chiếc khiên hoặc ổ khóa bạc trang nhã.
- Đặt ở vị trí góc trên xa tầm bấm thông thường của trẻ nhỏ để tránh chạm nhầm.

---

### 3. Bộ Giải Thuật Bố Cục Thích Ứng (Responsive PuzzleLayout Solver)

Ứng dụng hỗ trợ liền mạch cả 2 hướng màn hình: **Portrait (Dọc)** trên điện thoại và **Landscape (Ngang)** trên máy tính bảng hoặc khi xoay màn hình:

#### 3.1 Bố cục Dọc (Portrait Layout)
- **Phía trên (Top ~60% màn hình)**: Khung tranh chính (`PuzzleBoard`), tỷ lệ khung hình vuông 1:1, căn giữa với lề an toàn hai bên `16dp`.
- **Phía dưới (Bottom ~40% màn hình)**: Khay chứa mảnh ghép (`PuzzleTray`), phân bố các mảnh ghép rải đều hoặc theo 2 hàng linh hoạt.

#### 3.2 Bố cục Ngang (Landscape / Tablet Layout)
- **Bên trái (Left ~60% màn hình)**: Khung tranh chính 1:1 căn giữa theo trục dọc.
- **Bên phải (Right ~40% màn hình)**: Khay chứa mảnh ghép dạng cột dọc, tận dụng tối đa chiều rộng của máy tính bảng.

---

### 4. Các Màn Hình Lớp Phủ Đặc Biệt (Modal Overlays)

#### 4.1 Cổng Phụ Huynh (`ParentalGateOverlay`)
- Nền làm mờ (Backdrop Blur / Semi-transparent Scrim `#99000000`).
- Khung hộp thoại bo tròn `28dp`, màu kem dịu mắt.
- **Thành phần tương tác**:
  - Nhãn hướng dẫn rõ ràng: *"Grown-Ups Only: Press and hold for 3 seconds or solve the math problem"*.
  - Nút tròn giữ 3 giây với vòng tròn tiến trình (CircularProgressIndicator) tăng dần theo thời gian thực.
  - Câu đố toán học nhẩm (ví dụ: `8 × 3 = 24`) với 4 nút lựa chọn.
  - Nút dấu X đóng hộp thoại ở góc trên.

#### 4.2 Cài Đặt Dành Cho Người Lớn (`GrownUpsSettingsOverlay`)
- Thanh tiêu đề trang nhã với biểu tượng phụ huynh.
- Danh mục tùy chọn:
  1. Mục mua hàng: Nút mở khóa gói Magic Pack giá \$2.99 / Nút khôi phục bản quyền.
  2. Mục âm thanh: Thanh trượt / Công tắc bật tắt Sound FX và Nhạc nền cổ tích.
  3. Mục tiến trình: Nút xóa dữ liệu đặt lại sao (có hộp thoại xác nhận tránh bấm nhầm).
  4. Nút thoát ứng dụng sạch sẽ.

#### 4.3 Màn Hình Hoàn Thành Chiến Thắng (`CelebrationOverlay`)
- Lớp hạt pháo hoa ngũ sắc lấp lánh phủ toàn màn hình.
- Khung chúc mừng bo cong với dải cờ ruy băng bay phấp phới.
- Cụm 3 ngôi sao vàng nở rộ (Spring Scale Pop) với hiệu ứng ánh sao lấp lánh.
- Cụm 3 nút hành động to bản: Chơi lại (Play Again) - Màn tiếp theo (Next Level) - Chọn màn (Level Select).
