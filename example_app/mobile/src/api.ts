import type { Basket, CheckoutPreview, Order, Product, ShippingInfo } from './types';

const API_BASE_URL = process.env.EXPO_PUBLIC_API_BASE_URL ?? 'http://localhost:8080/api/v1';

async function fetchJson<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });

  if (!response.ok) {
    let message = `Request failed: ${response.status}`;
    try {
      const body = (await response.json()) as { message?: string };
      if (body.message) {
        message = body.message;
      }
    } catch {
      // keep fallback message
    }
    throw new Error(message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

export const getProducts = () => fetchJson<Product[]>('/products');
export const getProduct = (productId: string) => fetchJson<Product>(`/products/${productId}`);
export const getBasket = () => fetchJson<Basket>('/basket');
export const addBasketItem = (productId: string, quantity: number) =>
  fetchJson<Basket>('/basket/items', { method: 'POST', body: JSON.stringify({ productId, quantity }) });
export const updateBasketItem = (productId: string, quantity: number) =>
  fetchJson<Basket>(`/basket/items/${productId}`, { method: 'PATCH', body: JSON.stringify({ quantity }) });

export async function removeBasketItem(productId: string): Promise<Basket> {
  await fetchJson<void>(`/basket/items/${productId}`, { method: 'DELETE' });
  return getBasket();
}

export const checkoutPreview = (shippingInfo: ShippingInfo) =>
  fetchJson<CheckoutPreview>('/checkout/preview', { method: 'POST', body: JSON.stringify({ shippingInfo }) });
export const placeOrder = (shippingInfo: ShippingInfo) =>
  fetchJson<Order>('/orders', { method: 'POST', body: JSON.stringify({ shippingInfo }) });
export const getOrder = (orderId: string) => fetchJson<Order>(`/orders/${orderId}`);
