import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import { addWishlistItem, getProduct } from '../api'
import { useAuth } from '../context/AuthContext'
import { useBasket } from '../context/BasketContext'
import type { Product } from '../types'
import { formatCurrency } from '../utils'

export function ProductDetailsPage() {
  const { productId = '' } = useParams()
  const [product, setProduct] = useState<Product | null>(null)
  const [quantity, setQuantity] = useState(1)
  const [error, setError] = useState('')
  const [wishlistMessage, setWishlistMessage] = useState('')
  const { addItem } = useBasket()
  const { isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  useEffect(() => {
    if (!productId) {
      return
    }
    getProduct(productId)
      .then(setProduct)
      .catch((err: unknown) => {
        const message = err instanceof Error ? err.message : 'Failed to load product'
        setError(message)
      })
  }, [productId])

  const handleAddToBasket = () => {
    if (!product) return
    void addItem(product.id, quantity)
  }

  const handleAddToWishlist = async () => {
    if (!product) return
    if (!isAuthenticated) {
      navigate('/signin', { replace: true, state: { from: location.pathname } })
      return
    }
    try {
      setWishlistMessage('')
      await addWishlistItem(product.id)
      setWishlistMessage('Added to wishlist')
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to add to wishlist'
      setWishlistMessage(message)
    }
  }

  if (error) {
    return <p className="error">{error}</p>
  }

  if (!product) {
    return <p>Loading product...</p>
  }

  return (
    <section>
      <div className="row">
        <Link to="/products">Back to products</Link>
      </div>
      <div className="details">
        <img src={product.imageUrl} alt={product.name} className="product-image large" />
        <div>
          <h1>{product.name}</h1>
          <p className="muted">{product.category}</p>
          <p>{product.description}</p>
          <p className="price">{formatCurrency(product.price)}</p>
          <p>{product.inStock ? 'In stock' : 'Out of stock'}</p>

          <div className="row">
            <label htmlFor="quantity">Quantity</label>
            <input
              id="quantity"
              className="input small"
              type="number"
              min={1}
              value={quantity}
              onChange={(event) => setQuantity(Math.max(1, Number(event.target.value)))}
            />
          </div>

          <div className="row">
            <button className="button" onClick={handleAddToBasket}>
              Add to basket
            </button>
            <button className="button secondary" onClick={() => void handleAddToWishlist()}>
              Add to wishlist
            </button>
          </div>
          {wishlistMessage ? <p className="muted">{wishlistMessage}</p> : null}
        </div>
      </div>
    </section>
  )
}
