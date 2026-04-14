import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import LoginPage from './pages/LoginPage'
import ChatListPage from './pages/ChatListPage'
import ChatRoomPage from './pages/ChatRoomPage'

// Guard: yêu cầu đăng nhập
function PrivateRoute({ children }) {
  const { token, loading } = useAuth()

  if (loading) {
    return (
      <div style={{
        height: '100vh', display: 'flex', flexDirection: 'column',
        alignItems: 'center', justifyContent: 'center', gap: 16,
        background: 'var(--bg-base)'
      }}>
        <span style={{ fontSize: 40 }}>🏠</span>
        <span className="spinner" style={{ width: 28, height: 28 }} />
        <span style={{ color: 'var(--text-muted)', fontSize: 14 }}>Đang tải...</span>
      </div>
    )
  }

  return token ? children : <Navigate to="/login" replace />
}

// Guard: đã login rồi thì không vào login nữa
function PublicRoute({ children }) {
  const { token, loading } = useAuth()
  if (loading) return null
  return token ? <Navigate to="/chat" replace /> : children
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/chat" replace />} />

      <Route
        path="/login"
        element={
          <PublicRoute>
            <LoginPage />
          </PublicRoute>
        }
      />

      <Route
        path="/chat"
        element={
          <PrivateRoute>
            <ChatListPage />
          </PrivateRoute>
        }
      />

      <Route
        path="/chat/:roomId"
        element={
          <PrivateRoute>
            <ChatRoomPage />
          </PrivateRoute>
        }
      />

      {/* Fallback */}
      <Route path="*" element={<Navigate to="/chat" replace />} />
    </Routes>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  )
}
