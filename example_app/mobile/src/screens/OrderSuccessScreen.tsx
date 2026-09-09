import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { useEffect, useState } from 'react';
import { Pressable, SafeAreaView, Text, View } from 'react-native';
import { getOrder } from '../api';
import { styles } from '../styles';
import type { Order, RootStackParamList } from '../types';
import { formatCurrency } from '../utils';

type Props = NativeStackScreenProps<RootStackParamList, 'OrderSuccess'>;

export function OrderSuccessScreen({ navigation, route }: Props) {
  const { orderId } = route.params;
  const [order, setOrder] = useState<Order | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    getOrder(orderId)
      .then(setOrder)
      .catch((err: unknown) => setError(err instanceof Error ? err.message : 'Failed to load order'));
  }, [orderId]);

  if (error) {
    return (
      <SafeAreaView style={styles.screen}>
        <Text style={styles.error}>{error}</Text>
      </SafeAreaView>
    );
  }

  if (!order) {
    return (
      <SafeAreaView style={styles.screen}>
        <Text>Loading order confirmation...</Text>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.screen}>
      <View style={styles.card}>
        <Text style={styles.heading}>Order confirmed</Text>
        <Text style={styles.muted}>Order ID: {order.orderId}</Text>
        <Text>Status: {order.status}</Text>
        <Text>Payment: {order.paymentMethod}</Text>
        <Text style={[styles.price, { marginTop: 6 }]}>Total: {formatCurrency(order.totalAmount)}</Text>
        <Pressable style={styles.button} onPress={() => navigation.navigate('Products')}>
          <Text style={styles.buttonText}>Continue shopping</Text>
        </Pressable>
      </View>
    </SafeAreaView>
  );
}
