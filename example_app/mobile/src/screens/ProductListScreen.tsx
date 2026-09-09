import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { useEffect, useMemo, useState } from 'react';
import { FlatList, Pressable, SafeAreaView, Text, TextInput, View } from 'react-native';
import { getProducts } from '../api';
import { useBasket } from '../context/BasketContext';
import { styles } from '../styles';
import type { Product, RootStackParamList } from '../types';
import { formatCurrency } from '../utils';

type Props = NativeStackScreenProps<RootStackParamList, 'Products'>;

export function ProductListScreen({ navigation }: Props) {
  const [products, setProducts] = useState<Product[]>([]);
  const [search, setSearch] = useState('');
  const [error, setError] = useState('');
  const { addItem } = useBasket();

  useEffect(() => {
    getProducts()
      .then(setProducts)
      .catch((err: unknown) => setError(err instanceof Error ? err.message : 'Failed to load products'));
  }, []);

  const filtered = useMemo(() => {
    if (!search.trim()) return products;
    const query = search.toLowerCase();
    return products.filter((p) => `${p.name} ${p.description} ${p.category}`.toLowerCase().includes(query));
  }, [products, search]);

  return (
    <SafeAreaView style={styles.screen}>
      <TextInput
        value={search}
        onChangeText={setSearch}
        placeholder="Search products"
        style={styles.input}
      />
      {error ? <Text style={styles.error}>{error}</Text> : null}
      <FlatList
        data={filtered}
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
                <Text style={styles.buttonText}>Add to basket</Text>
              </Pressable>
            </View>
          </View>
        )}
      />
    </SafeAreaView>
  );
}
