'use client'

import { useState, useEffect } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'

export default function VerifyOTPPage() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const phoneNumber = searchParams.get('phoneNumber') || ''

  const [otp, setOtp] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isResending, setIsResending] = useState(false)
  const [message, setMessage] = useState('')
  const [canResend, setCanResend] = useState(false)
  const [countdown, setCountdown] = useState(120)

  useEffect(() => {
    let timer: NodeJS.Timeout

    if (countdown > 0 && !canResend) {
      timer = setTimeout(() => {
        setCountdown(prev => prev - 1)
      }, 1000)
    } else if (countdown === 0) {
      setCanResend(true)
    }

    return () => clearTimeout(timer)
  }, [countdown, canResend])

  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60)
    const secs = seconds % 60
    return `${mins}:${secs < 10 ? '0' : ''}${secs}`
  }

  const handleVerify = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!otp || otp.length !== 6) {
      setError('کد تأیید باید ۶ رقم باشد')
      return
    }

    setIsSubmitting(true)
    setError('')

    try {
      const response = await fetch('https://polaris-server-30ha.onrender.com/api/verify_otp/', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          phone_number: phoneNumber,
          otp_code: otp
        })
      })

      if (response.ok) {
        setMessage('حساب شما با موفقیت تأیید شد! در حال انتقال به صفحه ورود...')
        setTimeout(() => {
          router.push('/login')
        }, 2000)
      } else {
        const data = await response.json()
        setError(data.message || 'کد تأیید نامعتبر است')
      }
    } catch (err) {
      console.error('OTP verification error:', err)
      setError('خطایی در تأیید کد رخ داد. لطفاً دوباره تلاش کنید.')
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleResendOTP = async () => {
    if (!canResend) return

    setIsResending(true)
    setError('')
    setMessage('')

    try {
      const response = await fetch('https://polaris-server-30ha.onrender.com/api/request_otp/', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          phone_number: phoneNumber
        })
      })

      if (response.ok) {
        setMessage('کد جدید برای شما ارسال شد')
        setCanResend(false)
        setCountdown(120)
      } else {
        const data = await response.json()
        setError(data.message || 'خطا در ارسال مجدد کد')
      }
    } catch (err) {
      console.error('Resend OTP error:', err)
      setError('خطایی در ارسال مجدد کد رخ داد')
    } finally {
      setIsResending(false)
    }
  }

  if (!phoneNumber) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-background-from to-background-to text-foreground flex flex-col items-center justify-center p-4">
        <div className="bg-ocean-800/20 backdrop-blur-sm rounded-lg p-6 shadow-lg w-full max-w-md">
          <h1 className="text-2xl font-bold text-center mb-6">خطا</h1>
          <p className="text-center mb-6">شماره تلفن یافت نشد</p>
          <button
            onClick={() => router.push('/signup')}
            className="w-full bg-primary hover:bg-primary-hover text-primary-foreground py-3 px-6 rounded-lg text-lg font-medium transition-colors"
          >
            بازگشت به صفحه ثبت نام
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-background-from to-background-to text-foreground flex flex-col items-center justify-center p-4">
      <main className="w-full max-w-md">
        <div className="bg-ocean-800/20 backdrop-blur-sm rounded-lg p-6 shadow-lg">
          <button
            onClick={() => router.back()}
            className="text-ocean-400 hover:text-ocean-300 text-sm mb-4"
          >
            ← بازگشت
          </button>

          <h1 className="text-2xl font-bold text-center mb-6">تأیید شماره تلفن</h1>

          <p className="text-center mb-6">
            کد تأیید به شماره {phoneNumber} ارسال شد
          </p>

          {message && (
            <p className="text-green-400 text-center mb-4">{message}</p>
          )}

          <form onSubmit={handleVerify} className="space-y-4">
            <div>
              <label htmlFor="otp" className="block text-sm font-medium mb-2">
                کد تأیید (۶ رقم)
              </label>
              <input
                type="text"
                id="otp"
                name="otp"
                value={otp}
                onChange={(e) => {
                  const value = e.target.value.replace(/\D/g, '')
                  setOtp(value.slice(0, 6))
                }}
                className="w-full px-3 py-2 bg-ocean-700/30 border border-ocean-600/50 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent text-foreground placeholder-ocean-300 text-center"
                placeholder="------"
                maxLength={6}
                disabled={isSubmitting || isResending}
                dir="ltr"
              />
              {error && (
                <p className="text-red-400 text-sm mt-1 text-center">{error}</p>
              )}
            </div>

            <button
              type="submit"
              disabled={isSubmitting || isResending}
              className="w-full bg-primary hover:bg-primary-hover text-primary-foreground py-3 px-6 rounded-lg text-lg font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isSubmitting ? 'در حال تأیید...' : 'تأیید'}
            </button>
          </form>

          <div className="mt-4 text-center">
            <button
              onClick={handleResendOTP}
              disabled={!canResend || isResending || isSubmitting}
              className={`text-primary font-medium text-sm ${
                canResend ? 'hover:text-primary-hover' : 'text-ocean-400 cursor-not-allowed'
              } disabled:opacity-50`}
            >
              {canResend ? (
                isResending ? 'در حال ارسال مجدد...' : 'ارسال مجدد کد'
              ) : (
                `ارسال مجدد پس از ${formatTime(countdown)}`
              )}
            </button>
          </div>
        </div>
      </main>
    </div>
  )
}
