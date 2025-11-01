'use client'

import React from 'react'
import { useRouter } from 'next/navigation'
import { useAuth } from '@/context/AuthContext'
import Navigation from '@/components/Navigation'
import { FiMessageCircle } from 'react-icons/fi'

export default function ChatPage() {
  const router = useRouter()
  const { user } = useAuth()

  if (!user) {
    router.push('/login')
    return null
  }

  return (
    <div className="min-h-screen bg-lightGray pt-16">
      <Navigation />
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-white rounded-lg shadow-md p-8 text-center">
          <FiMessageCircle className="w-16 h-16 text-gray-400 mx-auto mb-4" />
          <h2 className="text-2xl font-bold text-darkGray mb-2">Chat Feature</h2>
          <p className="text-gray-600 mb-4">
            Real-time messaging functionality will be available here for communication between tenants, landlords, and contractors.
          </p>
          <p className="text-sm text-gray-500">
            This is a placeholder for the chat/messaging system.
          </p>
        </div>
      </div>
    </div>
  )
}

