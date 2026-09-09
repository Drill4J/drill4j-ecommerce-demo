import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { useEffect, useState } from 'react';
import { Pressable, SafeAreaView, Text, TextInput, View } from 'react-native';
import { getProduct } from '../api';
import { useBasket } from '../context/BasketContext';
import { styles } from '../styles';
import type { Product, RootStackParamList } from '../types';
import { formatCurrency } from '../utils';

type Props = NativeStackScreenProps<RootStackParamList, 'ProductDetails'>;

export function ProductDetailsScreen({ route }: Props) {
  const { productId } = route.params;
  const [product, setProduct] = useState<Product | null>(null);
  const [quantity, setQuantity] = useState('1');
  const [error, setError] = useState('');
  const { addItem } = useBasket();

  useEffect(() => {
    getProduct(productId)
      .then(setProduct)
      .catch((err: unknown) => setError(err instanceof Error ? err.message : 'Failed to load product'));
  }, [productId]);

  if (error) {
    return (
      <SafeAreaView style={styles.screen}>
        <Text style={styles.error}>{error}</Text>
      </SafeAreaView>
    );
  }

  if (!product) {
    return (
      <SafeAreaView style={styles.screen}>
        <Text>Loading product...</Text>
      </SafeAreaView>
    );
  }

  const safeQuantity = Math.max(1, Number(quantity) || 1);

  return (
    <SafeAreaView style={styles.screen}>
      <View style={styles.card}>
        <Text style={styles.heading}>{product.name}</Text>
        <Text style={styles.muted}>{product.category}</Text>
        <Text>{product.description}</Text>
        <Text style={styles.price}>{formatCurrency(product.price)}</Text>
        <Text style={styles.muted}>{product.inStock ? 'In stock' : 'Out of stock'}</Text>
        <TextInput
          style={styles.input}
          keyboardType="numeric"
          value={quantity}
          onChangeText={setQuantity}
          placeholder="Quantity"
        />
        <Pressable style={styles.button} onPress={() => void addItem(product.id, safeQuantity)}>
          <Text style={styles.buttonText}>Add to basket</Text>
        </Pressable>
      </View>
    </SafeAreaView>
  );
}
