import { Link } from 'react-router-dom'
import type { Product } from '../types'
import { formatCurrency } from '../utils'

type ProductCardProps = {
  product: Product
  onAddToBasket: (id: string) => void
}

export function ProductCard({ product, onAddToBasket }: ProductCardProps) {
  return (
    <article className="card product-card">
      <img src={product.imageUrl} alt={product.name} className="product-image" />
      <div className="product-body">
        <h3>{product.name}</h3>
        <p className="muted">{product.category}</p>
        <p className="price">{formatCurrency(product.price)}</p>
      </div>
      <div className="row">
        <Link to={`/products/${product.id}`} className="button secondary">
          Details
        </Link>
        <button className="button" onClick={() => onAddToBasket(product.id)}>
          Add to basket
        </button>
      </div>
    </article>
  )
}
