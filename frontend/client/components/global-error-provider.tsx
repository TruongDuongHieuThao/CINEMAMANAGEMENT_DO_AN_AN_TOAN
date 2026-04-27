'use client'

import { useEffect, useState } from 'react'
import { ErrorNotification } from '@/components/error-notification'

export interface GlobalErrorState {
  title?: string
  message: string
  details?: string
  showDetails?: boolean
}

// Create a simple event emitter for global error handling
class ErrorEmitter {
  private listeners: ((error: GlobalErrorState) => void)[] = []

  subscribe(callback: (error: GlobalErrorState) => void) {
    this.listeners.push(callback)
    return () => {
      this.listeners = this.listeners.filter((l) => l !== callback)
    }
  }

  emit(error: GlobalErrorState) {
    this.listeners.forEach((callback) => callback(error))
  }
}

export const errorEmitter = new ErrorEmitter()

export function GlobalErrorProvider() {
  const [error, setError] = useState<GlobalErrorState | null>(null)
  const [isVisible, setIsVisible] = useState(false)

  useEffect(() => {
    const unsubscribe = errorEmitter.subscribe((newError) => {
      setError(newError)
      setIsVisible(true)
    })

    return unsubscribe
  }, [])

  const handleClose = () => {
    setIsVisible(false)
    setTimeout(() => setError(null), 300)
  }

  return (
    <>
      {error && (
        <div className="fixed top-4 right-4 z-50 max-w-md">
          <ErrorNotification
            title={error.title}
            message={error.message}
            details={error.details}
            onClose={handleClose}
            isVisible={isVisible}
            showDetails={error.showDetails}
          />
        </div>
      )}
    </>
  )
}

// Helper function to show global errors
export function showGlobalError(error: GlobalErrorState | string) {
  const errorData = typeof error === 'string'
    ? { message: error }
    : error

  errorEmitter.emit(errorData)
}
