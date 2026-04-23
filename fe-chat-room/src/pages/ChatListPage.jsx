import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { getAllUsers, getOrCreateChatRoom } from '../services/api'
import { useAuth } from '../context/AuthContext'
import styles from './ChatListPage.module.css'

// Badge role color
function RoleBadge({ role }) {
  const cls = role === 'USER' ? 'badge-user' : role === 'LANDLORD' ? 'badge-landlord' : 'badge-admin'
  const label = role === 'USER' ? '👤 Người thuê' : role === 'LANDLORD' ? '🏠 Chủ nhà' : '⚙️ Admin'
  return <span className={`badge ${cls}`}>{label}</span>
}

// Avatar từ username
function Avatar({ username, avatar, size = 44 }) {
  if (avatar) {
    return (
      <img
        src={avatar}
        alt={username}
        style={{ width: size, height: size, borderRadius: '50%', objectFit: 'cover' }}
      />
    )
  }
  const initials = (username || '?').slice(0, 2).toUpperCase()
  const colors = ['#6c63ff', '#06d6a0', '#f59e0b', '#ef4444', '#8b5cf6']
  const color = colors[(username?.charCodeAt(0) || 0) % colors.length]
  return (
    <div
      style={{
        width: size, height: size,
        borderRadius: '50%',
        background: `linear-gradient(135deg, ${color}88, ${color})`,
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        fontWeight: 700, fontSize: size * 0.36, color: '#fff',
        flexShrink: 0,
      }}
    >
      {initials}
    </div>
  )
}

export default function ChatListPage() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const [contacts, setContacts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [connecting, setConnecting] = useState(null) // userId đang kết nối

  // Xác định role cần lấy: USER → thấy LANDLORD, LANDLORD → thấy USER
  const targetRole = user?.role === 'USER' ? 'LANDLORD' : 'USER'
  const roleLabel = targetRole === 'LANDLORD' ? 'Chủ nhà' : 'Người thuê'

  useEffect(() => {
    loadContacts()
  }, [user])

  async function loadContacts() {
    try {
      setLoading(true)
      const all = await getAllUsers()
      // Filter theo role đối diện, loại bỏ chính mình
      const filtered = all.filter(u => u.role === targetRole && u.id !== user?.id)
      setContacts(filtered)
    } catch (err) {
      setError(err.message || 'Không thể tải danh sách người dùng')
    } finally {
      setLoading(false)
    }
  }

  async function startChat(targetUser) {
    try {
      setConnecting(targetUser.id)
      const room = await getOrCreateChatRoom(targetUser.id)
      // Chuyển sang trang chat với roomId và thông tin đối phương
      navigate(`/chat/${room.roomId}`, {
        state: { roomId: room.roomId, targetUser }
      })
    } catch (err) {
      alert('Không thể tạo phòng chat: ' + err.message)
    } finally {
      setConnecting(null)
    }
  }

  return (
    <div className={styles.layout}>
      {/* Sidebar */}
      <aside className={styles.sidebar}>
        {/* Current user info */}
        <div className={styles.profile}>
          <Avatar username={user?.username} avatar={user?.avatar} size={48} />
          <div className={styles.profileInfo}>
            <div className={styles.profileName}>{user?.username || 'Người dùng'}</div>
            <RoleBadge role={user?.role} />
          </div>
          <button
            id="logout-btn"
            className={styles.logoutBtn}
            onClick={logout}
            title="Đăng xuất"
          >
            ⏻
          </button>
        </div>

        {/* Search / Title */}
        <div className={styles.sidebarHeader}>
          <h2 className={styles.sidebarTitle}>Danh sách {roleLabel}</h2>
          <button
            id="refresh-contacts"
            className={styles.refreshBtn}
            onClick={loadContacts}
            title="Làm mới"
          >
            ↻
          </button>
        </div>

        {/* Contacts */}
        <div className={styles.contacts}>
          {loading ? (
            <div className={styles.center}>
              <span className="spinner" />
              <span style={{ color: 'var(--text-muted)', fontSize: 13 }}>Đang tải...</span>
            </div>
          ) : error ? (
            <div className={styles.errorBox}>
              <p>⚠️ {error}</p>
              <button className={styles.retryBtn} onClick={loadContacts}>Thử lại</button>
            </div>
          ) : contacts.length === 0 ? (
            <div className={styles.empty}>
              <span style={{ fontSize: 36 }}>🔍</span>
              <p>Không có {roleLabel.toLowerCase()} nào</p>
            </div>
          ) : (
            contacts.map((contact, i) => (
              <button
                key={contact.id}
                id={`contact-${contact.id}`}
                className={styles.contactItem}
                style={{ animationDelay: `${i * 0.05}s` }}
                onClick={() => startChat(contact)}
                disabled={connecting === contact.id}
              >
                <div className={styles.contactAvatar}>
                  <Avatar username={contact.username} avatar={contact.avatar} size={44} />
                  <span className={styles.onlineDot} />
                </div>
                <div className={styles.contactInfo}>
                  <span className={styles.contactName}>{contact.username}</span>
                  <span className={styles.contactEmail}>{contact.email}</span>
                </div>
                {connecting === contact.id ? (
                  <span className="spinner" />
                ) : (
                  <span className={styles.chatArrow}>›</span>
                )}
              </button>
            ))
          )}
        </div>
      </aside>

      {/* Main placeholder */}
      <main className={styles.main}>
        <div className={styles.mainPlaceholder}>
          <div style={{ fontSize: 72, marginBottom: 16 }}>💬</div>
          <h2 style={{ color: 'var(--text-primary)', marginBottom: 8 }}>
            Chọn một cuộc trò chuyện
          </h2>
          <p style={{ color: 'var(--text-muted)', fontSize: 14 }}>
            Chọn {roleLabel.toLowerCase()} từ danh sách bên trái để bắt đầu chat
          </p>
        </div>
      </main>
    </div>
  )
}
