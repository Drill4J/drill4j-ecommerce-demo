import { FormEvent, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export function SignUpPage() {
  const navigate = useNavigate()
  const { signUp } = useAuth()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault()
    setSubmitting(true)
    setError('')
    try {
      await signUp(email, password)
      navigate('/products', { replace: true })
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to sign up'
      setError(message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section>
      <h1>Create account</h1>
      <p className="muted">This demo stores users in memory in the auth service.</p>
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
          {submitting ? 'Creating...' : 'Sign up'}
        </button>
      </form>

      <p className="muted">
        Already have an account? <Link to="/signin">Sign in</Link>
      </p>
    </section>
  )
}

