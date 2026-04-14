import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { login } from '../services/api'
import { useAuth } from '../context/AuthContext'
import styles from './LoginPage.module.css'

export default function LoginPage() {
  const { saveLogin } = useAuth()
  const navigate = useNavigate()

  const [form, setForm] = useState({ email: '', password: '' })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

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

        <p className={styles.hint}>
          Đăng nhập với tài khoản <strong>USER</strong> hoặc <strong>LANDLORD</strong>
        </p>
      </div>
    </div>
  )
}
