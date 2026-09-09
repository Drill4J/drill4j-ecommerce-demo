import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { useState } from 'react';
import { Pressable, SafeAreaView, ScrollView, Text, TextInput, View } from 'react-native';
import { checkoutPreview, placeOrder } from '../api';
import { useBasket } from '../context/BasketContext';
import { styles } from '../styles';
import type { CheckoutPreview, RootStackParamList, ShippingInfo } from '../types';
import { formatCurrency } from '../utils';

type Props = NativeStackScreenProps<RootStackParamList, 'Checkout'>;
type Step = 1 | 2 | 3;

const initialShippingInfo: ShippingInfo = {
  fullName: '',
  phoneNumber: '',
  addressLine: '',
  city: '',
  postalCode: '',
};

export function CheckoutScreen({ navigation }: Props) {
  const { basket, refreshBasket } = useBasket();
  const [step, setStep] = useState<Step>(1);
  const [shippingInfo, setShippingInfo] = useState<ShippingInfo>(initialShippingInfo);
  const [preview, setPreview] = useState<CheckoutPreview | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleContinueToReview = async () => {
    setError('');
    try {
      const response = await checkoutPreview(shippingInfo);
      setPreview(response);
      setStep(3);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to preview checkout');
    }
  };

  const handlePlaceOrder = async () => {
    setLoading(true);
    setError('');
    try {
      const order = await placeOrder(shippingInfo);
      await refreshBasket();
      navigation.replace('OrderSuccess', { orderId: order.orderId });
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to place order');
    } finally {
      setLoading(false);
    }
  };

  if (basket.items.length === 0) {
    return (
      <SafeAreaView style={styles.screen}>
        <Text>Your basket is empty.</Text>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.screen}>
      <ScrollView>
        <Text style={styles.heading}>Checkout</Text>
        <Text style={styles.muted}>Step {step} of 3</Text>
        {error ? <Text style={styles.error}>{error}</Text> : null}

        {step === 1 ? (
          <View style={styles.card}>
            <Text style={styles.subHeading}>Shipping information</Text>
            <TextInput style={styles.input} placeholder="Full name" value={shippingInfo.fullName} onChangeText={(value) => setShippingInfo({ ...shippingInfo, fullName: value })} />
            <TextInput style={styles.input} placeholder="Phone number" value={shippingInfo.phoneNumber} onChangeText={(value) => setShippingInfo({ ...shippingInfo, phoneNumber: value })} />
            <TextInput style={styles.input} placeholder="Address line" value={shippingInfo.addressLine} onChangeText={(value) => setShippingInfo({ ...shippingInfo, addressLine: value })} />
            <TextInput style={styles.input} placeholder="City" value={shippingInfo.city} onChangeText={(value) => setShippingInfo({ ...shippingInfo, city: value })} />
            <TextInput style={styles.input} placeholder="Postal code" value={shippingInfo.postalCode} onChangeText={(value) => setShippingInfo({ ...shippingInfo, postalCode: value })} />
            <Pressable style={styles.button} onPress={() => setStep(2)}>
              <Text style={styles.buttonText}>Continue to payment</Text>
            </Pressable>
          </View>
        ) : null}

        {step === 2 ? (
          <View style={styles.card}>
            <Text style={styles.subHeading}>Payment method</Text>
            <Text style={{ marginBottom: 8 }}>Cash on Delivery (COD)</Text>
            <View style={styles.row}>
              <Pressable style={[styles.button, styles.buttonSecondary]} onPress={() => setStep(1)}>
                <Text style={[styles.buttonText, styles.buttonTextSecondary]}>Back</Text>
              </Pressable>
              <Pressable style={styles.button} onPress={() => void handleContinueToReview()}>
                <Text style={styles.buttonText}>Continue to review</Text>
              </Pressable>
            </View>
          </View>
        ) : null}

        {step === 3 && preview ? (
          <View style={styles.card}>
            <Text style={styles.subHeading}>Order review</Text>
            <Text>{preview.shippingInfo.fullName}</Text>
            <Text style={styles.muted}>
              {preview.shippingInfo.addressLine}, {preview.shippingInfo.city}, {preview.shippingInfo.postalCode}
            </Text>
            <Text style={{ marginBottom: 8 }}>Payment: {preview.paymentMethod}</Text>
            {preview.items.map((item) => (
              <View key={item.productId} style={styles.row}>
                <Text>{item.productName} x {item.quantity}</Text>
                <Text>{formatCurrency(item.lineTotal)}</Text>
              </View>
            ))}
            <Text style={[styles.price, { marginTop: 8 }]}>Total: {formatCurrency(preview.totalAmount)}</Text>
            <View style={styles.row}>
              <Pressable style={[styles.button, styles.buttonSecondary]} onPress={() => setStep(2)}>
                <Text style={[styles.buttonText, styles.buttonTextSecondary]}>Back</Text>
              </Pressable>
              <Pressable style={styles.button} onPress={() => void handlePlaceOrder()} disabled={loading}>
                <Text style={styles.buttonText}>{loading ? 'Placing...' : 'Place order'}</Text>
              </Pressable>
            </View>
          </View>
        ) : null}
      </ScrollView>
    </SafeAreaView>
  );
}
