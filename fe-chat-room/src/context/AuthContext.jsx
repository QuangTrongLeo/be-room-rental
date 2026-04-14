import { createContext, useContext, useState, useEffect } from 'react'
import { getMyProfile } from '../services/api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)       // { id, username, email, role, avatar }
  const [token, setToken] = useState(() => localStorage.getItem('accessToken'))
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (token) {
      getMyProfile()
        .then(profile => setUser(profile))
        .catch(() => logout())
        .finally(() => setLoading(false))
    } else {
      setLoading(false)
    }
  }, [token])

  function saveLogin(accessToken, refreshToken) {
    localStorage.setItem('accessToken', accessToken)
    localStorage.setItem('refreshToken', refreshToken)
    setToken(accessToken)
  }

  function logout() {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    setToken(null)
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, setUser, token, saveLogin, logout, loading }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
