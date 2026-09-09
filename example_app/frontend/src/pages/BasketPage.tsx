import { Link, useNavigate } from 'react-router-dom'
import { useBasket } from '../context/BasketContext'
import { formatCurrency } from '../utils'

export function BasketPage() {
  const { basket, updateItem, removeItem, loading } = useBasket()
  const navigate = useNavigate()

  if (loading) {
    return <p>Loading basket...</p>
  }

  return (
    <section>
      <h1>Basket</h1>
      {basket.items.length === 0 ? (
        <div className="card">
          <p>Your basket is empty.</p>
          <Link to="/products" className="button">
            Go to products
          </Link>
        </div>
      ) : (
        <>
          <div className="list">
            {basket.items.map((item) => (
              <div className="list-row" key={item.productId}>
                <div>
                  <h3>{item.productName}</h3>
                  <p className="muted">{formatCurrency(item.unitPrice)} each</p>
                </div>
                <div className="row">
                  <input
                    className="input small"
                    type="number"
                    min={1}
                    value={item.quantity}
                    onChange={(event) => {
                      const quantity = Math.max(1, Number(event.target.value))
                      void updateItem(item.productId, quantity)
                    }}
                  />
                  <p>{formatCurrency(item.lineTotal)}</p>
                  <button className="button secondary" onClick={() => void removeItem(item.productId)}>
                    Remove
                  </button>
                </div>
              </div>
            ))}
          </div>

          <div className="summary">
            <p>
              <strong>Items:</strong> {basket.totalItems}
            </p>
            <p>
              <strong>Subtotal:</strong> {formatCurrency(basket.subtotal)}
            </p>
            <button className="button" onClick={() => navigate('/checkout')}>
              Proceed to checkout
            </button>
          </div>
        </>
      )}
    </section>
  )
}
