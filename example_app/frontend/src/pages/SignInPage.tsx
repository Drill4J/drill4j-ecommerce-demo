import { FormEvent, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

type LocationState = {
  from?: string
}

export function SignInPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { signIn } = useAuth()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  const from = ((location.state as LocationState | null)?.from ?? '/products') as string

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault()
    setSubmitting(true)
    setError('')
    try {
      await signIn(email, password)
      navigate(from, { replace: true })
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to sign in'
      setError(message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section>
      <h1>Sign in</h1>
      <p className="muted">Sign in to access your basket and orders.</p>
      {error && <p className="error">{error}</p>}

      <form className="card form" onSubmit={onSubmit}>
        <label>
          Email
          <input className="input" required type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
        </label>
        <label>
          Password
          <input
            className="input"
            required
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </label>
        <button className="button" type="submit" disabled={submitting}>
          {submitting ? 'Signing in...' : 'Sign in'}
        </button>
      </form>

      <p className="muted">
        No account yet? <Link to="/signup">Create one</Link>
      </p>
    </section>
  )
}

