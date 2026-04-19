// =============================================
// API Service — kết nối Spring Boot BE
// Base URL: http://localhost:8080/room-rental/api
// =============================================

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/room-rental/api'

// Lấy token từ localStorage
const getToken = () => localStorage.getItem('accessToken')

// Helper: fetch với auth header
async function apiFetch(endpoint, options = {}) {
  const token = getToken()
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...(options.headers || {}),
  }

  const res = await fetch(`${BASE_URL}${endpoint}`, {
    ...options,
    headers,
  })

  const data = await res.json()

  if (!res.ok) {
    throw new Error(data.message || `HTTP ${res.status}`)
  }

  return data
}

// ──────────────────────────────────────────────
// AUTH
// ──────────────────────────────────────────────

/**
 * POST /auth/login
 * Body: { email, password }
 * Returns: { accessToken, refreshToken }
 */
export async function login(email, password) {
  const data = await apiFetch('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  })
  return data.data // { accessToken, refreshToken }
}

/**
 * POST /auth/google
 * Body: { idToken }
 * Returns: { accessToken, refreshToken }
 */
export async function loginWithGoogle(idToken) {
  const data = await apiFetch('/auth/google', {
    method: 'POST',
    body: JSON.stringify({ idToken }),
  })
  return data.data // { accessToken, refreshToken }
}

// ──────────────────────────────────────────────
// USER
// ──────────────────────────────────────────────

/**
 * GET /users/profile
 * Returns: { id, username, email, phone, avatar, role }
 */
export async function getMyProfile() {
  const data = await apiFetch('/users/profile')
  return data.data
}

/**
 * GET /users/{id}
 */
export async function getUserById(id) {
  const data = await apiFetch(`/users/${id}`)
  return data.data
}

/**
 * GET /users — lấy tất cả users (ADMIN only trong BE)
 * FE sẽ filter theo role phù hợp khi hiển thị
 */
export async function getAllUsers() {
  const data = await apiFetch('/users')
  return data.data // List<UserResponse>
}

// ──────────────────────────────────────────────
// CHAT
// ──────────────────────────────────────────────

/**
 * POST /chat/room
 * Body: { targetUserId }
 * Returns: { roomId, participantIds, createdAt, updatedAt }
 */
export async function getOrCreateChatRoom(targetUserId) {
  const data = await apiFetch('/chat/room', {
    method: 'POST',
    body: JSON.stringify({ targetUserId }),
  })
  return data.data
}
