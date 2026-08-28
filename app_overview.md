# App Overview — AlarmClock

## 1. Mô tả App

**AlarmClock** là ứng dụng Android quản lý báo thức đa năng, kết hợp đồng hồ thế giới và đồng hồ bấm giờ. Người dùng có thể tạo, chỉnh sửa và quản lý nhiều báo thức với tùy chọn linh hoạt về lịch lặp, nhạc chuông, rung. Ứng dụng hỗ trợ **13 ngôn ngữ**, **chế độ sáng/tối** và giao diện hiện đại.

---

## 2. Chức năng

| # | Chức năng | Trạng thái |
|---|-----------|-----------|
| 1 | **Báo thức** — Tạo, sửa, xóa, bật/tắt báo thức | ✅ Hoàn thành |
| 2 | **Đồng hồ thế giới** — Xem giờ nhiều múi giờ, thêm/xóa thành phố | ✅ Hoàn thành |
| 3 | **Đồng hồ bấm giờ (Stopwatch)** — Bấm giờ, ghi lap, dừng, reset | ✅ Hoàn thành |
| 4 | **Cài đặt** — Ngôn ngữ, giao diện sáng/tối, âm lượng tăng dần | ✅ Hoàn thành |
| 5 | **Chọn ngôn ngữ lần đầu** — Màn hình chọn ngôn ngữ khi cài app mới | ✅ Hoàn thành |

---

## 3. Màn hình

### 3.1. Màn hình Home (Alarm — `MainActivity`)

**Mục đích:** Màn hình chính, hiển thị danh sách tất cả báo thức đã tạo.

**Các thành phần chính:**
- Header với tiêu đề và nút ⚙️ (Settings)
- `RecyclerView` danh sách báo thức (hiển thị giờ, nhãn, lịch lặp, toggle bật/tắt)
- FAB (+) để thêm báo thức mới
- Bottom Navigation (Alarm / World Clock / Stopwatch)

**Người dùng có thể:**
- Xem toàn bộ danh sách báo thức
- Bật / tắt báo thức qua toggle switch
- Nhấn vào báo thức để **chỉnh sửa**
- Nhấn giữ báo thức để **xóa** (có hộp thoại xác nhận)
- Nhấn FAB để **thêm báo thức mới**
- Nhấn ⚙️ để vào **màn hình Cài đặt**

---

### 3.2. Màn hình Add / Edit Alarm (`AddAlarmActivity`)

**Mục đích:** Tạo mới hoặc chỉnh sửa một báo thức hiện có.

**Các thành phần chính:**
- Header: nút ✕ đóng, nút "Save" (text)
- **Time Picker:** `NumberPicker` giờ + phút + toggle AM/PM
- **Nhãn (Label):** Ô nhập tên báo thức
- **Lịch lặp:** 7 nút ngày trong tuần (Mon–Sun), tóm tắt lịch lặp (Everyday / Weekdays / Weekends / Never / custom)
- **Ngày cụ thể:** Nhấn để mở `DatePickerDialog` chọn ngày/tháng/năm, có nút "Clear Date"
- **Nhạc chuông:** Nhấn để mở `RingtoneManager` chọn nhạc từ hệ thống
- **Rung (Vibration):** Toggle switch
- Nút **Save** dưới cùng

**Người dùng có thể:**
- Chọn giờ, phút, AM/PM
- Đặt tên/nhãn cho báo thức
- Chọn ngày lặp (theo tuần) hoặc ngày cụ thể
- Chọn nhạc chuông từ danh sách nhạc hệ thống
- Bật/tắt rung
- Lưu báo thức (kiểm tra trùng giờ) hoặc đóng không lưu

---

### 3.3. Màn hình Alarm Ring (`AlarmRingActivity`)

**Mục đích:** Hiển thị khi báo thức rung/chuông, cho phép dừng hoặc báo thức lại (snooze).

**Các thành phần chính:**
- Giờ hiện tại (lớn, nổi bật)
- Tên báo thức
- Nút **Dừng (Stop / Dismiss)**
- Nút **Báo lại (Snooze)**
- Phát nhạc chuông + rung, wake lock màn hình

**Người dùng có thể:**
- Dừng hoàn toàn báo thức
- Chọn báo lại sau vài phút (snooze)

---

### 3.4. Màn hình World Clock (`WorldClockActivity`)

**Mục đích:** Xem giờ thực tế của nhiều thành phố/múi giờ trên thế giới.

**Các thành phần chính:**
- Danh sách các thành phố đã thêm (hiển thị tên, múi giờ, giờ hiện tại, chênh lệch giờ so với local)
- Nút **+** để thêm thành phố mới (`AddCityActivity`)
- Swipe / xóa thành phố

**Người dùng có thể:**
- Xem giờ thực theo múi giờ của nhiều thành phố
- Thêm thành phố từ danh sách tìm kiếm
- Xóa thành phố khỏi danh sách

---

### 3.5. Màn hình Stopwatch (`StopwatchActivity`)

**Mục đích:** Đồng hồ bấm giờ với chức năng ghi lap.

**Các thành phần chính:**
- Hiển thị thời gian (giờ : phút : giây : mili giây)
- Nút **Start / Pause / Resume**
- Nút **Lap** (ghi thời gian hiện tại)
- Nút **Reset**
- `RecyclerView` danh sách các lap đã ghi

**Người dùng có thể:**
- Bắt đầu / tạm dừng / tiếp tục bấm giờ
- Ghi lại thời gian từng vòng (lap)
- Reset về 0

---

### 3.6. Màn hình Cài đặt (`SettingsActivity`)

**Mục đích:** Tùy chỉnh giao diện và hành vi của ứng dụng.

**Các thành phần chính:**
- **Language** — Hiển thị ngôn ngữ hiện tại, nhấn để vào màn hình chọn ngôn ngữ
- **Theme Mode** — Chọn Light / Dark / Follow System (dialog)
- **Gradual Volume Increase** — Toggle bật/tắt tính năng tăng âm lượng dần khi báo thức rung

**Người dùng có thể:**
- Đổi ngôn ngữ hiển thị toàn app (13 ngôn ngữ)
- Chuyển giao diện sáng ↔ tối ↔ theo hệ thống
- Bật/tắt tăng âm lượng dần

---

### 3.7. Màn hình Chọn ngôn ngữ (`LanguageActivity`)

**Mục đích:** Chọn ngôn ngữ — hiển thị bắt buộc lần đầu cài app, có thể truy cập lại từ Settings.

**Các thành phần chính:**
- Header: nút quay lại (ẩn nếu là lần đầu), tiêu đề "Select Language", nút ✓ xác nhận (tròn xanh)
- `RecyclerView` 13 ngôn ngữ kèm cờ quốc gia tròn
- Item được chọn: nền xanh/tím nhạt + icon tick bên phải

**Hỗ trợ 13 ngôn ngữ:**
🇬🇧 English · 🇩🇪 German · 🇫🇷 French · 🇪🇸 Spanish · 🇮🇹 Italian · 🇳🇱 Dutch · 🇵🇹 Portuguese · 🇦🇪 Arabic · 🇰🇷 Korean · 🇯🇵 Japanese · 🇮🇳 Hindi · 🇮🇩 Indonesian · 🇻🇳 Vietnamese

**Người dùng có thể:**
- Chọn ngôn ngữ từ danh sách (single-select)
- Nhấn ✓ để xác nhận và áp dụng ngay lập tức
- Lần đầu: sau khi chọn → vào `MainActivity`
- Từ Settings: sau khi chọn → quay lại Settings với ngôn ngữ mới

---

## 4. Kiến trúc kỹ thuật

| Thành phần | Chi tiết |
|---|---|
| **Ngôn ngữ** | Kotlin |
| **Lưu trữ** | SharedPreferences (`AlarmStorage`, `SettingsStorage`, `WorldClockStorage`) |
| **Lịch báo thức** | `AlarmManager` + `BroadcastReceiver` (`AlarmReceiver`) |
| **Đa ngôn ngữ** | `AppCompatDelegate.setApplicationLocales` + `res/values-*/strings.xml` |
| **Giao diện** | Light/Dark Mode qua `AppCompatDelegate.setDefaultNightMode` + theme-aware color resources |
| **Navigation** | Bottom Navigation tùy chỉnh (`NavigationHelper`) |
