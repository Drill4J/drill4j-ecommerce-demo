import { Link, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useBasket } from '../context/BasketContext'

export function Layout() {
  const { basket } = useBasket()
  const { isAuthenticated, signOut, user } = useAuth()

  return (
    <div className="app-shell">
      <header className="header">
        <div className="container nav">
          <Link to="/" className="brand">
            Example Store
          </Link>
          <nav className="nav-links">
            <Link to="/">Home</Link>
            <Link to="/products">Products</Link>
            <Link to="/wishlist">Wishlist</Link>
            <Link to="/basket">Basket ({basket.totalItems})</Link>
            {isAuthenticated ? (
              <>
                <Link to="/account">{user?.email ?? 'Account'}</Link>
                <button className="button secondary" type="button" onClick={signOut}>
                  Sign out
                </button>
              </>
            ) : (
              <Link to="/signin">Sign in</Link>
            )}
          </nav>
        </div>
      </header>

      <main className="container content">
        <Outlet />
      </main>
    </div>
  )
}
