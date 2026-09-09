import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getProduct } from '../api'
import { useBasket } from '../context/BasketContext'
import type { Product } from '../types'
import { formatCurrency } from '../utils'

export function ProductDetailsPage() {
  const { productId = '' } = useParams()
  const [product, setProduct] = useState<Product | null>(null)
  const [quantity, setQuantity] = useState(1)
  const [error, setError] = useState('')
  const { addItem } = useBasket()

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

          <button className="button" onClick={handleAddToBasket}>
            Add to basket
          </button>
        </div>
      </div>
    </section>
  )
}
