# Zustand Store Architecture

## 📁 Folder Structure

```
store/
├── index.ts                 # Central export for all stores
├── auth-store.ts            # Authentication state management
└── auth-modal-store.ts      # Auth modal UI state management
```

## 🎯 Design Principles

### 1. **Separation of Concerns**

- Each store manages a specific domain (auth, modal, etc.)
- Stores are independent and don't depend on each other
- UI state and business logic are separated

### 2. **Middleware Usage**

- **devtools**: Enable Redux DevTools for debugging
- **persist**: Automatically persist state to localStorage (only for auth)

### 3. **Type Safety**

- Full TypeScript support
- Separate interfaces for State and Actions
- Combined type for the complete store

## 📦 Stores

### `auth-store.ts`

**Purpose**: Manage user authentication state

**State:**

- `isAuthenticated`: boolean - Whether user is logged in
- `isChecking`: boolean - Loading state during initial check

**Actions:**

- `checkAuth()`: Check authentication status from localStorage
- `login()`: Update state after successful login
- `logout()`: Clear authentication state
- `setIsChecking()`: Update checking state

**Persistence**:

- Persists only `isAuthenticated` to localStorage
- Key: `auth-storage`

### `auth-modal-store.ts`

**Purpose**: Manage authentication modal UI state

**State:**

- `showAuthModal`: "login" | "register" | null - Current modal mode

**Actions:**

- `setShowAuthModal(mode)`: Set modal mode directly
- `openLoginModal()`: Show login modal
- `openRegisterModal()`: Show register modal
- `closeModal()`: Hide modal

**Persistence**: None (UI state doesn't need persistence)

## 🔨 Usage Examples

### Basic Usage

```tsx
import { useAuthStore, useAuthModalStore } from "@/store";

function MyComponent() {
  // Select only what you need (prevents unnecessary re-renders)
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const login = useAuthStore((state) => state.login);

  const openLoginModal = useAuthModalStore((state) => state.openLoginModal);

  return (
    <div>
      {isAuthenticated ? (
        <p>Welcome!</p>
      ) : (
        <button onClick={openLoginModal}>Login</button>
      )}
    </div>
  );
}
```

### Multiple Selectors

```tsx
function Header() {
  // Multiple selectors - component re-renders only when these values change
  const { isAuthenticated, logout } = useAuthStore((state) => ({
    isAuthenticated: state.isAuthenticated,
    logout: state.logout,
  }));

  return (
    <header>
      {isAuthenticated && <button onClick={logout}>Logout</button>}
    </header>
  );
}
```

### Outside React Components

```tsx
import { useAuthStore } from "@/store";

// Get state outside components
const isAuthenticated = useAuthStore.getState().isAuthenticated;

// Subscribe to changes
const unsubscribe = useAuthStore.subscribe((state) => {
  console.log("Auth state changed:", state.isAuthenticated);
});

// Don't forget to unsubscribe
unsubscribe();
```

## 🚀 Initialization

The `StoreInitializer` component handles initial state setup:

```tsx
// components/store-initializer.tsx
export function StoreInitializer({ children }) {
  const checkAuth = useAuthStore((state) => state.checkAuth);

  useEffect(() => {
    checkAuth(); // Check auth on mount
  }, []);

  return <>{children}</>;
}
```

Usage in `layout.tsx`:

```tsx
<StoreInitializer>
  <RouteGuard>
    <Header />
    {children}
  </RouteGuard>
</StoreInitializer>
```

## 🔄 Migration from Context API

### Before (Context API)

```tsx
// Provider wrapper needed
<AuthProvider>
  <AuthModalProvider>
    <App />
  </AuthModalProvider>
</AuthProvider>;

// Usage in components
const { isAuthenticated } = useAuth();
const { openLoginModal } = useAuthModal();
```

### After (Zustand)

```tsx
// No provider wrapper needed! (except for initialization)
<StoreInitializer>
  <App />
</StoreInitializer>;

// Usage in components (same API, cleaner)
const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
const openLoginModal = useAuthModalStore((state) => state.openLoginModal);
```

## 🎨 Benefits

### 1. **Less Boilerplate**

- No Provider components needed
- No Context + useContext setup
- Direct imports and usage

### 2. **Better Performance**

- Fine-grained subscriptions (only re-render when selected state changes)
- No unnecessary re-renders from context updates

### 3. **DevTools Integration**

- Redux DevTools support out of the box
- Time-travel debugging
- State inspection

### 4. **Easier Testing**

```tsx
import { useAuthStore } from "@/store";

// Reset state between tests
beforeEach(() => {
  useAuthStore.setState({ isAuthenticated: false });
});

// Test actions
test("login updates auth state", () => {
  const { login } = useAuthStore.getState();
  login();
  expect(useAuthStore.getState().isAuthenticated).toBe(true);
});
```

### 5. **TypeScript Support**

- Full type inference
- Autocomplete for state and actions
- Compile-time type checking

## 📝 Best Practices

### 1. **Selector Optimization**

```tsx
// ❌ Bad - Re-renders on any state change
const store = useAuthStore();

// ✅ Good - Re-renders only when isAuthenticated changes
const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
```

### 2. **Action Placement**

```tsx
// ✅ Keep actions in the store
const login = useAuthStore((state) => state.login);

// ❌ Don't call actions during render
useAuthStore.getState().login(); // Only in event handlers/effects
```

### 3. **Store Slicing**

```tsx
// For large stores, use slices
import { create } from "zustand";

const createAuthSlice = (set) => ({
  isAuthenticated: false,
  login: () => set({ isAuthenticated: true }),
});

const createUISlice = (set) => ({
  theme: "dark",
  setTheme: (theme) => set({ theme }),
});

export const useStore = create((set) => ({
  ...createAuthSlice(set),
  ...createUISlice(set),
}));
```

## 🔍 Debugging

### Redux DevTools

1. Install Redux DevTools browser extension
2. Open DevTools
3. Look for store names: "AuthStore", "AuthModalStore"
4. Inspect state, actions, and time-travel

### Console Logging

```tsx
// Log all state changes
useAuthStore.subscribe((state) => {
  console.log("Auth state:", state);
});
```

## 🆚 Comparison with Other Solutions

| Feature        | Zustand   | Context API  | Redux          | Jotai     |
| -------------- | --------- | ------------ | -------------- | --------- |
| Bundle Size    | ~1KB      | 0 (built-in) | ~10KB          | ~3KB      |
| Boilerplate    | Minimal   | Medium       | High           | Minimal   |
| DevTools       | ✅        | ❌           | ✅             | ✅        |
| Persistence    | ✅        | Manual       | Via middleware | Via atoms |
| Learning Curve | Easy      | Easy         | Steep          | Medium    |
| Performance    | Excellent | Good         | Excellent      | Excellent |

## 📚 Resources

- [Zustand Documentation](https://zustand-demo.pmnd.rs/)
- [Zustand GitHub](https://github.com/pmndrs/zustand)
- [Best Practices Guide](https://github.com/pmndrs/zustand/wiki/Best-Practices)
