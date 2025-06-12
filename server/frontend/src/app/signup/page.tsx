'use client'

import { useState } from 'react'
import Link from 'next/link'

export default function SignupPage() {
  const [formData, setFormData] = useState({
    username: '',
    phoneNumber: '',
    password: '',
    confirmPassword: ''
  })
  const [errors, setErrors] = useState({
    username: '',
    phoneNumber: '',
    password: '',
    confirmPassword: ''
  })
  const [isSubmitting, setIsSubmitting] = useState(false)

  const validateForm = () => {
    const newErrors = { username: '', phoneNumber: '', password: '', confirmPassword: '' }
    let isValid = true

    if (!formData.username.trim()) {
      newErrors.username = 'نام کاربری الزامی است'
      isValid = false
    } else if (formData.username.length < 3) {
      newErrors.username = 'نام کاربری باید حداقل ۳ کاراکتر باشد'
      isValid = false
    }

    if (!formData.phoneNumber.trim()) {
      newErrors.phoneNumber = 'شماره تلفن الزامی است'
      isValid = false
    } else if (!/^09\d{9}$/.test(formData.phoneNumber)) {
      newErrors.phoneNumber = 'شماره تلفن معتبر نیست (مثال: 09123456789)'
      isValid = false
    }

    if (!formData.password) {
      newErrors.password = 'رمز عبور الزامی است'
      isValid = false
    } else if (formData.password.length < 6) {
      newErrors.password = 'رمز عبور باید حداقل ۶ کاراکتر باشد'
      isValid = false
    }

    if (!formData.confirmPassword) {
      newErrors.confirmPassword = 'تکرار رمز عبور الزامی است'
      isValid = false
    } else if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = 'رمز عبور و تکرار آن یکسان نیستند'
      isValid = false
    }

    setErrors(newErrors)
    return isValid
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!validateForm()) return

    setIsSubmitting(true)

    try {
      console.log('Signup data:', formData)
      await new Promise(resolve => setTimeout(resolve, 1000))
      alert('ثبت نام با موفقیت انجام شد!')

    } catch (error) {
      console.error('Signup error:', error)
      alert('خطایی در ثبت نام رخ داد. لطفاً دوباره تلاش کنید.')
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
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-background-from to-background-to text-foreground flex flex-col items-center justify-center p-4">
      <main className="w-full max-w-md">
        <div className="bg-ocean-800/20 backdrop-blur-sm rounded-lg p-6 shadow-lg">
          <h1 className="text-2xl font-bold text-center mb-6">ثبت نام</h1>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label htmlFor="username" className="block text-sm font-medium mb-2">
                نام کاربری
              </label>
              <input
                type="text"
                id="username"
                name="username"
                value={formData.username}
                onChange={handleInputChange}
                className="w-full px-3 py-2 bg-ocean-700/30 border border-ocean-600/50 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent text-foreground placeholder-ocean-300"
                placeholder="نام کاربری خود را وارد کنید"
                disabled={isSubmitting}
              />
              {errors.username && (
                <p className="text-red-400 text-sm mt-1">{errors.username}</p>
              )}
            </div>

            <div>
              <label htmlFor="phoneNumber" className="block text-sm font-medium mb-2">
                شماره تلفن
              </label>
              <input
                type="tel"
                id="phoneNumber"
                name="phoneNumber"
                value={formData.phoneNumber}
                onChange={handleInputChange}
                className="w-full px-3 py-2 bg-ocean-700/30 border border-ocean-600/50 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent text-foreground placeholder-ocean-300"
                placeholder="09123456789"
                disabled={isSubmitting}
                dir="ltr"
              />
              {errors.phoneNumber && (
                <p className="text-red-400 text-sm mt-1">{errors.phoneNumber}</p>
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

            <div>
              <label htmlFor="confirmPassword" className="block text-sm font-medium mb-2">
                تکرار رمز عبور
              </label>
              <input
                type="password"
                id="confirmPassword"
                name="confirmPassword"
                value={formData.confirmPassword}
                onChange={handleInputChange}
                className="w-full px-3 py-2 bg-ocean-700/30 border border-ocean-600/50 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent text-foreground placeholder-ocean-300"
                placeholder="رمز عبور را مجدداً وارد کنید"
                disabled={isSubmitting}
              />
              {errors.confirmPassword && (
                <p className="text-red-400 text-sm mt-1">{errors.confirmPassword}</p>
              )}
            </div>

            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full bg-primary hover:bg-primary-hover text-primary-foreground py-3 px-6 rounded-lg text-lg font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isSubmitting ? 'در حال ثبت نام...' : 'ثبت نام'}
            </button>
          </form>

          <div className="mt-6 text-center">
            <p className="text-ocean-300">
              حساب کاربری دارید؟{' '}
              <Link href="/login" className="text-primary hover:text-primary-hover font-medium">
                وارد شوید
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
