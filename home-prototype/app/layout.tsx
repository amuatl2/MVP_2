import type { Metadata } from 'next'
import './globals.css'
import { AuthProvider } from '@/context/AuthContext'
import { DataProvider } from '@/context/DataContext'

export const metadata: Metadata = {
  title: 'HOME - Housing Operations & Maintenance Engine',
  description: 'MVP Prototype for HOME Platform',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en">
      <body>
        <AuthProvider>
          <DataProvider>
            {children}
          </DataProvider>
        </AuthProvider>
      </body>
    </html>
  )
}

