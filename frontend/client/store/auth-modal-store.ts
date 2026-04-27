import { create } from "zustand";
import { devtools } from "zustand/middleware";

interface AuthModalState {
  showAuthModal: "login" | "register" | null;
}

interface AuthModalActions {
  setShowAuthModal: (mode: "login" | "register" | null) => void;
  openLoginModal: () => void;
  openRegisterModal: () => void;
  closeModal: () => void;
}

type AuthModalStore = AuthModalState & AuthModalActions;

export const useAuthModalStore = create<AuthModalStore>()(
  devtools(
    (set) => ({
      // Initial state
      showAuthModal: null,

      // Actions
      setShowAuthModal: (mode) => set({ showAuthModal: mode }),
      
      openLoginModal: () => set({ showAuthModal: "login" }),
      
      openRegisterModal: () => set({ showAuthModal: "register" }),
      
      closeModal: () => set({ showAuthModal: null }),
    }),
    {
      name: "AuthModalStore", // Name in Redux DevTools
    }
  )
);
