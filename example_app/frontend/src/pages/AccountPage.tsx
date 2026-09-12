import { FormEvent, useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { cancelOrder, changePassword, createAddress, getAddresses, getOrders, reorderOrder } from '../api'
import { useAuth } from '../context/AuthContext'
import { useBasket } from '../context/BasketContext'
import type { Order, SavedAddress, ShippingInfo } from '../types'
import { formatCurrency } from '../utils'

const emptyAddress: ShippingInfo = {
  fullName: '',
  phoneNumber: '',
  addressLine: '',
  city: '',
  postalCode: '',
}

export function AccountPage() {
  const navigate = useNavigate()
  const { user, signOut } = useAuth()
  const { refreshBasket } = useBasket()
  const [orders, setOrders] = useState<Order[]>([])
  const [addresses, setAddresses] = useState<SavedAddress[]>([])
  const [newAddress, setNewAddress] = useState<ShippingInfo>(emptyAddress)
  const [saveAsDefault, setSaveAsDefault] = useState(false)
  const [loading, setLoading] = useState(true)
  const [savingAddress, setSavingAddress] = useState(false)
  const [cancellingOrderId, setCancellingOrderId] = useState<string | null>(null)
  const [reorderingOrderId, setReorderingOrderId] = useState<string | null>(null)
  const [error, setError] = useState('')
  const [addressError, setAddressError] = useState('')
  const [addressMessage, setAddressMessage] = useState('')
  const [orderMessage, setOrderMessage] = useState('')
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [changingPassword, setChangingPassword] = useState(false)
  const [passwordError, setPasswordError] = useState('')

  useEffect(() => {
    setLoading(true)
    setError('')
    Promise.all([getOrders(), getAddresses()])
      .then(([orderList, addressList]) => {
        setOrders(orderList)
        setAddresses(addressList)
      })
      .catch((err: unknown) => {
        const message = err instanceof Error ? err.message : 'Failed to load account data'
        setError(message)
      })
      .finally(() => setLoading(false))
  }, [])

  const handleSaveAddress = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setAddressError('')
    setAddressMessage('')
    setSavingAddress(true)
    try {
      const created = await createAddress(newAddress, saveAsDefault || addresses.length === 0)
      setAddresses((current) => {
        const next = saveAsDefault
          ? current.map((address) => ({ ...address, isDefault: false }))
          : current
        return [...next, created]
      })
      setNewAddress(emptyAddress)
      setSaveAsDefault(false)
      setAddressMessage('Address saved.')
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to save address'
      setAddressError(message)
    } finally {
      setSavingAddress(false)
    }
  }

  const handleChangePassword = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setPasswordError('')
    setChangingPassword(true)
    try {
      await changePassword(currentPassword, newPassword)
      signOut()
      navigate('/signin', { replace: true, state: { from: '/products' } })
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to change password'
      setPasswordError(message)
    } finally {
      setChangingPassword(false)
    }
  }

  const handleCancelOrder = async (orderId: string) => {
    setOrderMessage('')
    setError('')
    setCancellingOrderId(orderId)
    try {
      const cancelled = await cancelOrder(orderId)
      setOrders((current) =>
        current.map((order) => (order.orderId === cancelled.orderId ? cancelled : order)),
      )
      setOrderMessage(`Order ${cancelled.orderId} cancelled.`)
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to cancel order'
      setError(message)
    } finally {
      setCancellingOrderId(null)
    }
  }

  const handleReorder = async (orderId: string) => {
    setOrderMessage('')
    setError('')
    setReorderingOrderId(orderId)
    try {
      const result = await reorderOrder(orderId)
      await refreshBasket()
      const skipped = result.skippedProductIds?.length ?? 0
      setOrderMessage(
        skipped > 0
          ? `Reordered into basket (${result.totalItems} items). Skipped ${skipped} out-of-stock item(s).`
          : `Reordered into basket (${result.totalItems} items).`,
      )
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to reorder'
      setError(message)
    } finally {
      setReorderingOrderId(null)
    }
  }

  return (
    <section>
      <h1>Account</h1>
      <div className="card">
        <p>
          <strong>Email:</strong> {user?.email ?? '—'}
        </p>
      </div>

      <h2>Change password</h2>
      {passwordError && <p className="error">{passwordError}</p>}
      <form className="card form" onSubmit={(event) => void handleChangePassword(event)}>
        <label>
          Current password
          <input
            className="input"
            required
            type="password"
            autoComplete="current-password"
            value={currentPassword}
            onChange={(event) => setCurrentPassword(event.target.value)}
          />
        </label>
        <label>
          New password
          <input
            className="input"
            required
            type="password"
            autoComplete="new-password"
            value={newPassword}
            onChange={(event) => setNewPassword(event.target.value)}
          />
        </label>
        <button className="button" type="submit" disabled={changingPassword}>
          {changingPassword ? 'Changing...' : 'Change password'}
        </button>
      </form>

      <h2>Saved shipping addresses</h2>
      {addressError && <p className="error">{addressError}</p>}
      {addressMessage && <p className="muted">{addressMessage}</p>}
      {addresses.length === 0 ? (
        <div className="card">
          <p>No saved addresses yet.</p>
        </div>
      ) : (
        <div className="list">
          {addresses.map((address) => (
            <div className="list-row" key={address.id}>
              <div>
                <h3>{address.fullName}</h3>
                <p className="muted">
                  {address.addressLine}, {address.city}, {address.postalCode}
                </p>
                <p className="muted">{address.phoneNumber}</p>
              </div>
              {address.isDefault && <span className="muted">Default</span>}
            </div>
          ))}
        </div>
      )}

      <form className="card form" onSubmit={(event) => void handleSaveAddress(event)}>
        <h3>Add address</h3>
        <label>
          Full name
          <input
            className="input"
            required
            value={newAddress.fullName}
            onChange={(event) => setNewAddress({ ...newAddress, fullName: event.target.value })}
          />
        </label>
        <label>
          Phone number
          <input
            className="input"
            required
            value={newAddress.phoneNumber}
            onChange={(event) => setNewAddress({ ...newAddress, phoneNumber: event.target.value })}
          />
        </label>
        <label>
          Address line
          <input
            className="input"
            required
            value={newAddress.addressLine}
            onChange={(event) => setNewAddress({ ...newAddress, addressLine: event.target.value })}
          />
        </label>
        <label>
          City
          <input
            className="input"
            required
            value={newAddress.city}
            onChange={(event) => setNewAddress({ ...newAddress, city: event.target.value })}
          />
        </label>
        <label>
          Postal code
          <input
            className="input"
            required
            value={newAddress.postalCode}
            onChange={(event) => setNewAddress({ ...newAddress, postalCode: event.target.value })}
          />
        </label>
        <label className="row">
          <input
            type="checkbox"
            checked={saveAsDefault}
            onChange={(event) => setSaveAsDefault(event.target.checked)}
          />
          Set as default
        </label>
        <button className="button" type="submit" disabled={savingAddress}>
          {savingAddress ? 'Saving...' : 'Save address'}
        </button>
      </form>

      <h2>Your orders</h2>
      {orderMessage && <p className="muted">{orderMessage}</p>}
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
                <button
                  className="button secondary"
                  type="button"
                  disabled={reorderingOrderId === order.orderId}
                  onClick={() => void handleReorder(order.orderId)}
                >
                  {reorderingOrderId === order.orderId ? 'Reordering...' : 'Reorder'}
                </button>
                {order.status === 'PLACED_COD' && (
                  <button
                    className="button secondary"
                    type="button"
                    disabled={cancellingOrderId === order.orderId}
                    onClick={() => void handleCancelOrder(order.orderId)}
                  >
                    {cancellingOrderId === order.orderId ? 'Cancelling...' : 'Cancel order'}
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </section>
  )
}
