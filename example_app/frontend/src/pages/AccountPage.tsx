import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getOrders } from '../api'
import { useAuth } from '../context/AuthContext'
import type { Order } from '../types'
import { formatCurrency } from '../utils'

export function AccountPage() {
  const { user } = useAuth()
  const [orders, setOrders] = useState<Order[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    setLoading(true)
    setError('')
    getOrders()
      .then(setOrders)
      .catch((err: unknown) => {
        const message = err instanceof Error ? err.message : 'Failed to load orders'
        setError(message)
      })
      .finally(() => setLoading(false))
  }, [])

  return (
    <section>
      <h1>Account</h1>
      <div className="card">
        <p>
          <strong>Email:</strong> {user?.email ?? '—'}
        </p>
      </div>

      <h2>Your orders</h2>
      {loading ? (
        <p>Loading orders...</p>
      ) : error ? (
        <p className="error">{error}</p>
      ) : orders.length === 0 ? (
        <div className="card">
          <p>You have no orders yet.</p>
          <Link to="/products" className="button">
            Browse products
          </Link>
        </div>
      ) : (
        <div className="list">
          {orders.map((order) => (
            <div className="list-row" key={order.orderId}>
              <div>
                <h3>Order {order.orderId}</h3>
                <p className="muted">
                  {order.status} · {order.paymentMethod}
                </p>
              </div>
              <div className="row">
                <span className="price">{formatCurrency(order.totalAmount)}</span>
                <Link className="button secondary" to={`/checkout/success/${order.orderId}`}>
                  View
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}
    </section>
  )
}

