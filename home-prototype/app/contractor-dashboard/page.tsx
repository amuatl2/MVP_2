'use client'

import React, { useState } from 'react'
import { useRouter } from 'next/navigation'
import { useAuth } from '@/context/AuthContext'
import { useData } from '@/context/DataContext'
import Navigation from '@/components/Navigation'
import Link from 'next/link'
import { FiClock, FiCheckCircle, FiBriefcase } from 'react-icons/fi'

export default function ContractorDashboardPage() {
  const router = useRouter()
  const { user } = useAuth()
  const { jobs } = useData()
  const [activeTab, setActiveTab] = useState<'assigned' | 'completed'>('assigned')

  if (!user || user.role !== 'contractor') {
    router.push('/login')
    return null
  }

  const assignedJobs = jobs.filter(j => j.status !== 'completed')
  const completedJobs = jobs.filter(j => j.status === 'completed')

  return (
    <div className="min-h-screen bg-lightGray pt-16">
      <Navigation />
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-6">
          <h1 className="text-3xl font-bold text-darkGray mb-2">Contractor Dashboard</h1>
          <p className="text-gray-600">Manage your jobs and assignments</p>
        </div>

        <div className="bg-white rounded-lg shadow-md mb-6">
          <div className="flex border-b">
            <button
              onClick={() => setActiveTab('assigned')}
              className={`flex-1 px-6 py-4 font-medium transition-colors ${
                activeTab === 'assigned'
                  ? 'border-b-2 border-primary text-primary'
                  : 'text-gray-600 hover:text-darkGray'
              }`}
            >
              <div className="flex items-center justify-center space-x-2">
                <FiBriefcase className="w-4 h-4" />
                <span>Assigned Jobs ({assignedJobs.length})</span>
              </div>
            </button>
            <button
              onClick={() => setActiveTab('completed')}
              className={`flex-1 px-6 py-4 font-medium transition-colors ${
                activeTab === 'completed'
                  ? 'border-b-2 border-primary text-primary'
                  : 'text-gray-600 hover:text-darkGray'
              }`}
            >
              <div className="flex items-center justify-center space-x-2">
                <FiCheckCircle className="w-4 h-4" />
                <span>Completed Jobs ({completedJobs.length})</span>
              </div>
            </button>
          </div>

          <div className="p-6">
            {activeTab === 'assigned' ? (
              <div className="space-y-4">
                {assignedJobs.length > 0 ? (
                  assignedJobs.map((job) => (
                    <Link
                      key={job.id}
                      href={`/job/${job.id}`}
                      className="block p-4 border border-lightGray rounded-lg hover:bg-lightGray transition-colors"
                    >
                      <div className="flex justify-between items-start">
                        <div>
                          <p className="font-medium text-darkGray">{job.issueType}</p>
                          <p className="text-sm text-gray-600">{job.propertyAddress}</p>
                          <div className="flex items-center space-x-4 mt-2 text-sm text-gray-500">
                            <span className="flex items-center space-x-1">
                              <FiClock className="w-4 h-4" />
                              <span>Date: {job.date}</span>
                            </span>
                          </div>
                        </div>
                        <span className="px-3 py-1 bg-blue-100 text-blue-800 rounded text-sm">
                          {job.status}
                        </span>
                      </div>
                    </Link>
                  ))
                ) : (
                  <div className="text-center py-8">
                    <FiBriefcase className="w-12 h-12 text-gray-400 mx-auto mb-2" />
                    <p className="text-gray-600">No assigned jobs</p>
                  </div>
                )}
              </div>
            ) : (
              <div className="space-y-4">
                {completedJobs.length > 0 ? (
                  completedJobs.map((job) => (
                    <div
                      key={job.id}
                      className="p-4 border border-lightGray rounded-lg"
                    >
                      <div className="flex justify-between items-start">
                        <div>
                          <p className="font-medium text-darkGray">{job.issueType}</p>
                          <p className="text-sm text-gray-600">{job.propertyAddress}</p>
                          <p className="text-sm text-gray-500">Completed: {job.date}</p>
                        </div>
                        <span className="px-3 py-1 bg-green-100 text-green-800 rounded text-sm">
                          Completed
                        </span>
                      </div>
                    </div>
                  ))
                ) : (
                  <div className="text-center py-8">
                    <FiCheckCircle className="w-12 h-12 text-gray-400 mx-auto mb-2" />
                    <p className="text-gray-600">No completed jobs</p>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

