// import { useState, useEffect, useRef } from 'react'
// import { useLocation, useNavigate, useParams } from 'react-router-dom'
// import { ref, onValue, push, serverTimestamp } from 'firebase/database'
// import { db } from '../services/firebase'
// import { useAuth } from '../context/AuthContext'
// import { getUserById } from '../services/api'
// import styles from './ChatRoomPage.module.css'

// // Format timestamp
// function formatTime(ts) {
//   if (!ts) return ''
//   const d = new Date(ts)
//   return d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
// }

// // Format date header
// function formatDate(ts) {
//   if (!ts) return ''
//   const d = new Date(ts)
//   const today = new Date()
//   if (d.toDateString() === today.toDateString()) return 'Hôm nay'
//   return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
// }

// function Avatar({ username, avatar, size = 36 }) {
//   if (avatar) {
//     return <img src={avatar} alt={username} style={{ width: size, height: size, borderRadius: '50%', objectFit: 'cover' }} />
//   }
//   const initials = (username || '?').slice(0, 2).toUpperCase()
//   const colors = ['#6c63ff', '#06d6a0', '#f59e0b', '#ef4444', '#8b5cf6']
//   const color = colors[(username?.charCodeAt(0) || 0) % colors.length]
//   return (
//     <div style={{
//       width: size, height: size, borderRadius: '50%',
//       background: `linear-gradient(135deg, ${color}88, ${color})`,
//       display: 'flex', alignItems: 'center', justifyContent: 'center',
//       fontWeight: 700, fontSize: size * 0.36, color: '#fff', flexShrink: 0,
//     }}>{initials}</div>
//   )
// }

// export default function ChatRoomPage() {
//   const { roomId } = useParams()
//   const location = useLocation()
//   const navigate = useNavigate()
//   const { user } = useAuth()

//   // Lấy thông tin đối phương từ state (navigate) hoặc fetch từ API
//   const [targetUser, setTargetUser] = useState(location.state?.targetUser || null)
//   const [messages, setMessages] = useState([])
//   const [text, setText] = useState('')
//   const [sending, setSending] = useState(false)
//   const [isLoading, setIsLoading] = useState(true)
//   const messagesEndRef = useRef(null)

//   // Load targetUser nếu chưa có
//   useEffect(() => {
//     if (!targetUser && roomId && user) {
//       // Lấy userId của đối phương từ roomId (format: id1_id2)
//       const parts = roomId.split('_')
//       const otherId = parts.find(p => p !== user.id) || parts[0]
//       if (otherId && otherId !== user.id) {
//         getUserById(otherId)
//           .then(u => setTargetUser(u))
//           .catch(() => {})
//       }
//     }
//   }, [roomId, user])

//   // Subscribe Firebase Realtime DB
//   useEffect(() => {
//     if (!roomId) return

//     const messagesRef = ref(db, `chat_rooms/${roomId}/messages`)
//     const unsubscribe = onValue(messagesRef, (snapshot) => {
//       const data = snapshot.val()
//       const list = data
//         ? Object.entries(data).map(([id, msg]) => ({ id, ...msg }))
//             .sort((a, b) => (a.timestamp || 0) - (b.timestamp || 0))
//         : []
//       setMessages(list)
//       setIsLoading(false)
//     })

//     return () => unsubscribe()
//   }, [roomId])

//   // Auto scroll
//   useEffect(() => {
//     messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
//   }, [messages])

//   async function sendMessage(e) {
//     e.preventDefault()
//     const trimmed = text.trim()
//     if (!trimmed || sending) return

//     setSending(true)
//     setText('')
//     try {
//       const messagesRef = ref(db, `chat_rooms/${roomId}/messages`)
//       await push(messagesRef, {
//         senderId: user.id,
//         senderName: user.username,
//         content: trimmed,
//         timestamp: serverTimestamp(),
//       })
//     } catch (err) {
//       console.error('Send failed:', err)
//       setText(trimmed) // restore
//     } finally {
//       setSending(false)
//     }
//   }

//   function handleKeyDown(e) {
//     if (e.key === 'Enter' && !e.shiftKey) {
//       e.preventDefault()
//       sendMessage(e)
//     }
//   }

//   // Group messages by date
//   let lastDate = ''

//   return (
//     <div className={styles.layout}>
//       {/* Sidebar mini */}
//       <aside className={styles.sidebar}>
//         <button
//           id="back-to-list"
//           className={styles.backBtn}
//           onClick={() => navigate('/chat')}
//         >
//           ‹ Quay lại
//         </button>

//         <div className={styles.targetCard}>
//           <Avatar username={targetUser?.username} avatar={targetUser?.avatar} size={56} />
//           <div className={styles.targetName}>{targetUser?.username || 'Đang tải...'}</div>
//           <div className={styles.targetEmail}>{targetUser?.email || ''}</div>
//           {targetUser?.role && (
//             <span className={`badge ${targetUser.role === 'USER' ? 'badge-user' : 'badge-landlord'}`}>
//               {targetUser.role === 'USER' ? '👤 Người thuê' : '🏠 Chủ nhà'}
//             </span>
//           )}
//         </div>

//         {/* Room info */}
//         <div className={styles.roomInfo}>
//           <div className={styles.roomInfoLabel}>Room ID</div>
//           <div className={styles.roomIdText}>{roomId}</div>
//         </div>
//       </aside>

//       {/* Chat area */}
//       <main className={styles.chat}>
//         {/* Header */}
//         <div className={styles.chatHeader}>
//           <Avatar username={targetUser?.username} avatar={targetUser?.avatar} size={40} />
//           <div>
//             <div className={styles.headerName}>{targetUser?.username || '...'}</div>
//             <div className={styles.headerStatus}>
//               <span className={styles.statusDot} /> Online
//             </div>
//           </div>
//         </div>

//         {/* Messages */}
//         <div className={styles.messages}>
//           {isLoading ? (
//             <div className={styles.loadingMsg}>
//               <span className="spinner" />
//               <span>Đang tải tin nhắn...</span>
//             </div>
//           ) : messages.length === 0 ? (
//             <div className={styles.emptyMsg}>
//               <span style={{ fontSize: 40 }}>👋</span>
//               <p>Chưa có tin nhắn. Hãy bắt đầu trò chuyện!</p>
//             </div>
//           ) : (
//             messages.map((msg) => {
//               const isMine = msg.senderId === user?.id
//               const dateStr = formatDate(msg.timestamp)
//               const showDate = dateStr !== lastDate
//               lastDate = dateStr

//               return (
//                 <div key={msg.id}>
//                   {showDate && (
//                     <div className={styles.dateDivider}>
//                       <span>{dateStr}</span>
//                     </div>
//                   )}
//                   <div className={`${styles.msgRow} ${isMine ? styles.mine : styles.theirs}`}>
//                     {!isMine && (
//                       <Avatar username={msg.senderName} size={28} />
//                     )}
//                     <div className={styles.bubble}>
//                       <div className={styles.bubbleContent}>{msg.content}</div>
//                       <div className={styles.bubbleTime}>{formatTime(msg.timestamp)}</div>
//                     </div>
//                   </div>
//                 </div>
//               )
//             })
//           )}
//           <div ref={messagesEndRef} />
//         </div>

//         {/* Input */}
//         <form className={styles.inputBar} onSubmit={sendMessage}>
//           <textarea
//             id="chat-input"
//             className={styles.textarea}
//             placeholder="Nhập tin nhắn... (Enter để gửi)"
//             value={text}
//             onChange={e => setText(e.target.value)}
//             onKeyDown={handleKeyDown}
//             rows={1}
//             disabled={sending}
//           />
//           <button
//             id="send-btn"
//             type="submit"
//             className={styles.sendBtn}
//             disabled={!text.trim() || sending}
//           >
//             {sending ? <span className="spinner" /> : '➤'}
//           </button>
//         </form>
//       </main>
//     </div>
//   )
// }

import { useState, useEffect, useRef } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router-dom'
// 1. IMPORT TỪ FIRESTORE THAY VÌ DATABASE
import { collection, addDoc, onSnapshot, query, orderBy, serverTimestamp } from 'firebase/firestore'
import { db } from '../services/firebase'
import { useAuth } from '../context/AuthContext'
import { getUserById } from '../services/api'
import styles from './ChatRoomPage.module.css'

// Format timestamp (Fix lại một chút để tương thích với Firestore Timestamp)
function formatTime(ts) {
  if (!ts) return ''
  // Firestore trả về object có hàm toDate()
  const d = ts.toDate ? ts.toDate() : new Date(ts)
  return d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
}

// Format date header
function formatDate(ts) {
  if (!ts) return ''
  const d = ts.toDate ? ts.toDate() : new Date(ts)
  const today = new Date()
  if (d.toDateString() === today.toDateString()) return 'Hôm nay'
  return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

function Avatar({ username, avatar, size = 36 }) {
  if (avatar) {
    return <img src={avatar} alt={username} style={{ width: size, height: size, borderRadius: '50%', objectFit: 'cover' }} />
  }
  const initials = (username || '?').slice(0, 2).toUpperCase()
  const colors = ['#6c63ff', '#06d6a0', '#f59e0b', '#ef4444', '#8b5cf6']
  const color = colors[(username?.charCodeAt(0) || 0) % colors.length]
  return (
    <div style={{
      width: size, height: size, borderRadius: '50%',
      background: `linear-gradient(135deg, ${color}88, ${color})`,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      fontWeight: 700, fontSize: size * 0.36, color: '#fff', flexShrink: 0,
    }}>{initials}</div>
  )
}

export default function ChatRoomPage() {
  const { roomId } = useParams()
  const location = useLocation()
  const navigate = useNavigate()
  const { user } = useAuth()

  const [targetUser, setTargetUser] = useState(location.state?.targetUser || null)
  const [messages, setMessages] = useState([])
  const [text, setText] = useState('')
  const [sending, setSending] = useState(false)
  const [isLoading, setIsLoading] = useState(true)
  const messagesEndRef = useRef(null)

  useEffect(() => {
    if (!targetUser && roomId && user) {
      const parts = roomId.split('_')
      const otherId = parts.find(p => p !== user.id) || parts[0]
      if (otherId && otherId !== user.id) {
        getUserById(otherId)
          .then(u => setTargetUser(u))
          .catch(() => { })
      }
    }
  }, [roomId, user])

  // ----------------------------------------------------
  // 2. SUBSCRIBE FIRESTORE REALTIME
  // ----------------------------------------------------
  useEffect(() => {
    if (!roomId) return

    // Trỏ tới: chat_rooms -> {roomId} -> messages
    const messagesRef = collection(db, "chat_rooms", roomId, "messages")
    // Sắp xếp theo thời gian tăng dần
    const q = query(messagesRef, orderBy("timestamp", "asc"))

    const unsubscribe = onSnapshot(q, (snapshot) => {
      const list = snapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data()
      }))

      setMessages(list)
      setIsLoading(false)
    }, (error) => {
      console.error("Lỗi lấy tin nhắn: ", error)
      setIsLoading(false)
    })

    return () => unsubscribe()
  }, [roomId])

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  // ----------------------------------------------------
  // 3. GỬI TIN NHẮN LÊN FIRESTORE
  // ----------------------------------------------------
  async function sendMessage(e) {
    e.preventDefault()
    const trimmed = text.trim()
    if (!trimmed || sending) return

    setSending(true)
    setText('')
    try {
      const messagesRef = collection(db, "chat_rooms", roomId, "messages")
      await addDoc(messagesRef, {
        senderId: user.id,
        senderName: user.username,
        content: trimmed,
        timestamp: serverTimestamp(), // Hàm của Firestore
      })
    } catch (err) {
      console.error('Send failed:', err)
      setText(trimmed) // restore nếu lỗi
    } finally {
      setSending(false)
    }
  }

  function handleKeyDown(e) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      sendMessage(e)
    }
  }

  let lastDate = ''

  return (
    <div className={styles.layout}>
      <aside className={styles.sidebar}>
        <button
          id="back-to-list"
          className={styles.backBtn}
          onClick={() => navigate('/chat')}
        >
          ‹ Quay lại
        </button>

        <div className={styles.targetCard}>
          <Avatar username={targetUser?.username} avatar={targetUser?.avatar} size={56} />
          <div className={styles.targetName}>{targetUser?.username || 'Đang tải...'}</div>
          <div className={styles.targetEmail}>{targetUser?.email || ''}</div>
          {targetUser?.role && (
            <span className={`badge ${targetUser.role === 'USER' ? 'badge-user' : 'badge-landlord'}`}>
              {targetUser.role === 'USER' ? '👤 Người thuê' : '🏠 Chủ nhà'}
            </span>
          )}
        </div>

        <div className={styles.roomInfo}>
          <div className={styles.roomInfoLabel}>Room ID</div>
          <div className={styles.roomIdText}>{roomId}</div>
        </div>
      </aside>

      <main className={styles.chat}>
        <div className={styles.chatHeader}>
          <Avatar username={targetUser?.username} avatar={targetUser?.avatar} size={40} />
          <div>
            <div className={styles.headerName}>{targetUser?.username || '...'}</div>
            <div className={styles.headerStatus}>
              <span className={styles.statusDot} /> Online
            </div>
          </div>
        </div>

        <div className={styles.messages}>
          {isLoading ? (
            <div className={styles.loadingMsg}>
              <span className="spinner" />
              <span>Đang tải tin nhắn...</span>
            </div>
          ) : messages.length === 0 ? (
            <div className={styles.emptyMsg}>
              <span style={{ fontSize: 40 }}>👋</span>
              <p>Chưa có tin nhắn. Hãy bắt đầu trò chuyện!</p>
            </div>
          ) : (
            messages.map((msg) => {
              const isMine = msg.senderId === user?.id
              // Chặn render nếu timestamp đang bị null (do server Firebase chưa kịp trả về)
              if (!msg.timestamp) return null;

              const dateStr = formatDate(msg.timestamp)
              const showDate = dateStr !== lastDate
              lastDate = dateStr

              return (
                <div key={msg.id}>
                  {showDate && (
                    <div className={styles.dateDivider}>
                      <span>{dateStr}</span>
                    </div>
                  )}
                  <div className={`${styles.msgRow} ${isMine ? styles.mine : styles.theirs}`}>
                    {!isMine && (
                      <Avatar username={msg.senderName} size={28} />
                    )}
                    <div className={styles.bubble}>
                      <div className={styles.bubbleContent}>{msg.content}</div>
                      <div className={styles.bubbleTime}>{formatTime(msg.timestamp)}</div>
                    </div>
                  </div>
                </div>
              )
            })
          )}
          <div ref={messagesEndRef} />
        </div>

        <form className={styles.inputBar} onSubmit={sendMessage}>
          <textarea
            id="chat-input"
            className={styles.textarea}
            placeholder="Nhập tin nhắn... (Enter để gửi)"
            value={text}
            onChange={e => setText(e.target.value)}
            onKeyDown={handleKeyDown}
            rows={1}
            disabled={sending}
          />
          <button
            id="send-btn"
            type="submit"
            className={styles.sendBtn}
            disabled={!text.trim() || sending}
          >
            {sending ? <span className="spinner" /> : '➤'}
          </button>
        </form>
      </main>
    </div>
  )
}