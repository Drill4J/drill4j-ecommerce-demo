# Example E-commerce Mobile

React Native mobile app (Expo + TypeScript) for iOS and Android.

## Implemented screens

- Home
- Product list
- Product details
- Basket
- Checkout (shipping -> COD payment -> review)
- Order confirmation

## Run

From `example_app/mobile`:

```bash
npm install
npm start
```

Or directly:

```bash
npm run android
npm run ios
```

## Backend API base URL

Default API URL:

`http://localhost:8080/api/v1`

To override:

```bash
EXPO_PUBLIC_API_BASE_URL=http://<your-machine-ip>:8080/api/v1 npm start
```

Note: On physical devices and emulators, `localhost` usually points to the device itself, not your backend machine.

## Documentation

- Setup: `../docs/setup-guide.md`
- API: `../docs/api-reference.md`
- User flows: `../docs/user-flows.md`
