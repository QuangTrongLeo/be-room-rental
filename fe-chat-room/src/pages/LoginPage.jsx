import { useState, useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { login, loginWithGoogle } from '../services/api'
import { useAuth } from '../context/AuthContext'
import styles from './LoginPage.module.css'

const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID

export default function LoginPage() {
  const { saveLogin } = useAuth()
  const navigate = useNavigate()
  const googleBtnRef = useRef(null)

  const [form, setForm] = useState({ email: '', password: '' })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  // ── Google Sign-In callback ──
  async function handleGoogleCallback(response) {
    setError('')
    setLoading(true)
    try {
      const { accessToken, refreshToken } = await loginWithGoogle(response.credential)
      saveLogin(accessToken, refreshToken)
      navigate('/chat')
    } catch (err) {
      setError(err.message || 'Đăng nhập Google thất bại')
    } finally {
      setLoading(false)
    }
  }

  // ── Khởi tạo Google Identity Services ──
  useEffect(() => {
    function initGoogle() {
      if (!window.google?.accounts?.id) return false

      window.google.accounts.id.initialize({
        client_id: GOOGLE_CLIENT_ID,
        callback: handleGoogleCallback,
      })

      window.google.accounts.id.renderButton(googleBtnRef.current, {
        theme: 'filled_black',
        size: 'large',
        width: '100%',
        text: 'signin_with',
        shape: 'rectangular',
        logo_alignment: 'left',
      })

      return true
    }

    // Google script có thể chưa load xong → retry
    if (!initGoogle()) {
      const interval = setInterval(() => {
        if (initGoogle()) clearInterval(interval)
      }, 200)
      return () => clearInterval(interval)
    }
  }, [])

  // ── Email/Password login ──
  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const { accessToken, refreshToken } = await login(form.email, form.password)
      saveLogin(accessToken, refreshToken)
      navigate('/chat')
    } catch (err) {
      setError(err.message || 'Đăng nhập thất bại')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.wrapper}>
      {/* Background blobs */}
      <div className={styles.blob1} />
      <div className={styles.blob2} />

      <div className={styles.card}>
        {/* Logo/Header */}
        <div className={styles.header}>
          <div className={styles.logoIcon}>🏠</div>
          <h1 className={styles.title}>Room Rental</h1>
          <p className={styles.subtitle}>Chat realtime với chủ nhà &amp; người thuê</p>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className={styles.form}>
          <div className={styles.field}>
            <label className={styles.label}>Email</label>
            <input
              id="login-email"
              type="email"
              className={styles.input}
              placeholder="example@gmail.com"
              value={form.email}
              onChange={e => setForm(f => ({ ...f, email: e.target.value }))}
              required
              autoComplete="email"
            />
          </div>

          <div className={styles.field}>
            <label className={styles.label}>Mật khẩu</label>
            <input
              id="login-password"
              type="password"
              className={styles.input}
              placeholder="••••••••"
              value={form.password}
              onChange={e => setForm(f => ({ ...f, password: e.target.value }))}
              required
              autoComplete="current-password"
            />
          </div>

          {error && (
            <div className={styles.error}>
              <span>⚠️</span> {error}
            </div>
          )}

          <button
            id="login-submit"
            type="submit"
            className={styles.btn}
            disabled={loading}
          >
            {loading ? <span className="spinner" /> : 'Đăng nhập'}
          </button>
        </form>

        {/* ── Divider ── */}
        <div className={styles.divider}>
          <span className={styles.dividerLine} />
          <span className={styles.dividerText}>hoặc</span>
          <span className={styles.dividerLine} />
        </div>

        {/* ── Google Sign-In Button ── */}
        <div className={styles.googleBtnWrapper}>
          <div ref={googleBtnRef} id="google-signin-btn" />
        </div>

        <p className={styles.hint}>
          Đăng nhập với tài khoản <strong>USER</strong> hoặc <strong>LANDLORD</strong>
        </p>
      </div>
    </div>
  )
}
