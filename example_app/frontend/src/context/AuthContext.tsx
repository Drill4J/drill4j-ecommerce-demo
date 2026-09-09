import { createContext, useContext, useMemo, useState, type ReactNode } from 'react'
import * as api from '../api'
import { deleteCookie, getCookie, setCookie } from '../auth/cookies'
import { decodeJwtClaims } from '../auth/jwt'

type AuthUser = {
  userId: string
  email: string
}

type AuthContextValue = {
  user: AuthUser | null
  isAuthenticated: boolean
  signUp: (email: string, password: string) => Promise<void>
  signIn: (email: string, password: string) => Promise<void>
  signOut: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

function bootstrapUserFromToken(): AuthUser | null {
  const token = getCookie('access_token')
  if (!token) {
    return null
  }
  const claims = decodeJwtClaims(token)
  const userId = claims?.userId
  const email = claims?.email ?? claims?.sub
  if (!userId || !email) {
    return null
  }
  return { userId, email }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(() => bootstrapUserFromToken())

  const signUp = async (email: string, password: string) => {
    const result = await api.signUp(email, password)
    setCookie('access_token', result.token, { maxAgeDays: 1 })
    setUser({ userId: result.userId, email: result.email })
  }

  const signIn = async (email: string, password: string) => {
    const result = await api.signIn(email, password)
    setCookie('access_token', result.token, { maxAgeDays: 1 })
    setUser({ userId: result.userId, email: result.email })
  }

  const signOut = () => {
    deleteCookie('access_token')
    setUser(null)
  }

  const value = useMemo(
    () => ({
      user,
      isAuthenticated: Boolean(getCookie('access_token')),
      signUp,
      signIn,
      signOut,
    }),
    [user],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider')
  }
  return context
}

