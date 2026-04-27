"use client"

import { useEffect, useState } from "react"
import { Clock } from "lucide-react"

interface BookingTimerProps {
  expiredAt: string | null
  onExpired?: () => void
}

export default function BookingTimer({ expiredAt, onExpired }: BookingTimerProps) {
  const [timeLeft, setTimeLeft] = useState<number>(0)

  useEffect(() => {
    if (!expiredAt) return

    const calculateTimeLeft = () => {
      const now = new Date().getTime()
      const expiry = new Date(expiredAt).getTime()
      const diff = expiry - now

      if (diff <= 0) {
        setTimeLeft(0)
        onExpired?.()
        return 0
      }

      return Math.floor(diff / 1000) // Convert to seconds
    }

    // Calculate initial time
    setTimeLeft(calculateTimeLeft())

    // Update every second
    const timer = setInterval(() => {
      const remaining = calculateTimeLeft()
      setTimeLeft(remaining)

      if (remaining <= 0) {
        clearInterval(timer)
      }
    }, 1000)

    return () => clearInterval(timer)
  }, [expiredAt, onExpired])

  if (!expiredAt || timeLeft <= 0) return null

  const minutes = Math.floor(timeLeft / 60)
  const seconds = timeLeft % 60

  const isUrgent = timeLeft <= 120 // Less than 2 minutes

  return (
    <div className={`flex items-center gap-2 px-4 py-2 rounded-lg border ${
      isUrgent 
        ? 'bg-red-50 dark:bg-red-950/20 border-red-500 text-red-700 dark:text-red-400' 
        : 'bg-purple-50 dark:bg-purple-950/20 border-purple-500 text-purple-700 dark:text-purple-400'
    }`}>
      <Clock size={18} className={isUrgent ? 'animate-pulse' : ''} />
      <span className="font-semibold text-sm">
        {isUrgent ? 'Hurry up! ' : 'Time remaining: '}
        {minutes}:{seconds.toString().padStart(2, '0')}
      </span>
    </div>
  )
}
