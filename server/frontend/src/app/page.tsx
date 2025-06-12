import Link from 'next/link'

export default function LandingPage() {
  return (
    <div className="min-h-screen bg-gradient-to-b from-background-from to-background-to text-foreground flex flex-col items-center justify-center p-4">
      <main className="w-full max-w-md text-center">
        <div className="flex flex-col space-y-4">
          <Link
            href="/signup"
            className="bg-primary hover:bg-primary-hover text-primary-foreground py-3 px-6 rounded-lg text-lg font-medium transition-colors"
          >
            ثبت نام
          </Link>

          <Link
            href="/login"
            className="bg-secondary hover:bg-secondary-hover text-secondary-foreground py-3 px-6 rounded-lg text-lg font-medium transition-colors"
          >
            ورود
          </Link>
        </div>
      </main>
    </div>
  )
}
