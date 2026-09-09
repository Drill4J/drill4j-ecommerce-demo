import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import * as api from '../api';
import type { Basket } from '../types';

type BasketContextValue = {
  basket: Basket;
  loading: boolean;
  refreshBasket: () => Promise<void>;
  addItem: (productId: string, quantity?: number) => Promise<void>;
  updateItem: (productId: string, quantity: number) => Promise<void>;
  removeItem: (productId: string) => Promise<void>;
};

const emptyBasket: Basket = {
  items: [],
  totalItems: 0,
  subtotal: 0,
};

const BasketContext = createContext<BasketContextValue | null>(null);

export function BasketProvider({ children }: { children: ReactNode }) {
  const [basket, setBasket] = useState<Basket>(emptyBasket);
  const [loading, setLoading] = useState(true);

  const refreshBasket = async () => {
    setLoading(true);
    try {
      setBasket(await api.getBasket());
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void refreshBasket();
  }, []);

  const addItem = async (productId: string, quantity = 1) => {
    setBasket(await api.addBasketItem(productId, quantity));
  };

  const updateItem = async (productId: string, quantity: number) => {
    setBasket(await api.updateBasketItem(productId, quantity));
  };

  const removeItem = async (productId: string) => {
    setBasket(await api.removeBasketItem(productId));
  };

  const value = useMemo(
    () => ({ basket, loading, refreshBasket, addItem, updateItem, removeItem }),
    [basket, loading],
  );

  return <BasketContext.Provider value={value}>{children}</BasketContext.Provider>;
}

export function useBasket() {
  const context = useContext(BasketContext);
  if (!context) {
    throw new Error('useBasket must be used inside BasketProvider');
  }
  return context;
}
