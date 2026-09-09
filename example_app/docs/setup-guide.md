# Setup Guide

## 1) Prerequisites

- [Java 17+](https://adoptium.net/temurin/releases/?version=17&os=any&arch=any)
- [Node.js 20+](https://nodejs.org/en/download)
- npm 10+

## 2) Start backend

From `example_app/backend`:

```bash
gradle bootRun
```

Backend runs on `http://localhost:8080` with API base path `/api/v1`.

## 3) Start web frontend

From `example_app/frontend`:

```bash
npm install
npm run dev
```

Web app default API base URL:

- `http://localhost:8080/api/v1`

Override with:

```bash
API_BASE_URL=http://localhost:8080/api/v1 npm run dev
```

## 4) Start mobile app (iOS/Android)

From `example_app/mobile`:

```bash
npm install
npm start
```

Or:

```bash
npm run android
npm run ios
```

Mobile app default API base URL:

- `http://localhost:8080/api/v1`

For physical devices or emulators, use your machine LAN IP:

```bash
EXPO_PUBLIC_API_BASE_URL=http://<your-machine-ip>:8080/api/v1 npm start
```

## 5) Recommended startup order

1. Start backend.
2. Start frontend and verify product list loads.
3. Start mobile and verify product list loads.

## 6) Quick sanity check

- `GET /api/v1/products` returns seeded products.
- Web home and products pages show data.
- Mobile home and products screens show data.
