'use client'

import { useState, useEffect } from 'react'
import { AlertCircle, X } from 'lucide-react'

export interface ErrorNotificationProps {
  title?: string
  message: string
  details?: string
  onClose?: () => void
  isVisible?: boolean
  showDetails?: boolean
  autoCloseDuration?: number
}

export function ErrorNotification({
  title = 'Error',
  message,
  details,
  onClose,
  isVisible = true,
  showDetails = false,
  autoCloseDuration = 5000,
}: ErrorNotificationProps) {
  const [shouldRender, setShouldRender] = useState(isVisible)
  const [isClosing, setIsClosing] = useState(false)

  useEffect(() => {
    if (isVisible) {
      setShouldRender(true)
      setIsClosing(false)
      
      // Auto-close after duration
      const autoCloseTimer = setTimeout(() => {
        handleClose()
      }, autoCloseDuration)

      return () => clearTimeout(autoCloseTimer)
    } else {
      setIsClosing(true)
      const timer = setTimeout(() => setShouldRender(false), 300)
      return () => clearTimeout(timer)
    }
  }, [isVisible, autoCloseDuration])

  const handleClose = () => {
    setIsClosing(true)
    setTimeout(() => {
      setShouldRender(false)
      onClose?.()
    }, 300)
  }

  if (!shouldRender) return null

  return (
    <div
      className={`transition-all duration-300 ease-out transform ${
        isClosing
          ? 'opacity-0 translate-x-full scale-95'
          : 'opacity-100 translate-x-0 scale-100'
      }`}
    >
      <div className="relative mb-4 shadow-lg rounded-lg border-l-4 border-l-red-600 bg-red-50 dark:bg-red-950/40 p-4 pr-10">
        <div className="flex items-start gap-4">
          <div className="mt-0.5 flex-shrink-0 animate-bounce">
            <AlertCircle className="h-5 w-5 text-red-600" />
          </div>
          <div className="flex-1 min-w-0">
            <h3 className="text-red-700 font-bold text-base">{title}</h3>
            <div className="mt-2">
              <p className="text-sm text-red-600 font-medium whitespace-normal break-words">{message}</p>
              {details && showDetails && (
                <details className="mt-3">
                  <summary className="cursor-pointer text-xs font-semibold hover:underline text-red-600">
                    More details
                  </summary>
                  <pre className="mt-2 overflow-auto rounded bg-red-100 dark:bg-red-900/50 p-2 text-xs text-red-700 max-h-48">
                    {details}
                  </pre>
                </details>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
