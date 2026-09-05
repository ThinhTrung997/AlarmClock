# ⏰ AlarmClock App - Tài liệu Tổng quan Dự án (Project Overview)

Ứng dụng **AlarmClock** là một ứng dụng đồng hồ đa năng trên hệ điều hành Android, được thiết kế theo phong cách giao diện tối hiện đại (**Modern Dark Theme**), bo góc mượt mà (**Material 3**) với đầy đủ 3 chức năng cốt lõi: **Báo thức (Alarm)**, **Đồng hồ Thế giới (World Clock)** và **Bấm giờ (Stopwatch)**.

---

## 📱 1. Các Tính năng Chính (Core Features)

### 🔔 1.1. Báo thức (Alarm)
- **Quản lý danh sách báo thức**: Hiển thị danh sách các báo thức đã lưu với giao diện thẻ trực quan (thời gian, AM/PM, nhãn tên, các ngày lặp lại và công tắc Bật/Tắt).
- **Thêm / Chỉnh sửa báo thức**:
  - Bộ cuộn chọn giờ và phút dạng số (`NumberPicker`).
  - Nút chuyển đổi nhanh **AM / PM**.
  - Tùy chọn lặp lại theo từng ngày trong tuần (Thứ 2 đến Chủ nhật, Everyday, Weekends).
  - Tùy chỉnh rung (`Vibration`) và chọn nhạc chuông từ hệ thống thiết bị (`RingtonePicker`).
  - Kiểm tra và cảnh báo chống trùng lặp giờ báo thức.
- **Xóa báo thức**: Nhấn giữ vào thẻ báo thức để mở hộp thoại xác nhận xóa.
- **Màn hình đổ chuông (`AlarmRingActivity`)**:
  - Tự động bật sáng màn hình và hiển thị đè lên màn hình khóa (`ShowWhenLocked`, `TurnScreenOn`, `WakeLock`).
  - Phát nhạc chuông báo thức liên tục (`MediaPlayer` trên luồng âm thanh `STREAM_ALARM`) và rung theo chu kỳ.
  - Hỗ trợ nút **Snooze (Nhắc lại sau 10 phút)** và nút **Dismiss (Tắt báo thức)**.
  - Tự động lên lịch lại báo thức sau khi thiết bị khởi động lại (`BootReceiver`).

---

### 🌍 1.2. Đồng hồ Thế giới (World Clock)
- **Thẻ Giờ Địa Phương (Local Time Card)**:
  - Tự động nhận diện múi giờ và vị trí của thiết bị (ví dụ: `Asia/Ho_Chi_Minh`, `San Francisco, CA`).
  - Đồng hồ số lớn chạy thời gian thực (tích tắc theo từng giây).
  - Icon ghim vị trí đặc trưng.
- **Danh sách thành phố trên thế giới**:
  - Hiển thị tên thành phố, quốc gia, giờ kỹ thuật số (`04:15 PM`).
  - Badge tính toán độ lệch múi giờ tự động so với giờ địa phương (ví dụ: `+9 hrs`, `-7 hrs`, `Local time`).
  - Hình quả địa cầu chìm tạo chiều sâu thị giác.
  - Đồng hồ của tất cả các thành phố đều cập nhật trực tiếp theo thời gian thực.
  - Nhấn giữ vào thành phố để xóa khỏi danh sách.
- **Nút FAB (+)**: Nút hành động nổi màu tím nổi bật mở nhanh màn hình Thêm Thành phố.

---

### ➕ 1.3. Thêm Thành phố (Add City)
- **Tìm kiếm tức thời (Instant Search)**: Ô nhập liệu tìm kiếm bo tròn với icon kính lúp, lọc nhanh danh sách thành phố hoặc quốc gia ngay khi người dùng gõ phím.
- **Danh mục thành phố toàn cầu**: Cung cấp sẵn cơ sở dữ liệu các thành phố lớn trên thế giới (Tokyo, London, New York, Paris, Seoul, Singapore, Sydney, Dubai, Hanoi, Berlin, Toronto,...).
- **Thao tác 1 chạm**: Nút tròn `+` bên cạnh mỗi thành phố cho phép thêm nhanh vào danh sách World Clock và lưu lại ngay lập tức.

---

### ⏱️ 1.4. Bấm giờ (Stopwatch)
- **Mặt đồng hồ số lớn**: Hiển thị định dạng `00:00:12.34` (Giờ:Phút:Giây cùng phần trăm giây nổi bật).
- **Cơ chế bấm giờ mượt mà**: Chạy trên chu kỳ ~30ms (`Handler` & `SystemClock.elapsedRealtime`) cho độ chính xác cao.
- **Hệ thống nút điều khiển tròn**:
  - **Start / Pause / Resume / Stop**: Chuyển đổi trạng thái linh hoạt với biểu tượng Play/Stop trực quan.
  - **Lap / Reset**: Khi đang chạy -> ghi nhận vòng chạy (**Lap**); Khi tạm dừng -> xóa và đặt lại về 0 (**Reset**).
- **Danh sách Lịch sử Vòng chạy (Lap List)**:
  - Hiển thị số vòng (`Lap 1`, `Lap 2`, `Lap 3`...) cùng thời gian phân đoạn tương ứng.
  - Vòng mới nhất luôn tự động xuất hiện ở trên đầu danh sách.
  - Đường kẻ phân cách mờ tinh tế giữa các dòng.

---

### 🧭 1.5. Thanh Điều hướng (Bottom Navigation)
- Thanh điều hướng 3 tab cố định dưới đáy màn hình: **Alarm**, **World Clock**, **Stopwatch**.
- Hiệu ứng **Pill Highlight** màu tím (`#8083FF`) bo tròn viền xung quanh tab đang hoạt động.
- Chuyển đổi Activity mượt mà không bị giật nháy màn hình (`FLAG_ACTIVITY_REORDER_TO_FRONT`).

---

## 🏗️ 2. Kiến trúc & Cấu trúc Thư mục (Architecture)

Dự án được phân chia theo cấu trúc Layered & Feature Modules chuẩn Android:

```
com.example.alarmclock
├── model/                     # Định nghĩa cấu trúc dữ liệu (Data Classes)
│   ├── Alarm.kt               # Model đối tượng Báo thức
│   ├── WorldClockCity.kt      # Model thành phố Đồng hồ thế giới
│   └── StopwatchLap.kt        # Model lưu dữ liệu vòng chạy Bấm giờ
│
├── data/                      # Tầng lưu trữ & Quản lý dữ liệu cục bộ
│   ├── AlarmStorage.kt        # Đọc/ghi báo thức vào SharedPreferences
│   └── WorldClockStorage.kt   # Đọc/ghi danh sách thành phố World Clock
│
├── receiver/                  # Tầng xử lý Sự kiện Hệ thống (BroadcastReceiver)
│   ├── AlarmReceiver.kt       # Bắt tín hiệu khi đến giờ báo thức, kích hoạt WakeLock & Notification
│   └── BootReceiver.kt        # Tự động lập lịch lại các báo thức khi máy khởi động lại
│
├── util/                      # Các tiện ích hệ thống & Điều hướng
│   ├── AlarmScheduler.kt      # Tương tác với AlarmManager (Schedule, Cancel, Snooze)
│   └── NavigationHelper.kt    # Quản lý giao diện & logic chuyển tab Bottom Navigation
│
└── ui/                        # Tầng Giao diện Người dùng (User Interface)
    ├── adapter/               # Các RecyclerView Adapter
    │   ├── AlarmAdapter.kt         # Adapter danh sách báo thức
    │   ├── WorldClockAdapter.kt    # Adapter danh sách đồng hồ thế giới
    │   ├── AddCityAdapter.kt       # Adapter tìm kiếm thêm thành phố
    │   └── StopwatchLapAdapter.kt  # Adapter danh sách vòng bấm giờ
    │
    ├── alarm/                 # Nhóm màn hình Báo thức
    │   ├── MainActivity.kt         # Màn hình danh sách báo thức (Trang chủ)
    │   ├── AddAlarmActivity.kt     # Màn hình thêm / sửa báo thức
    │   └── AlarmRingActivity.kt    # Màn hình hiển thị chuông reo báo thức
    │
    ├── worldclock/            # Nhóm màn hình Đồng hồ thế giới
    │   ├── WorldClockActivity.kt   # Màn hình đồng hồ thế giới & giờ địa phương
    │   └── AddCityActivity.kt      # Màn hình tìm kiếm & thêm thành phố
    │
    └── stopwatch/             # Nhóm màn hình Bấm giờ
        └── StopwatchActivity.kt    # Màn hình đồng hồ bấm giờ
```

---

## ⚙️ 3. Công nghệ & Thư viện Sử dụng (Tech Stack)

| Thành phần | Công nghệ / Thư viện |
| :--- | :--- |
| **Ngôn ngữ** | Kotlin 2.0+ |
| **Target SDK / Min SDK** | Min SDK: `24` (Android 7.0) / Target SDK: `36` / Compile SDK: `37.1` |
| **UI Toolkit** | XML Layouts, `ConstraintLayout 2.2.2`, `Material Components 1.14.0`, `RecyclerView` |
| **Hệ thống Báo thức** | Android `AlarmManager` (`setAlarmClock`, `canScheduleExactAlarms`) |
| **Âm thanh & Rung** | `MediaPlayer`, `RingtoneManager`, `Vibrator` / `VibratorManager` |
| **Thông báo & Đánh thức** | `NotificationManager`, `NotificationCompat` (High Priority Channel, Fullscreen Intent), `PowerManager.WakeLock` |
| **Lưu trữ dữ liệu** | `SharedPreferences` với cơ chế tuần tự hóa JSON (`org.json`) |
| **Quản lý quyền** | `ActivityResultContracts.RequestPermission` (quyền `POST_NOTIFICATIONS` cho Android 13+) |

---

## 🚀 4. Hướng dẫn Biên dịch & Chạy Ứng dụng (Build & Run)

1. **Yêu cầu môi trường**:
   - Android Studio (Koala / Ladybug hoặc mới hơn).
   - JDK 11 hoặc JDK 17+.
   - Android SDK API 24 trở lên.

2. **Lệnh biên dịch từ Terminal**:
   ```bash
   # Build bản Debug APK
   ./gradlew assembleDebug

   # Cài đặt trực tiếp lên thiết bị/giả lập
   ./gradlew installDebug
   ```

3. **Vị trí file APK xuất ra**:
   `app/build/outputs/apk/debug/app-debug.apk`

---
*Tài liệu được cập nhật tự động theo phiên bản mới nhất của dự án.*
