import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getOrder } from '../api'
import type { Order } from '../types'
import { formatCurrency } from '../utils'

export function OrderSuccessPage() {
  const { orderId = '' } = useParams()
  const [order, setOrder] = useState<Order | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!orderId) {
      return
    }
    getOrder(orderId)
      .then(setOrder)
      .catch((err: unknown) => {
        const message = err instanceof Error ? err.message : 'Failed to load order'
        setError(message)
      })
  }, [orderId])

  if (error) {
    return <p className="error">{error}</p>
  }

  if (!order) {
    return <p>Loading order confirmation...</p>
  }

  return (
    <section className="card">
      <h1>Order confirmed</h1>
      <p>Your order has been placed successfully.</p>
      <p>
        <strong>Order ID:</strong> {order.orderId}
      </p>
      <p>
        <strong>Status:</strong> {order.status}
      </p>
      <p>
        <strong>Payment method:</strong> {order.paymentMethod}
      </p>
      <p>
        <strong>Total:</strong> {formatCurrency(order.totalAmount)}
      </p>
      <div className="row">
        <Link className="button" to="/products">
          Continue shopping
        </Link>
      </div>
    </section>
  )
}
