export type Product = {
  id: string
  name: string
  description: string
  price: number
  imageUrl: string
  category: string
  inStock: boolean
}

export type PageParams = {
  page: number;
  size: number;
  search?: string; // Optional search query
  category?: string; // Optional exact category filter
}

export type BasketItem = {
  productId: string
  productName: string
  unitPrice: number
  quantity: number
  lineTotal: number
}

export type Basket = {
  items: BasketItem[]
  totalItems: number
  subtotal: number
}

export type ReorderResult = Basket & {
  skippedProductIds: string[]
}

export type WishlistItem = {
  productId: string
  productName: string
  unitPrice: number
  imageUrl: string
  category: string
  inStock: boolean
}

export type ShippingInfo = {
  fullName: string
  phoneNumber: string
  addressLine: string
  city: string
  postalCode: string
}

export type SavedAddress = ShippingInfo & {
  id: string
  isDefault: boolean
}

export type CheckoutPreview = {
  shippingInfo: ShippingInfo
  items: BasketItem[]
  totalAmount: number
  paymentMethod: string
  promoCode?: string | null
  discountAmount?: number | null
  deliveryFee?: number | null
}

export type Order = {
  orderId: string
  status: string
  shippingInfo: ShippingInfo
  items: BasketItem[]
  totalAmount: number
  paymentMethod: string
  promoCode?: string | null
  discountAmount?: number | null
  deliveryFee?: number | null
}
