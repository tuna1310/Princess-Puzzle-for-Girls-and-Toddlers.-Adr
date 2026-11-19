# Phân Tích Ứng Dụng Tham Chiếu (APP_ANALYSIS.md)
## Princess Puzzle for Girls and Toddlers (iOS Reference Reverse Engineering)

---

### 1. Thông Tin Tổng Quan Ứng Dụng Tham Chiếu
- **Tên ứng dụng gốc**: Princess Puzzle for girls and toddlers
- **Nhà phát triển**: Tommy Hass
- **ID App Store**: `id682756571`
- **Link App Store**: [https://apps.apple.com/us/app/princess-puzzle-for-girls-and-toddlers/id682756571?l=vi](https://apps.apple.com/us/app/princess-puzzle-for-girls-and-toddlers/id682756571?l=vi)
- **Phiên bản ghi nhận**: 2.1 (Cập nhật 64-bit architecture, tối ưu iOS 8.0+ đến iPadOS / macOS M1)
- **Dung lượng gốc**: ~23.4 MB
- **Độ tuổi mục tiêu**: 1 – 6 tuổi (Dành cho trẻ mới biết đi và bé gái mầm non)
- **Mô hình kinh doanh**: Freemium (Miễn phí 4 màn đầu tiên, Mua gói mở khóa toàn bộ 16 màn "Unlock all puzzles" giá $2.99)
- **Đánh giá App Store**: 5.0 / 5 sao

---

### 2. Thu Thập Dữ Liệu & Phân Tích Hình Ảnh (Screenshots & Artwork)
Dữ liệu trích xuất từ Apple iTunes Metadata API (`https://itunes.apple.com/lookup?id=682756571`):
1. **Icon 512x512**: Vương miện công chúa đính ngọc hồng trên nền pastel tím mộng mơ viền hoa.
2. **iPhone Screenshots (750x1333 / 392x696)**:
   - *Screen 1*: Màn hình gameplay ghép tranh công chúa tóc vàng đầm hồng, khay chứa mảnh ghép bo góc, viền ghost slot mờ giúp bé định hướng.
   - *Screen 2*: Màn chơi xe ngựa hoàng gia bí ngô mạ vàng với các mảnh ghép interlocking zic-zac Bezier 4 mảnh.
   - *Screen 3*: Màn chơi kỳ lân Unicorn sừng ngũ sắc trong rừng hoa tuyết lấp lánh (6 mảnh ghép).
   - *Screen 4*: Màn chơi lâu đài cổ tích kẹo ngọt (Castle Blossom) với khay mảnh ghép bố trí trực quan.
   - *Screen 5*: Màn hình hoàn thành (Celebration): Pháo hoa/bóng bay/ngôi sao bay lượn ngập tràn, nút Next to bản cho ngón tay trẻ nhỏ.
3. **iPad Screenshots (1024x768 / 576x768)**:
   - Trực quan hóa layout ngang (Landscape): Khay mảnh ghép đặt gọn gàng bên phải hoặc phía dưới, khung ghép tranh chính chiếm 65-70% diện tích màn hình, tỷ lệ khung hình 4:3 và 16:9 thích ứng mượt mà.

---

### 3. Đánh Giá Trải Nghiệm Người Dùng (UX & Toddler Psychology)
1. **Triết lý Zero-Frustration (Không áp lực)**:
   - Trẻ 1-6 tuổi chưa có phản xạ chịu đựng thất bại cao. Khi kéo mảnh ghép thả sai vị trí, ứng dụng gốc không bao giờ phát tiếng còi báo lỗi (buzzer), không hiển thị dấu X đỏ trừng phạt, không đếm ngược thời gian (no timer), và không có điểm số âm.
   - Thay vào đó, mảnh ghép tự động nảy (drop-and-bounce return physics) nhẹ nhàng quay về khay ban đầu với âm thanh bong bóng nước vui nhộn.
2. **Công thái học ngón tay trẻ nhỏ (Toddler Ergonomics)**:
   - Vùng chạm (hitbox) phải tối thiểu 64dp – 80dp, lớn hơn hẳn kích thước thực của chi tiết đồ họa.
   - Cơ chế Touch-Lift Offset: Khi ngón tay trẻ ấn xuống kéo mảnh ghép, tâm của mảnh ghép tự động nâng lên trên vị trí tiếp xúc ngón tay một khoảng ~24-32dp. Điều này đảm bảo ngón tay bụ bẫm của bé không che mất góc nhìn của mảnh ghép khi đang tìm vị trí lắp.
   - Snap-to-Position (Hút nam châm): Khi đưa mảnh ghép lại gần vị trí đúng trong bán kính dung sai (~40-52dp), mảnh ghép tự động hút vào slot với hiệu ứng hào quang lấp lánh (magnetic glow) và âm thanh "ding" thánh thót.
3. **Bảo vệ trẻ em & Kiểm soát phụ huynh (Kid-Safe & Parental Gate)**:
   - Trẻ không được tự ý truy cập cài đặt hệ thống hoặc mua hàng IAP.
   - Ứng dụng gốc trang bị cổng phụ huynh (Parental Gate): Yêu cầu giữ tay 3 giây hoặc làm phép tính nhẩm đơn giản của người lớn trước khi vào cài đặt mở khóa.
   - Tuyệt đối không chứa quảng cáo bên thứ ba (Zero Ads), không theo dõi định danh quảng cáo (No Advertising ID), hoàn toàn ngoại tuyến và bảo mật.

---

### 4. Bảng Phân Tích 16 Màn Chơi (Progression System)

| STT | Tên Màn Chơi | Chủ Đề Hình Ảnh | Số Mảnh Ghép | Cấu Trúc Lưới | Trạng Thái Mặc Định |
|:---:|:---|:---|:---:|:---:|:---:|
| 1 | Starlight Tiara | Vương miện sao pha lê & đũa phép | 2 mảnh | 1x2 Ngang | Miễn phí (Free) |
| 2 | Clover Bunny Princess | Công chúa thỏ ngọc & đồng cỏ hoa sen | 3 mảnh | 1x3 Dọc | Miễn phí (Free) |
| 3 | Cloudberry Unicorn | Kỳ lân mây hồng & cầu vồng pastel | 4 mảnh | 2x2 Lưới | Miễn phí (Free) |
| 4 | Cupcake Castle | Lâu đài kẹo ngọt kem dâu | 4 mảnh | 2x2 Lưới | Miễn phí (Free) |
| 5 | Swan Lake Princess | Thiên nga trắng & vương miện hồ ngọc | 5 mảnh | Lưới tự do | Magic Pack (Khóa) |
| 6 | Pumpkin Carriage | Cỗ xe bí ngô hoàng gia dát vàng | 6 mảnh | 2x3 Lưới | Magic Pack (Khóa) |
| 7 | Willow Tree Swing | Xích đu hoàng gia dưới tán liễu biếc | 6 mảnh | 2x3 Lưới | Magic Pack (Khóa) |
| 8 | Rosebud Tea Party | Tiệc trà dã ngoại hoàng gia | 7 mảnh | Lưới đa hình | Magic Pack (Khóa) |
| 9 | Firefly Balcony | Ban công ngắm đom đóm trong đêm hội | 8 mảnh | 2x4 Lưới | Magic Pack (Khóa) |
| 10 | Golden Harp Pavilion | Nhạc đình đàn hạc vàng trong cung điện | 8 mảnh | 2x4 Lưới | Magic Pack (Khóa) |
| 11 | Lilypad Fountain | Đài phun nước hoa súng và cá koi ngọc | 9 mảnh | 3x3 Lưới | Magic Pack (Khóa) |
| 12 | Midnight Comet Gazing | Tháp thiên văn ngắm sao băng lấp lánh | 9 mảnh | 3x3 Lưới | Magic Pack (Khóa) |
| 13 | Floating Lanterns River | Lễ hội hoa đăng bến sông hoàng gia | 10 mảnh | 2x5 Lưới | Magic Pack (Khóa) |
| 14 | Carousel Fairground | Đu quay ngựa gỗ hoàng gia | 10 mảnh | 2x5 Lưới | Magic Pack (Khóa) |
| 15 | Crystal Ballroom Waltz | Dạ vũ pha lê đại sảnh cung điện | 12 mảnh | 3x4 Lưới | Magic Pack (Khóa) |
| 16 | Coronation Garden | Lễ đăng quang rực rỡ ngập hoa hồng | 12 mảnh | 3x4 Lưới | Magic Pack (Khóa) |

---

### 5. Kết Luận Nghiên Cứu & Chiến Lược Triển Khai Android Native
1. **Tuân thủ 100% Android Native (Kotlin + Jetpack Compose)**:
   - Dùng Jetpack Compose kết hợp Canvas/Path API để vẽ hình ảnh minh họa vector và cắt ghép puzzle động mượt mà 60 FPS.
   - Không sử dụng bất kỳ thư viện lai nào (không Flutter, React Native, WebView).
2. **Âm thanh Native sống động**:
   - Tích hợp động cơ tổng hợp âm thanh procedural sound synthesis hoặc Android SoundPool/MediaPlayer để tạo ra âm thanh chạm, nhấc, thả, hút nam châm và nhạc nền dịu êm cho trẻ mà không làm tăng dung lượng APK cồng kềnh.
3. **Lưu trữ chuẩn Jetpack DataStore**:
   - Lưu tiến trình sao đạt được (1-3 sao), trạng thái mở khóa của 16 màn chơi, cài đặt âm thanh Sound/Music một cách bền bỉ và khôi phục tức thì khi xoay màn hình hoặc khởi động lại ứng dụng.
