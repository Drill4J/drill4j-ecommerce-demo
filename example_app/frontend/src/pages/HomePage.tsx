import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getFeaturedProducts } from '../api'
import { ProductCard } from '../components/ProductCard'
import { useBasket } from '../context/BasketContext'
import type { Product } from '../types'

export function HomePage() {
  const [products, setProducts] = useState<Product[]>([])
  const [error, setError] = useState('')
  const { addItem } = useBasket()

  useEffect(() => {
    getFeaturedProducts()
      .then((data) => setProducts(data))
      .catch((err: unknown) => {
        const message = err instanceof Error ? err.message : 'Failed to load products'
        setError(message)
      })
  }, [])

  const handleAddToBasket = (productId: string) => {
    void addItem(productId, 1)
  }

  return (
    <section>
      <div className="hero">
        <h1>Shop everyday essentials</h1>
        <p>Discover quality products and place your order with easy cash-on-delivery checkout.</p>
        <div className="row">
          <Link className="button" to="/products">
            Browse products
          </Link>
          <Link className="button secondary" to="/basket">
            Open basket
          </Link>
        </div>
      </div>

      <h2>Featured products</h2>
      {error && <p className="error">{error}</p>}
      <div className="grid">
        {products.map((product) => (
          <ProductCard key={product.id} product={product} onAddToBasket={handleAddToBasket} />
        ))}
      </div>
    </section>
  )
}
