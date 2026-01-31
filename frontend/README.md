# CRCS Frontend

React (Vite + TypeScript) UI for the Campus Resource Coordination System. Role-based access and full integration with the backend API Gateway.

## Setup

1. **Install dependencies**
   ```bash
   npm install
   ```

2. **Configure API URL**
   - Copy `.env.example` to `.env`
   - Set `VITE_API_BASE_URL` to your API Gateway URL (default: `http://localhost:6000`)
 

3**Run development server**
   ```bash
   npm run dev
   ```
   App runs at `http://localhost:5173` (or next available port).


In **development**, the app uses a **Vite proxy**: requests to `/auth`, `/users`, `/resources`, `/bookings` are sent to the same origin (localhost:5173), and Vite forwards them to the API Gateway (localhost:6000). So **CORS is not used** in dev and this error should not appear if the gateway is up.

1. **Ensure the API Gateway is running** on port 6000 (e.g. Postman can call `http://localhost:6000/auth/login`).
2. **Restart the Vite dev server** so the proxy is active: stop it (Ctrl+C) and run `npm run dev` again.
3. Open `http://localhost:5173`, go to login, and try again.

If the error persists, check the terminal where `npm run dev` is running for proxy errors (e.g. "ECONNREFUSED" means the gateway is not running on 6000).

## Build

```bash
npm run build
```

Output is in `dist/`. Serve with any static host or use `npm run preview` to preview the production build.

## Role-based access

- **USER**: Dashboard, browse resources, my bookings, create booking
- **RESOURCE_MANAGER** / **FACILITY_MANAGER**: + Manage resources (add/edit), Pending bookings (approve)
- **ADMIN**: + Users list and full access

Routes and nav items are gated by role. JWT is sent with every request; the gateway adds `X-User-Id` and `X-User-Role` for downstream services.

## Main routes

| Route | Description |
|-------|-------------|
| `/login` | Sign in |
| `/signup` | Register (choose role) |
| `/dashboard` | Home, recent bookings, quick links |
| `/resources` | List resources (filter by type) |
| `/resources/:id` | Resource detail, link to book |
| `/resources/manage/new` | Add resource (managers) |
| `/resources/manage/:id` | Edit resource (managers) |
| `/bookings` | My bookings |
| `/bookings/new` | Create booking |
| `/bookings/pending` | Pending bookings to approve (facility manager / admin) |
| `/users` | User list (admin only) |

## Tech stack

- **React 18** + **TypeScript**
- **Vite**
- **React Router 6**
- **Axios** (API client, JWT + refresh interceptor)
