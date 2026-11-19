# Luồng Màn Hình & Trải Nghiệm (SCREEN_FLOW.md)
## Princess Puzzle for Girls and Toddlers (Screen Flow & Navigation)

```mermaid
flowchart TD
    Launch([Khởi động Ứng dụng]) --> Splash[Android Core SplashScreen]
    Splash --> Home[Màn Hình Chính - HomeScreen]
    
    Home -->|Bấm Nút Play To| LevelSelect[Danh Sách Màn Chơi - LevelSelectScreen]
    Home -->|Góc Phụ Huynh - Grown-Ups| Gate1[Cổng Phụ Huynh - ParentalGateOverlay]
    
    LevelSelect -->|Chọn Màn 1-4 (Miễn Phí)| Gameplay[Màn Chơi Ghép Tranh - PuzzleScreen]
    LevelSelect -->|Chọn Màn 5-16 (Đã Mở Khóa)| Gameplay
    LevelSelect -->|Chọn Màn 5-16 (Đang Khóa)| LockedNotice[Thông Báo Magic Pack & Gợi Ý Mở Khóa]
    LevelSelect -->|Góc Phụ Huynh| Gate1
    LevelSelect -->|Nút Quay Lại| Home
    
    LockedNotice -->|Bấm Mở Khóa| Gate1
    LockedNotice -->|Đóng| LevelSelect
    
    Gate1 -->|Vượt Qua: Giữ 3s / Tính Nhẩm| Settings[Cài Đặt Dành Cho Người Lớn - GrownUpsSettingsOverlay]
    Gate1 -->|Hủy / Sai Kết Quả| CancelGate[Đóng Cổng - Quay Lại Màn Trước]
    
    Settings -->|Mở Khóa Toàn Bộ 16 Màn| UnlockAction[Kích Hoạt Magic Pack - Lưu DataStore]
    Settings -->|Bật/Tắt Âm Thanh & Nhạc| AudioToggle[Cập Nhật Cài Đặt Audio]
    Settings -->|Khôi Phục Tiến Trình| ResetAction[Xóa Sao & Đặt Lại Trạng Thái]
    Settings -->|Thoát Cài Đặt| LevelSelect
    
    subgraph GameplayLoop [Vòng Lặp Chơi Game]
        Gameplay -->|Kéo Thả Mảnh Ghép| InGameAction[Cảm Ứng & Vật Lý: Nhấc Mảnh, Hút Nam Châm, Nảy Về Khay]
        InGameAction -->|Ghép Đúng Mảnh Cuối| Celebration[Hiệu Ứng Chiến Thắng - CelebrationOverlay]
        Celebration -->|Bấm Next Level| NextPuzzle{Còn Màn Tiếp Theo?}
        NextPuzzle -->|Màn Tiếp Mở Khóa| Gameplay
        NextPuzzle -->|Màn Tiếp Đang Khóa| LockedNotice
        Celebration -->|Chơi Lại| Gameplay
        Celebration -->|Chọn Màn Khác| LevelSelect
        Gameplay -->|Nút Quay Lại TopBar| LevelSelect
    end
```

---

### Chi Tiết Từng Màn Hình & Thành Phần Trực Quan

#### 1. Màn Hình Khởi Động (Splash & Launcher)
- **Nhiệm vụ**: Khởi tạo mượt mà không có màn hình trắng nhấp nháy, hỗ trợ Android 12+ SplashScreen API.
- **Thiết lập hệ thống**:
  - `WindowCompat.setDecorFitsSystemWindows(window, false)` (Toàn màn hình Edge-to-Edge).
  - Immersive sticky bars: Ẩn thanh điều hướng và thanh trạng thái hệ thống để tránh trẻ vô tình vuốt thoát ứng dụng.
  - `FLAG_KEEP_SCREEN_ON`: Giữ màn hình luôn sáng trong suốt buổi chơi, tránh gián đoạn khi trẻ suy nghĩ.

#### 2. Màn Hình Chính (Home Screen)
- **Header**: Huy hiệu vương miện hoàng gia vàng pastel, viền dải ruy băng lụa mềm mại.
- **Bánh xe truyện cổ tích (Preview Carousel)**: Đĩa xoay nhẹ nhàng hiển thị hình ảnh đại diện các màn chơi với vòng quay mượt mà.
- **Huy hiệu ngôi sao (Star Summary Pill)**: Hiển thị tổng số sao bé đã tích lũy (ví dụ: `★ 12 / 48`).
- **Nút Chơi Lớn (Hero Play Button)**: Nút dạng viên ngọc nổi 3D bo tròn, kích thước tối thiểu `88dp`, viền bóng mềm, màu hồng đào rực rỡ kèm biểu tượng mũi tên tam giác vui nhộn.
- **Góc Phụ Huynh (Grown-Ups Button)**: Đặt kín đáo ở góc trên với biểu tượng ổ khóa an toàn, ngăn bé ấn nhầm.

#### 3. Màn Hình Chọn Màn Chơi (Level Select Screen)
- **Lưới hiển thị (Responsive Grid)**:
  - Tự động chia 2 cột trên điện thoại (Portrait), 4 cột trên máy tính bảng hoặc chế độ ngang (Landscape).
  - Thumbnail ảnh vector sắc nét 200px được lưu vào bộ nhớ cache, viền khung vàng hoàng gia.
- **Hệ thống đánh giá**: Hiển thị 3 ngôi sao dưới mỗi thẻ màn chơi (Sao rỗng / Sao vàng lấp lánh).
- **Huy hiệu trạng thái**:
  - Màn 1 đến 4: Huy hiệu "FREE" màu xanh ngọc tươi sáng.
  - Màn 5 đến 16: Huy hiệu ổ khóa bạc / ruy băng tím "MAGIC PACK" khi chưa mua; chuyển thành huy hiệu sao khi đã mở khóa.

#### 4. Màn Hình Gameplay Ghép Tranh (Puzzle Screen)
- **Top HUD**:
  - Nút Back hình mũi tên tròn to bản bên trái.
  - Thẻ tên màn chơi trang nhã ở giữa.
  - Huy hiệu số lượng mảnh ghép (ví dụ: `4 pcs`, `9 pcs`).
  - Nút Gợi ý (Hint Button) hình cây đũa thần lấp lánh.
- **Khu vực tranh ghép (Puzzle Frame)**:
  - Tỷ lệ khung hình chuẩn 1:1, nằm cân đối ở nửa trên màn hình dọc hoặc nửa trái màn hình ngang.
  - Lớp nền mờ (Ghost Slot): Đường viền mờ của các mảnh ghép giúp bé mường tượng vị trí.
- **Khay chứa mảnh ghép (Puzzle Tray)**:
  - Nền pastel bo góc trang nhã với bóng đổ nhẹ.
  - Các mảnh ghép được xáo trộn ngẫu nhiên với góc xoay nhẹ tự nhiên.

#### 5. Màn Hình Phần Thưởng & Chiến Thắng (Celebration Overlay)
- **Hiệu ứng tức thì**:
  - Bắn pháo hoa giấy ngũ sắc (`ConfettiParticles`) với vận tốc và góc xoay ngẫu nhiên rơi khắp màn hình.
  - Hoạt ảnh sống động tương ứng với chủ đề từng màn (ví dụ: cỗ xe bí ngô lăn bánh, đom đóm phát sáng, làn hơi tách trà bốc khói).
  - Chuỗi âm thanh kèn trumpet hoàng gia (Fanfare) và tiếng vỗ tay chúc mừng.
- **3 Ngôi sao chiến thắng**: Bay từ giữa màn hình bung sáng rực rỡ với hiệu ứng phóng to nảy (scale pop animation).
- **Cụm nút điều hướng sau chiến thắng**:
  - Nút "Next Level" to nhất ở giữa màu xanh lá ngọc bảo, mời gọi bé tiếp tục hành trình cổ tích.
  - Nút "Play Again" bên trái giúp bé xếp lại nếu yêu thích tranh.
  - Nút "Level Select" bên phải quay về danh sách.

#### 6. Cổng Phụ Huynh & Cài Đặt (Parental Gate & Grown-Ups Menu)
- **Cơ chế bảo vệ kép**:
  - Chế độ 1: Giữ nút tròn trong đủ 3 giây liên tục (với vòng tròn tiến trình chạy quanh nút).
  - Chế độ 2: Bài toán nhân nhẩm 1 chữ số (ví dụ: `6 × 7 = ?`) với 4 lựa chọn đáp án.
- **Bảng điều khiển Grown-Ups**:
  - Nút "Unlock All 16 Puzzles" (Mở khóa vĩnh viễn toàn bộ nội dung).
  - Nút "Restore Purchase" (Khôi phục bản quyền mua hàng).
  - Công tắc Bật/Tắt Âm thanh tương tác (Sound FX).
  - Công tắc Bật/Tắt Nhạc nền cổ tích (Music Melody).
  - Nút "Reset Stars & Progress" (Đặt lại sao cho bé chơi lại từ đầu, có xác nhận 2 bước).
  - Nút "Exit App" (Thoát ứng dụng an toàn).
