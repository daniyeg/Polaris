'use client'

import { useState, useEffect } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'

export default function LoginPage() {
  const router = useRouter()
  const [formData, setFormData] = useState({
    usernameOrPhone: '',
    password: ''
  })
  const [errors, setErrors] = useState({
    usernameOrPhone: '',
    password: ''
  })
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [apiError, setApiError] = useState('')

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      router.push('/dashboard');
    }
  }, [router]);

  const validateForm = () => {
    const newErrors = { usernameOrPhone: '', password: '' }
    let isValid = true

    if (!formData.usernameOrPhone.trim()) {
      newErrors.usernameOrPhone = 'نام کاربری یا شماره تلفن الزامی است'
      isValid = false
    }

    if (!formData.password) {
      newErrors.password = 'رمز عبور الزامی است'
      isValid = false
    }

    setErrors(newErrors)
    return isValid
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!validateForm()) return

    setIsSubmitting(true)
    setApiError('')

    try {
      const response = await fetch('https://polaris-server-30ha.onrender.com/api/login/', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          identifier: formData.usernameOrPhone.trim(),
          password: formData.password
        })
      })

      if (!response.ok) {
        const errorData = await response.json()
        throw new Error(JSON.stringify(errorData) || 'نام کاربری یا رمز عبور اشتباه است')
      }

      const data = await response.json()
      localStorage.setItem('token', data.token);

      router.push('/dashboard')
    } catch (error) {
      console.error('Login error:', error)
      setApiError(error instanceof Error ? error.message : 'خطایی در ورود رخ داد')
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))

    if (errors[name as keyof typeof errors]) {
      setErrors(prev => ({ ...prev, [name]: '' }))
    }

    if (apiError) {
      setApiError('')
    }
  }

  const isPhoneNumber = (value: string) => {
    return /^09\d/.test(value.trim())
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-background-from to-background-to text-foreground flex flex-col items-center justify-center p-4">
      <main className="w-full max-w-md">
        <div className="bg-ocean-800/20 backdrop-blur-sm rounded-lg p-6 shadow-lg">
          <h1 className="text-2xl font-bold text-center mb-6">ورود</h1>

          {apiError && (
            <div className="bg-red-500/20 border border-red-500/50 text-red-400 p-3 rounded-lg mb-4 text-sm">
              {apiError}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label htmlFor="usernameOrPhone" className="block text-sm font-medium mb-2">
                نام کاربری یا شماره تلفن
              </label>
              <input
                type="text"
                id="usernameOrPhone"
                name="usernameOrPhone"
                value={formData.usernameOrPhone}
                onChange={handleInputChange}
                className="w-full px-3 py-2 bg-ocean-700/30 border border-ocean-600/50 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent text-foreground placeholder-ocean-300"
                placeholder="نام کاربری یا 09123456789"
                disabled={isSubmitting}
                dir={isPhoneNumber(formData.usernameOrPhone) ? 'ltr' : 'rtl'}
              />
              {errors.usernameOrPhone && (
                <p className="text-red-400 text-sm mt-1">{errors.usernameOrPhone}</p>
              )}
            </div>

            <div>
              <label htmlFor="password" className="block text-sm font-medium mb-2">
                رمز عبور
              </label>
              <input
                type="password"
                id="password"
                name="password"
                value={formData.password}
                onChange={handleInputChange}
                className="w-full px-3 py-2 bg-ocean-700/30 border border-ocean-600/50 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent text-foreground placeholder-ocean-300"
                placeholder="رمز عبور خود را وارد کنید"
                disabled={isSubmitting}
              />
              {errors.password && (
                <p className="text-red-400 text-sm mt-1">{errors.password}</p>
              )}
            </div>

            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full bg-secondary hover:bg-secondary-hover text-secondary-foreground py-3 px-6 rounded-lg text-lg font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isSubmitting ? 'در حال ورود...' : 'ورود'}
            </button>
          </form>

          <div className="mt-6 text-center">
            <p className="text-ocean-300">
              حساب کاربری ندارید؟{' '}
              <Link href="/signup" className="text-primary hover:text-primary-hover font-medium">
                ثبت نام کنید
              </Link>
            </p>
          </div>

          <div className="mt-4 text-center">
            <Link href="/" className="text-ocean-400 hover:text-ocean-300 text-sm">
              بازگشت به صفحه اصلی
            </Link>
          </div>
        </div>
      </main>
    </div>
  )
}
