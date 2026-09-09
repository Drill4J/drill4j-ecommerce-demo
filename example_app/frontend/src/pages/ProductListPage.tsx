import { useEffect, useMemo, useState, ChangeEvent } from 'react'
import { getProducts } from '../api'
import { ProductCard } from '../components/ProductCard'
import { useBasket } from '../context/BasketContext'
import type { Product } from '../types'

const DEFAULT_PAGE_SIZE = 8;

export function ProductListPage() {
  const [products, setProducts] = useState<Product[]>([])
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [error, setError] = useState('')
  const { addItem } = useBasket()

    // Fetch from backend whenever page OR search text changes
  useEffect(() => {
      getProducts({ page, size: DEFAULT_PAGE_SIZE, search })
          .then((data) => {
              setProducts(data)
              setError('')
          })
          .catch((err: unknown) => {
              const message = err instanceof Error ? err.message : 'Failed to load products'
              setError(message)
          })
  }, [page, search]) // Dependent on 'page' and 'search'

  const handleSearchChange = (event: ChangeEvent<HTMLInputElement>) => {
      setSearch(event.target.value)
      setPage(0) // search should always start results on page 1
  }

  const filteredProducts = useMemo(() => {
    if (!search.trim()) {
      return products
    }
    const normalizedSearch = search.toLowerCase()
    return products.filter((product) =>
      [product.name, product.description, product.category].join(' ').toLowerCase().includes(normalizedSearch),
    )
  }, [products, search])

  const handleAddToBasket = (productId: string) => {
    void addItem(productId, 1)
  }

    // Handle page switches safely
  const handlePrevPage = () => {
      setPage((prev) => Math.max(0, prev - 1))
  }

  const handleNextPage = () => {
      setPage((prev) => prev + 1)
  }

  const isPrevDisabled = page === 0
  const isNextDisabled = products.length < DEFAULT_PAGE_SIZE

  return (
    <section>
      <h1>Products</h1>
      <div className="row">
        <input
          value={search}
          onChange={handleSearchChange} // Uses reset handling method
          placeholder="Search by name, description, category"
          className="input"
        />
      </div>
      {error && <p className="error">{error}</p>}
      {!error && filteredProducts.length === 0 && <p>No products found.</p>}
      <div className="grid">
        {filteredProducts.map((product) => (
          <ProductCard key={product.id} product={product} onAddToBasket={handleAddToBasket} />
        ))}
      </div>
        {/* Pagination Controls Section using isolated classes */}
        <div className="pagination-container">
            {/* PREVIOUS BUTTON */}
            <button
                onClick={handlePrevPage}
                disabled={isPrevDisabled}
                // Uses isolated 'pagination-button' styles, appending 'secondary' when disabled
                className={isPrevDisabled ? 'pagination-button secondary' : 'pagination-button'}
            >
                Previous
            </button>

            {/* PAGE INDICATOR */}
            <span className="pagination-text">
          Page {page + 1}
        </span>

            {/* NEXT BUTTON */}
            <button
                onClick={handleNextPage}
                disabled={isNextDisabled}
                className={isNextDisabled ? 'pagination-button secondary' : 'pagination-button'}
            >
                Next
            </button>
        </div>
    </section>
  )
}
