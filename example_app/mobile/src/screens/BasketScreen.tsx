import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { FlatList, Pressable, SafeAreaView, Text, TextInput, View } from 'react-native';
import { useBasket } from '../context/BasketContext';
import { styles } from '../styles';
import type { RootStackParamList } from '../types';
import { formatCurrency } from '../utils';

type Props = NativeStackScreenProps<RootStackParamList, 'Basket'>;

export function BasketScreen({ navigation }: Props) {
  const { basket, loading, updateItem, removeItem } = useBasket();

  if (loading) {
    return (
      <SafeAreaView style={styles.screen}>
        <Text>Loading basket...</Text>
      </SafeAreaView>
    );
  }

  if (basket.items.length === 0) {
    return (
      <SafeAreaView style={styles.screen}>
        <View style={styles.card}>
          <Text style={styles.subHeading}>Your basket is empty</Text>
          <Pressable style={styles.button} onPress={() => navigation.navigate('Products')}>
            <Text style={styles.buttonText}>Go to products</Text>
          </Pressable>
        </View>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.screen}>
      <FlatList
        data={basket.items}
        keyExtractor={(item) => item.productId}
        renderItem={({ item }) => (
          <View style={styles.card}>
            <Text style={styles.subHeading}>{item.productName}</Text>
            <Text style={styles.muted}>{formatCurrency(item.unitPrice)} each</Text>
            <Text style={styles.price}>{formatCurrency(item.lineTotal)}</Text>
            <TextInput
              style={styles.input}
              keyboardType="numeric"
              defaultValue={String(item.quantity)}
              onEndEditing={(event) => {
                const quantity = Math.max(1, Number(event.nativeEvent.text) || 1);
                void updateItem(item.productId, quantity);
              }}
            />
            <Pressable style={[styles.button, styles.buttonSecondary]} onPress={() => void removeItem(item.productId)}>
              <Text style={[styles.buttonText, styles.buttonTextSecondary]}>Remove</Text>
            </Pressable>
          </View>
        )}
        ListFooterComponent={
          <View style={styles.card}>
            <Text>
              <Text style={styles.price}>Items:</Text> {basket.totalItems}
            </Text>
            <Text style={{ marginBottom: 8 }}>
              <Text style={styles.price}>Subtotal:</Text> {formatCurrency(basket.subtotal)}
            </Text>
            <Pressable style={styles.button} onPress={() => navigation.navigate('Checkout')}>
              <Text style={styles.buttonText}>Proceed to checkout</Text>
            </Pressable>
          </View>
        }
      />
    </SafeAreaView>
  );
}
