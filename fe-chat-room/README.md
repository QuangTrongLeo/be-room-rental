# fe-chat-room

> FE chat đơn giản cho hệ thống Room Rental — React + Vite + Firebase Realtime DB

## Cấu trúc

```
src/
├── context/
│   └── AuthContext.jsx      # Global auth state (user, token, login/logout)
├── pages/
│   ├── LoginPage.jsx        # Trang đăng nhập
│   ├── ChatListPage.jsx     # Danh sách contacts (USER thấy LANDLORD và ngược lại)
│   └── ChatRoomPage.jsx     # Màn hình chat realtime (Firebase)
├── services/
│   ├── api.js               # Axios-like fetch wrapper cho Spring Boot BE
│   └── firebase.js          # Firebase initialization
├── App.jsx                  # Routes + Auth guards
└── index.css                # Global design tokens & animations
```

## Setup

### 1. Cấu hình Firebase
Mở file `.env` và điền thông tin Firebase project:
```env
VITE_FIREBASE_API_KEY=...
VITE_FIREBASE_AUTH_DOMAIN=...
VITE_FIREBASE_DATABASE_URL=https://your-project-rtdb.firebaseio.com
VITE_FIREBASE_PROJECT_ID=...
```

Bật **Realtime Database** trong Firebase Console và set rules:
```json
{
  "rules": {
    "chat_rooms": {
      "$roomId": {
        ".read": "auth != null",
        ".write": "auth != null"
      }
    }
  }
}
```
> **Lưu ý**: FE dùng Anonymous/No-auth Firebase — nếu BE không cấu hình Firebase Auth, hãy set rules `.read: true, .write: true` tạm thời để test.

### 2. Cài dependencies
```bash
npm install
```

### 3. Chạy dev
```bash
npm run dev
```
Mở http://localhost:3000

## API Flow

| Bước | Method | Endpoint | Mô tả |
|------|--------|----------|-------|
| Login | POST | `/auth/login` | Trả về `accessToken` |
| Profile | GET | `/users/profile` | Lấy thông tin user hiện tại |
| Contacts | GET | `/users` | Lấy tất cả users → filter theo role |
| Chat room | POST | `/chat/room` | `{ targetUserId }` → `{ roomId }` |
| Messages | Firebase RT | `chat_rooms/{roomId}/messages` | Realtime messages |

## Lưu ý

- `GET /users` là ADMIN only trong BE → cần thêm endpoint `/users?role=LANDLORD` hoặc mở quyền cho USER/LANDLORD nếu muốn FE hoạt động đúng.
- Firebase path: `chat_rooms/{roomId}/messages` — khớp với BE service.
