import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { useEffect, useState } from 'react';
import { FlatList, Pressable, SafeAreaView, Text, View } from 'react-native';
import { getProducts } from '../api';
import { useBasket } from '../context/BasketContext';
import { styles } from '../styles';
import type { Product, RootStackParamList } from '../types';
import { formatCurrency } from '../utils';

type Props = NativeStackScreenProps<RootStackParamList, 'Home'>;

export function HomeScreen({ navigation }: Props) {
  const [products, setProducts] = useState<Product[]>([]);
  const [error, setError] = useState('');
  const { basket, addItem } = useBasket();

  useEffect(() => {
    getProducts()
      .then((data) => setProducts(data.slice(0, 4)))
      .catch((err: unknown) => setError(err instanceof Error ? err.message : 'Failed to load products'));
  }, []);

  return (
    <SafeAreaView style={styles.screen}>
      <View style={styles.card}>
        <Text style={styles.heading}>Example Store</Text>
        <Text style={styles.muted}>Browse products and checkout with cash on delivery.</Text>
        <Pressable style={styles.button} onPress={() => navigation.navigate('Products')}>
          <Text style={styles.buttonText}>Browse products</Text>
        </Pressable>
        <Pressable style={styles.button} onPress={() => navigation.navigate('Basket')}>
          <Text style={styles.buttonText}>Open basket ({basket.totalItems})</Text>
        </Pressable>
      </View>

      <Text style={styles.subHeading}>Featured products</Text>
      {error ? <Text style={styles.error}>{error}</Text> : null}
      <FlatList
        data={products}
        keyExtractor={(item) => item.id}
        renderItem={({ item }) => (
          <View style={styles.card}>
            <Text style={styles.subHeading}>{item.name}</Text>
            <Text style={styles.muted}>{item.category}</Text>
            <Text style={styles.price}>{formatCurrency(item.price)}</Text>
            <View style={styles.row}>
              <Pressable style={[styles.button, styles.buttonSecondary]} onPress={() => navigation.navigate('ProductDetails', { productId: item.id })}>
                <Text style={[styles.buttonText, styles.buttonTextSecondary]}>Details</Text>
              </Pressable>
              <Pressable style={styles.button} onPress={() => void addItem(item.id, 1)}>
                <Text style={styles.buttonText}>Add</Text>
              </Pressable>
            </View>
          </View>
        )}
      />
    </SafeAreaView>
  );
}
