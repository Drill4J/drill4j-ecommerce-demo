import { useEffect, useMemo, useState, ChangeEvent } from 'react'
import { getProducts } from '../api'
import { ProductCard } from '../components/ProductCard'
import { useBasket } from '../context/BasketContext'
import type { Product } from '../types'

const DEFAULT_PAGE_SIZE = 8;

const CATEGORIES = [
  'Accessories',
  'Audio',
  'Fitness',
  'Gaming',
  'Home',
  'Office',
  'Photography',
  'Storage',
] as const;

export function ProductListPage() {
  const [products, setProducts] = useState<Product[]>([])
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [category, setCategory] = useState('')
  const [error, setError] = useState('')
  const { addItem } = useBasket()

  useEffect(() => {
      getProducts({
        page,
        size: DEFAULT_PAGE_SIZE,
        search: search || undefined,
        category: category || undefined,
      })
          .then((data) => {
              setProducts(data)
              setError('')
          })
          .catch((err: unknown) => {
              const message = err instanceof Error ? err.message : 'Failed to load products'
              setError(message)
          })
  }, [page, search, category])

  const handleSearchChange = (event: ChangeEvent<HTMLInputElement>) => {
      setSearch(event.target.value)
      setPage(0)
  }

  const handleCategorySelect = (next: string) => {
      setCategory((prev) => (prev === next ? '' : next))
      setPage(0)
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
          onChange={handleSearchChange}
          placeholder="Search by name, description, category"
          className="input"
        />
      </div>
      <div className="row category-filters" role="group" aria-label="Product categories">
        {CATEGORIES.map((name) => (
          <button
            key={name}
            type="button"
            className={category === name ? 'pagination-button' : 'pagination-button secondary'}
            aria-pressed={category === name}
            onClick={() => handleCategorySelect(name)}
          >
            {name}
          </button>
        ))}
      </div>
      {error && <p className="error">{error}</p>}
      {!error && filteredProducts.length === 0 && <p>No products found.</p>}
      <div className="grid">
        {filteredProducts.map((product) => (
          <ProductCard key={product.id} product={product} onAddToBasket={handleAddToBasket} />
        ))}
      </div>
        <div className="pagination-container">
            <button
                onClick={handlePrevPage}
                disabled={isPrevDisabled}
                className={isPrevDisabled ? 'pagination-button secondary' : 'pagination-button'}
            >
                Previous
            </button>
            <span className="pagination-text">
          Page {page + 1}
        </span>
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
