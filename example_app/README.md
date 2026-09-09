# Example E-commerce App

Example full-stack e-commerce app with:

- Java backend API (Spring Boot + Gradle)
- React web frontend (Vite + TypeScript)
- React Native mobile app for iOS and Android (Expo + TypeScript)

Checkout supports Cash on Delivery (COD) only.

## Quick Start

1) Start backend

```bash
cd backend
gradle bootRun
```

1) Start web frontend

```bash
cd frontend
npm install
npm run dev
```

1) Start mobile app

```bash
cd mobile
npm install
npm start
```

## Project Modules

- `requirements` - MVP requirements and scope
- `docs` - setup, API, and user-flow documentation
- `backend` - Java REST API
- `frontend` - React web app
- `mobile` - React Native mobile app

## Documentation Index

- Requirements: `requirements/README.md`
- Functional requirements: `requirements/functional-requirements.md`
- Non-functional requirements: `requirements/non-functional-requirements.md`
- Setup guide: `docs/setup-guide.md`
- API reference: `docs/api-reference.md`
- User flows: `docs/user-flows.md`

## API Base URL

- Default backend URL: `http://localhost:8080/api/v1`
- Web override: `API_BASE_URL`
- Mobile override: `EXPO_PUBLIC_API_BASE_URL`
