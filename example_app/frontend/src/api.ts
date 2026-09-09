import type {Basket, CheckoutPreview, Order, PageParams, Product, ShippingInfo} from './types'
import { getCookie } from './auth/cookies'

// In production (nginx), the UI must call same-origin:
// - /api/*  (proxied to backend)
// - /auth/* (proxied to auth service)
//
// In local dev (vite), you can still use these paths if you configure a dev proxy,
// or you can point the Vite dev server to backend/auth directly.
const API_BASE_URL = '/api'
const AUTH_BASE_URL = '/auth'

export class ApiRequestError extends Error {
  status: number

  constructor(status: number, message: string) {
    super(message)
    this.name = 'ApiRequestError'
    this.status = status
  }
}

type JsonFetchOptions = Omit<RequestInit, 'headers'> & {
  headers?: Record<string, string>
  includeAuth?: boolean
  baseUrl?: string
}

function authHeader(): Record<string, string> {
  const token = getCookie('access_token')
  if (!token) {
    return {}
  }
  return { Authorization: `Bearer ${token}` }
}

async function fetchJson<T>(path: string, options?: JsonFetchOptions): Promise<T> {
  const baseUrl = options?.baseUrl ?? API_BASE_URL
  const response = await fetch(`${baseUrl}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(options?.includeAuth ? authHeader() : {}),
      ...(options?.headers ?? {}),
    },
    ...options,
  })

  if (!response.ok) {
    let message = `Request failed: ${response.status}`
    try {
      const errorBody = (await response.json()) as { message?: string }
      if (errorBody.message) {
        message = errorBody.message
      }
    } catch {
      // Keep the default message when response is not JSON.
    }
    throw new ApiRequestError(response.status, message)
  }

  if (response.status === 204) {
    return undefined as T
  }
  return (await response.json()) as T
}

export function getProducts(params: PageParams): Promise<Product[]> {
  if (!params || params.page === undefined || params.size === undefined) {
    throw new Error('Pagination parameters (page and size) are required to fetch products.');
  }

  const queryParams = new URLSearchParams({
    page: params.page.toString(),
    size: params.size.toString()
  });

  // Only append search parameter if it's not empty
  if (params.search && params.search.trim()) {
    queryParams.append('search', params.search.trim());
  }

  return fetchJson<Product[]>(`/products?${queryParams.toString()}`);
}

export function getProduct(productId: string): Promise<Product> {
  return fetchJson<Product>(`/products/${productId}`)
}

export function getBasket(): Promise<Basket> {
  return fetchJson<Basket>('/basket', { includeAuth: true })
}

export function addBasketItem(productId: string, quantity: number): Promise<Basket> {
  return fetchJson<Basket>('/basket/items', {
    method: 'POST',
    includeAuth: true,
    body: JSON.stringify({ productId, quantity }),
  })
}

export function updateBasketItem(productId: string, quantity: number): Promise<Basket> {
  return fetchJson<Basket>(`/basket/items/${productId}`, {
    method: 'PATCH',
    includeAuth: true,
    body: JSON.stringify({ quantity }),
  })
}

export async function removeBasketItem(productId: string): Promise<Basket> {
  await fetchJson<void>(`/basket/items/${productId}`, { method: 'DELETE', includeAuth: true })
  return getBasket()
}

export function checkoutPreview(shippingInfo: ShippingInfo): Promise<CheckoutPreview> {
  return fetchJson<CheckoutPreview>('/checkout/preview', {
    method: 'POST',
    includeAuth: true,
    body: JSON.stringify({ shippingInfo }),
  })
}

export function placeOrder(shippingInfo: ShippingInfo): Promise<Order> {
  return fetchJson<Order>('/orders', {
    method: 'POST',
    includeAuth: true,
    body: JSON.stringify({ shippingInfo }),
  })
}

export function getOrder(orderId: string): Promise<Order> {
  return fetchJson<Order>(`/orders/${orderId}`, { includeAuth: true })
}

export function getOrders(): Promise<Order[]> {
  return fetchJson<Order[]>('/orders', { includeAuth: true })
}

export type AuthResponse = {
  userId: string
  email: string
  token: string
}

export function signUp(email: string, password: string): Promise<AuthResponse> {
  return fetchJson<AuthResponse>('/signup', {
    baseUrl: AUTH_BASE_URL,
    method: 'POST',
    body: JSON.stringify({ email, password }),
  })
}

export function signIn(email: string, password: string): Promise<AuthResponse> {
  return fetchJson<AuthResponse>('/signin', {
    baseUrl: AUTH_BASE_URL,
    method: 'POST',
    body: JSON.stringify({ email, password }),
  })
}
