import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { StatusBar } from 'expo-status-bar';
import { BasketProvider } from './src/context/BasketContext';
import { BasketScreen } from './src/screens/BasketScreen';
import { CheckoutScreen } from './src/screens/CheckoutScreen';
import { HomeScreen } from './src/screens/HomeScreen';
import { OrderSuccessScreen } from './src/screens/OrderSuccessScreen';
import { ProductDetailsScreen } from './src/screens/ProductDetailsScreen';
import { ProductListScreen } from './src/screens/ProductListScreen';
import type { RootStackParamList } from './src/types';

const Stack = createNativeStackNavigator<RootStackParamList>();

export default function App() {
  return (
    <BasketProvider>
      <NavigationContainer>
        <StatusBar style="dark" />
        <Stack.Navigator screenOptions={{ headerBackTitle: 'Back' }}>
          <Stack.Screen name="Home" component={HomeScreen} options={{ title: 'Example Store' }} />
          <Stack.Screen name="Products" component={ProductListScreen} options={{ title: 'Products' }} />
          <Stack.Screen name="ProductDetails" component={ProductDetailsScreen} options={{ title: 'Product Details' }} />
          <Stack.Screen name="Basket" component={BasketScreen} options={{ title: 'Basket' }} />
          <Stack.Screen name="Checkout" component={CheckoutScreen} options={{ title: 'Checkout' }} />
          <Stack.Screen name="OrderSuccess" component={OrderSuccessScreen} options={{ title: 'Order Confirmation' }} />
        </Stack.Navigator>
      </NavigationContainer>
    </BasketProvider>
  );
}
