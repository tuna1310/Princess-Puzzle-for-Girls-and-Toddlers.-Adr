# Đặc Tả Tính Năng Chi Tiết (FEATURE_SPECIFICATION.md)
## Princess Puzzle for Girls and Toddlers (Comprehensive Feature Specification)

---

### 1. Phân Hệ Động Cơ Ghép Tranh (Puzzle Engine)

#### 1.1 Thuật toán cắt mảnh ghép Bezier (JigsawSlicer)
- **Cơ chế cắt tự động**:
  - Nhận vào kích thước khung tranh (`width`, `height`) và cấu hình hàng/cột (`rows`, `cols`).
  - Hỗ trợ cắt từ 2 đến 12 mảnh ghép với đường nối zic-zac Bezier cong mượt mà (interlocking tabs & blanks).
  - Mỗi cạnh trong (internal edge) được tạo ngẫu nhiên nhưng có tính tất định theo hạt giống (seed) của màn chơi, đảm bảo mảnh ghép khớp hoàn hảo không kẽ hở (watertight geometry).
- **Cấu trúc dữ liệu mảnh ghép (`PuzzlePiece`)**:
  - `id: Int`: Định danh duy nhất của mảnh trong màn chơi.
  - `row: Int, col: Int`: Tọa độ vị trí chuẩn trong lưới tranh.
  - `outlinePath: Path`: Đường viền hình học Bezier để vẽ và clipping.
  - `slotBounds: Rect`: Tọa độ đích chính xác trên khung tranh (`targetPosition`).
  - `currentPosition: Offset`: Tọa độ hiện tại trên màn hình.
  - `homeTrayPosition: Offset`: Tọa độ vị trí nghỉ trong khay chứa.
  - `state: PieceState`: Trạng thái (`IN_TRAY`, `DRAGGING`, `SNAPPED`, `RETURNING_BOUNCE`).

#### 1.2 Nhận diện vị trí và hút nam châm (Snap Detection)
- **Bán kính hút (`SNAP_TOLERANCE_DP = 48dp`)**:
  - Khi tâm mảnh ghép cách tâm vị trí chuẩn (`slotBounds.center`) nhỏ hơn hoặc bằng bán kính dung sai, hệ thống tự động kích hoạt trạng thái hút (`Snap`).
- **Phản hồi xúc giác & thị giác khi hút đúng**:
  - Phát ánh hào quang vàng kim lấp lánh (magnetic halo glow) quanh vị trí ghép.
  - Tự động căn chỉnh tọa độ mảnh ghép khớp chính xác vào `slotBounds` với hoạt ảnh nảy nhẹ (elastic snap settle).
  - Mảnh ghép sau khi hút được khóa trạng thái (`isLocked = true`), không thể kéo lại được nữa để tránh trẻ làm xáo trộn mảnh đã hoàn thành.

---

### 2. Phân Hệ Vật Lý & Tương Tác Cảm Ứng (Touch & Physics Engine)

#### 2.1 Công thái học cảm ứng cho ngón tay trẻ (Touch-Lift Offset)
- **Độ dịch tâm ngón tay (`TOUCH_LIFT_OFFSET_Y = -32dp`)**:
  - Khi ngón tay ấn xuống mảnh ghép, tâm hiển thị của mảnh ghép được nâng lên trên điểm tiếp xúc của ngón tay 32dp.
  - Trẻ có thể quan sát rõ ràng góc cạnh của mảnh ghép mà không bị ngón tay che khuất tầm nhìn.

#### 2.2 Động học nảy về khay khi thả sai (Drop-and-Bounce Return)
- **Mô phỏng lò xo giảm chấn (Damped Spring Physics)**:
  - Khi trẻ thả tay ra ngoài vùng dung sai hút, mảnh ghép không dịch chuyển đột ngột mà kích hoạt chuyển động nảy vật lý quay về tọa độ `homeTrayPosition`.
  - Công thức lực kéo hồi tiếp:
    $$F = -k \cdot (x - x_{home}) - c \cdot v$$
    với hệ số đàn hồi $k = 0.18f$ và hệ số ma sát giảm xóc $c = 0.75f$.
  - Mảnh ghép nảy 2-3 nhịp nhẹ nhàng trước khi dừng hẳn, kèm âm thanh nảy nước vui nhộn.

#### 2.3 Bảo vệ chống kẹt mảnh ghép (Anti-Stuck Guarantee)
- **Giới hạn khung nhìn (Viewport Clamping)**:
  - Tọa độ mảnh ghép được kẹp chặt trong giới hạn màn hình khả dụng (`0 <= x <= screenWidth - pieceWidth`, `0 <= y <= screenHeight - pieceHeight`). Mảnh ghép không bao giờ có thể bị kéo văng ra ngoài mép màn hình.
- **Xử lý sự cố cảm ứng (Touch Cancellation & Anti-Stuck Watchdog)**:
  - Khi hệ điều hành gửi tín hiệu ngắt cảm ứng (`PointerEventType.Cancel` hoặc nhận nhiều hơn 1 ngón tay), hệ thống lập tức nhả trạng thái kéo và trả mảnh ghép về khay.
  - Watchdog timer: Nếu mảnh ghép ở trạng thái `RETURNING_BOUNCE` quá `1.5 giây` mà chưa dừng hẳn, hệ thống tự động gán thẳng về vị trí nghỉ, đảm bảo không bao giờ bị đóng băng (never freeze).

---

### 3. Hệ Thống 16 Màn Chơi & Lưu Trữ Tiến Trình (Levels & DataStore)

#### 3.1 Phân cấp độ khó tăng dần
- **Giai đoạn mầm non (Ages 1-3)**:
  - Level 1: 2 mảnh (1x2) - Làm quen thao tác kéo thả cơ bản.
  - Level 2: 3 mảnh (1x3) - Nhận diện phần trên / giữa / dưới.
  - Level 3: 4 mảnh (2x2) - Bắt đầu nhận biết 4 góc phần tư.
  - Level 4: 4 mảnh (2x2) - Hoàn thiện kỹ năng cơ bản.
- **Giai đoạn phát triển (Ages 3-5)**:
  - Level 5: 5 mảnh.
  - Level 6: 6 mảnh (2x3).
  - Level 7: 6 mảnh (2x3).
  - Level 8: 7 mảnh.
  - Level 9: 8 mảnh (2x4).
  - Level 10: 8 mảnh (2x4).
- **Giai đoạn thử thách (Ages 4-6)**:
  - Level 11: 9 mảnh (3x3).
  - Level 12: 9 mảnh (3x3).
  - Level 13: 10 mảnh (2x5).
  - Level 14: 10 mảnh (2x5).
  - Level 15: 12 mảnh (3x4).
  - Level 16: 12 mảnh (3x4) - Đỉnh cao thử thách công chúa đăng quang.

#### 3.2 Lưu trữ Jetpack DataStore Preferences
- Quản lý các trường dữ liệu:
  - `stars_level_1` đến `stars_level_16`: Điểm sao đạt được (0 đến 3 sao).
  - `unlocked_all`: Cờ boolean đánh dấu đã mua gói Magic Pack (mặc định: `false`).
  - `sound_enabled`: Cờ bật/tắt hiệu ứng âm thanh (mặc định: `true`).
  - `music_enabled`: Cờ bật/tắt nhạc nền dịu êm (mặc định: `true`).
  - `sound_volume`: Âm lượng hiệu ứng âm thanh (0.0f đến 1.0f).
  - `music_volume`: Âm lượng nhạc nền (0.0f đến 1.0f).
  - `last_played_level`: Màn chơi gần nhất trẻ đang tương tác.

---

### 4. Hệ Thống Phần Thưởng Sống Động (Living Rewards & Confetti)

#### 4.1 Hạt pháo hoa giấy không cấp phát bộ nhớ (Zero-Allocation Confetti)
- Mảng 50 hạt confetti được khởi tạo sẵn một lần duy nhất (`ConfettiParticles`).
- Mỗi khung hình trong `withFrameNanos` cập nhật tọa độ $(x, y)$, vận tốc rơi, góc xoay và độ lắc lư con lắc sin, tái sử dụng bộ nhớ mà không kích hoạt Garbage Collector (GC pause).

#### 4.2 Hoạt ảnh chúc mừng theo từng chủ đề tranh (16 Living Celebrations)
- Mỗi màn chơi khi hoàn thành sẽ kích hoạt một hoạt ảnh tương tác riêng biệt:
  - Level 1: Vương miện phát tia sáng sao lấp lánh (Sparkle Rays).
  - Level 2: Tai thỏ ngoe nguẩy và đồng cỏ hoa nở rộ.
  - Level 3: Kỳ lân chớp mắt và cầu vồng chuyển màu quang phổ.
  - Level 4: Pháo hoa kẹo nổ tung trên đỉnh tháp lâu đài.
  - Level 5: Đôi thiên nga chụm đầu tạo hình trái tim và sóng nước gợn lăn tăn.
  - Level 6: Bánh xe cỗ xe bí ngô quay tròn lăn bánh.
  - Level 7: Lá liễu xanh biếc bay lượn trong gió thu.
  - Level 8: Làn hơi ấm áp bốc lên từ vòi tách trà hoàng gia.
  - Level 9: Đàn đom đóm dạ quang bay lượn quanh ban công.
  - Level 10: Dây đàn hạc vàng rung động phát sóng âm nhạc ngọc bích.
  - Level 11: Đài phun nước tung bọt trắng xóa và hoa súng nở.
  - Level 12: Vệt sao băng rực sáng lướt qua bầu trời đêm.
  - Level 13: Đèn hoa đăng trôi bồng bềnh trên dòng sông thơ mộng.
  - Level 14: Vòng quay ngựa gỗ nhấp nhô đèn led hội chợ.
  - Level 15: Chùm đèn pha lê lấp lánh phản chiếu dạ vũ hoàng cung.
  - Level 16: Mưa cánh hoa hồng rơi phủ đầy khu vườn lễ đăng quang.

---

### 5. Động Cơ Âm Thanh Native (Native Audio Engine)

#### 5.1 Kiến trúc Android Audio Native
- Sử dụng Android Audio API kết hợp bộ tổng hợp âm thanh số hóa sóng sin/tam giác (Procedural Audio Synthesizer) hoặc Android SoundPool/MediaPlayer.
- **Các loại âm thanh tương tác bắt buộc**:
  1. `playPickup()`: Âm thanh nhẹ nhàng khi chạm ngón tay nhấc mảnh ghép lên.
  2. `playReturnBounce()`: Tiếng "boing" / nảy bong bóng nước khi mảnh ghép trở về khay.
  3. `playSnap(placedCount, totalCount)`: Tiếng chuông ngọc "ding" thánh thót với cao độ tăng dần theo số lượng mảnh đã ghép đúng (tạo cảm giác tiến bộ phấn khích).
  4. `playWinFanfare()`: Khúc nhạc kèn trumpet hoàng gia hào hứng chúc mừng hoàn thành màn.
  5. `playTap()`: Âm thanh click nhẹ khi bấm các nút điều hướng.
  6. `playBackgroundMusic()`: Giai điệu nhạc ru êm dịu, không lời, lặp lại mượt mà, âm lượng cân bằng để không làm chói tai trẻ nhỏ.

---

### 6. Cổng Phụ Huynh & Bảo Vệ An Toàn Cho Trẻ (COPPA & Kid-Safe Gate)

- **Ngăn chặn trẻ vô tình thanh toán hoặc thoát app**:
  - Không có quảng cáo banner hay interstitial từ bên thứ ba.
  - Không thu thập dữ liệu cá nhân hay vị trí GPS.
  - Mọi thao tác vào cài đặt, mua hàng hoặc thoát app đều phải qua màn hình `ParentalGateOverlay`.
  - Phụ huynh có thể tùy chọn giữ nút 3 giây hoặc nhập đáp án bài toán cộng/nhân nhỏ.
