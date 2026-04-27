'use client'

import { useState, useCallback } from 'react'
import { useToast } from '@/components/ui/use-toast'

export interface ErrorInfo {
  title?: string
  message: string
  details?: string
}

export function useErrorNotification() {
  const [error, setError] = useState<ErrorInfo | null>(null)
  const [isVisible, setIsVisible] = useState(false)
  const { toast } = useToast()

  const showError = useCallback((errorInfo: ErrorInfo | string) => {
    const errorData = typeof errorInfo === 'string' 
      ? { message: errorInfo } 
      : errorInfo

    setError(errorData)
    setIsVisible(true)

    // Auto hide after 5 seconds (optional)
    const timer = setTimeout(() => {
      setIsVisible(false)
    }, 5000)

    return () => clearTimeout(timer)
  }, [])

  const showErrorToast = useCallback((errorInfo: ErrorInfo | string) => {
    const errorData = typeof errorInfo === 'string' 
      ? { message: errorInfo } 
      : errorInfo

    toast({
      title: errorData.title || 'Error',
      description: errorData.message,
      variant: 'destructive',
    })
  }, [toast])

  const clearError = useCallback(() => {
    setIsVisible(false)
    setError(null)
  }, [])

  return {
    error,
    isVisible,
    showError,
    showErrorToast,
    clearError,
  }
}
