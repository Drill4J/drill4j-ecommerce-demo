# Non-Functional Requirements

## NFR-1 Technology Constraints

- Backend shall use Java and a mainstream framework (recommended: Spring Boot).
- Web frontend shall use React.
- Mobile app shall support both iOS and Android from a shared codebase.

## NFR-2 Performance (MVP Targets)

- Product list response time shall be under 500 ms for local development data set.
- Basket update operations shall be reflected in UI within 300 ms under normal local conditions.

## NFR-3 Reliability

- The system shall return clear error messages for invalid requests.
- Order creation shall be transactional: either order is fully created or not created.

## NFR-4 Usability

- UI flows shall be clear and consistent across web and mobile.
- Forms shall show validation errors near relevant fields.

## NFR-5 Security (MVP)

- Input validation shall be enforced at backend boundaries.
- CORS shall be configured to allow intended frontend/mobile clients.
- Sensitive secrets (e.g., JWT signing secrets) shall not be hardcoded in source files and shall be provided via environment variables.
- Authentication shall use JWT (Bearer tokens) for backend API access.
- The backend shall allow CORS preflight requests (HTTP `OPTIONS`) to protected endpoints without requiring authentication.

## NFR-6 Maintainability

- Code shall be organized by domain or feature.
- Basic automated tests shall exist for critical logic:
  - Basket operations.
  - Order creation.
- Auth and authorization behavior shall have basic automated coverage (e.g., happy path and unauthorized access).
- API contracts shall be documented (OpenAPI or equivalent).

## NFR-7 Observability

- Backend shall include structured logging for key events:
  - Basket updates.
  - Checkout attempts.
  - Order placement.

## NFR-8 Deployability

- Each app (backend, frontend, mobile) shall have setup instructions.
- Local environment shall be reproducible with documented run commands.
- Services shall be runnable independently (e.g., `backend` and `auth` as separate processes).
