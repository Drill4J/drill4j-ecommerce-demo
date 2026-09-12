import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { addBasketItem, getWishlist, removeWishlistItem } from '../api'
import { useBasket } from '../context/BasketContext'
import type { WishlistItem } from '../types'
import { formatCurrency } from '../utils'

export function WishlistPage() {
  const [items, setItems] = useState<WishlistItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const { refreshBasket } = useBasket()

  const loadWishlist = () => {
    setLoading(true)
    getWishlist()
      .then(setItems)
      .catch((err: unknown) => {
        const message = err instanceof Error ? err.message : 'Failed to load wishlist'
        setError(message)
      })
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    loadWishlist()
  }, [])

  const handleRemove = async (productId: string) => {
    try {
      await removeWishlistItem(productId)
      setItems((current) => current.filter((item) => item.productId !== productId))
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to remove wishlist item'
      setError(message)
    }
  }

  const handleMoveToBasket = async (productId: string) => {
    try {
      await addBasketItem(productId, 1)
      await removeWishlistItem(productId)
      await refreshBasket()
      setItems((current) => current.filter((item) => item.productId !== productId))
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to move item to basket'
      setError(message)
    }
  }

  if (loading) {
    return <p>Loading wishlist...</p>
  }

  return (
    <section>
      <h1>Wishlist</h1>
      {error ? <p className="error">{error}</p> : null}
      {items.length === 0 ? (
        <div className="card">
          <p>Your wishlist is empty.</p>
          <Link to="/products" className="button">
            Go to products
          </Link>
        </div>
      ) : (
        <div className="list">
          {items.map((item) => (
            <div className="list-row" key={item.productId}>
              <div>
                <h3>
                  <Link to={`/products/${item.productId}`}>{item.productName}</Link>
                </h3>
                <p className="muted">{item.category}</p>
                <p className="price">{formatCurrency(item.unitPrice)}</p>
                <p>{item.inStock ? 'In stock' : 'Out of stock'}</p>
              </div>
              <div className="row">
                <button className="button" onClick={() => void handleMoveToBasket(item.productId)}>
                  Move to basket
                </button>
                <button className="button secondary" onClick={() => void handleRemove(item.productId)}>
                  Remove
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </section>
  )
}
