export type RootStackParamList = {
  Home: undefined;
  Products: undefined;
  ProductDetails: { productId: string };
  Basket: undefined;
  Checkout: undefined;
  OrderSuccess: { orderId: string };
};

export type Product = {
  id: string;
  name: string;
  description: string;
  price: number;
  imageUrl: string;
  category: string;
  inStock: boolean;
};

export type BasketItem = {
  productId: string;
  productName: string;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
};

export type Basket = {
  items: BasketItem[];
  totalItems: number;
  subtotal: number;
};

export type ShippingInfo = {
  fullName: string;
  phoneNumber: string;
  addressLine: string;
  city: string;
  postalCode: string;
};

export type CheckoutPreview = {
  shippingInfo: ShippingInfo;
  items: BasketItem[];
  totalAmount: number;
  paymentMethod: string;
};

export type Order = {
  orderId: string;
  status: string;
  shippingInfo: ShippingInfo;
  items: BasketItem[];
  totalAmount: number;
  paymentMethod: string;
};
