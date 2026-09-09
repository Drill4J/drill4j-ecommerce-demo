import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import * as api from '../api'
import type { Basket } from '../types'
import { getCookie } from '../auth/cookies'

type BasketContextValue = {
  basket: Basket
  loading: boolean
  refreshBasket: () => Promise<void>
  addItem: (productId: string, quantity?: number) => Promise<void>
  updateItem: (productId: string, quantity: number) => Promise<void>
  removeItem: (productId: string) => Promise<void>
}

const emptyBasket: Basket = {
  items: [],
  totalItems: 0,
  subtotal: 0,
}

const BasketContext = createContext<BasketContextValue | null>(null)

export function BasketProvider({ children }: { children: ReactNode }) {
  const [basket, setBasket] = useState<Basket>(emptyBasket)
  const [loading, setLoading] = useState(true)
  const navigate = useNavigate()
  const location = useLocation()

  const redirectToSignIn = () => {
    navigate('/signin', { replace: true, state: { from: location.pathname } })
  }

  const isUnauthorized = (err: unknown) => err instanceof api.ApiRequestError && err.status === 401

  const refreshBasket = async () => {
    if (!getCookie('access_token')) {
      setBasket(emptyBasket)
      setLoading(false)
      return
    }
    setLoading(true)
    try {
      const freshBasket = await api.getBasket()
      setBasket(freshBasket)
    } catch (err: unknown) {
      if (isUnauthorized(err)) {
        redirectToSignIn()
      }
      // If token is missing/invalid, keep basket empty.
      setBasket(emptyBasket)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void refreshBasket()
  }, [])

  const addItem = async (productId: string, quantity = 1) => {
    try {
      const updated = await api.addBasketItem(productId, quantity)
      setBasket(updated)
    } catch (err: unknown) {
      if (isUnauthorized(err)) {
        redirectToSignIn()
        return
      }
      throw err
    }
  }

  const updateItem = async (productId: string, quantity: number) => {
    try {
      const updated = await api.updateBasketItem(productId, quantity)
      setBasket(updated)
    } catch (err: unknown) {
      if (isUnauthorized(err)) {
        redirectToSignIn()
        return
      }
      throw err
    }
  }

  const removeItem = async (productId: string) => {
    try {
      const updated = await api.removeBasketItem(productId)
      setBasket(updated)
    } catch (err: unknown) {
      if (isUnauthorized(err)) {
        redirectToSignIn()
        return
      }
      throw err
    }
  }

  const value = useMemo(
    () => ({
      basket,
      loading,
      refreshBasket,
      addItem,
      updateItem,
      removeItem,
    }),
    [basket, loading],
  )

  return <BasketContext.Provider value={value}>{children}</BasketContext.Provider>
}

export function useBasket() {
  const context = useContext(BasketContext)
  if (!context) {
    throw new Error('useBasket must be used inside BasketProvider')
  }
  return context
}
