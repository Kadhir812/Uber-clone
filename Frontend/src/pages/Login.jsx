import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Car, User } from 'lucide-react'
import Card from '../components/shared/Card'
import Button from '../components/shared/Button'
import Input from '../components/shared/Input'
import DarkModeToggle from '../components/shared/DarkModeToggle'

const Login = () => {
  const [selectedRole, setSelectedRole] = useState(null)
  const [userId, setUserId] = useState('')
  const [userName, setUserName] = useState('')
  const [error, setError] = useState('')
  const navigate = useNavigate()
  const { login } = useAuth()

  const handleRoleSelect = (role) => {
    setSelectedRole(role)
    setError('')
  }

  const handleLogin = (e) => {
    e.preventDefault()
    
    if (!userId.trim()) {
      setError('Please enter a user ID')
      return
    }

    const userData = {
      id: parseInt(userId),
      name: userName.trim() || `${selectedRole.charAt(0).toUpperCase() + selectedRole.slice(1)} ${userId}`,
      role: selectedRole,
    }

    login(userData)
    navigate(selectedRole === 'rider' ? '/rider' : '/driver')
  }

  const handleBack = () => {
    setSelectedRole(null)
    setUserId('')
    setUserName('')
    setError('')
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-50 via-white to-blue-50 dark:from-gray-900 dark:via-gray-900 dark:to-gray-800 flex items-center justify-center p-4">
      <div className="absolute top-4 right-4">
        <DarkModeToggle />
      </div>

      <div className="w-full max-w-lg">
        {/* Logo & Title */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-20 h-20 bg-primary-600 rounded-2xl shadow-lg mb-4">
            <span className="text-4xl">🚗</span>
          </div>
          <h1 className="text-4xl font-bold text-gray-900 dark:text-gray-100 mb-2">
            Uber Ride Booking
          </h1>
          <p className="text-gray-600 dark:text-gray-400">
            Modern ride-sharing platform
          </p>
        </div>

        {!selectedRole ? (
          /* Role Selection */
          <Card className="p-8">
            <h2 className="text-2xl font-semibold text-gray-900 dark:text-gray-100 text-center mb-6">
              Select Your Role
            </h2>
            
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <button
                onClick={() => handleRoleSelect('rider')}
                className="group relative p-8 border-2 border-gray-200 dark:border-gray-700 rounded-xl hover:border-primary-500 hover:shadow-lg transition-all duration-200 bg-white dark:bg-gray-800"
              >
                <div className="flex flex-col items-center gap-4">
                  <div className="p-4 bg-primary-50 dark:bg-primary-900/20 rounded-full group-hover:scale-110 transition-transform">
                    <User className="w-8 h-8 text-primary-600" />
                  </div>
                  <div className="text-center">
                    <h3 className="text-xl font-semibold text-gray-900 dark:text-gray-100 mb-1">
                      Rider
                    </h3>
                    <p className="text-sm text-gray-500 dark:text-gray-400">
                      Request rides
                    </p>
                  </div>
                </div>
              </button>

              <button
                onClick={() => handleRoleSelect('driver')}
                className="group relative p-8 border-2 border-gray-200 dark:border-gray-700 rounded-xl hover:border-primary-500 hover:shadow-lg transition-all duration-200 bg-white dark:bg-gray-800"
              >
                <div className="flex flex-col items-center gap-4">
                  <div className="p-4 bg-blue-50 dark:bg-blue-900/20 rounded-full group-hover:scale-110 transition-transform">
                    <Car className="w-8 h-8 text-blue-600" />
                  </div>
                  <div className="text-center">
                    <h3 className="text-xl font-semibold text-gray-900 dark:text-gray-100 mb-1">
                      Driver
                    </h3>
                    <p className="text-sm text-gray-500 dark:text-gray-400">
                      Accept rides
                    </p>
                  </div>
                </div>
              </button>
            </div>
          </Card>
        ) : (
          /* Login Form */
          <Card className="p-8">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-2xl font-semibold text-gray-900 dark:text-gray-100 capitalize">
                {selectedRole} Login
              </h2>
              <button
                onClick={handleBack}
                className="text-sm text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-100"
              >
                ← Back
              </button>
            </div>

            <form onSubmit={handleLogin} className="space-y-4">
              <Input
                label="User ID"
                type="number"
                placeholder="Enter your ID (e.g., 1001)"
                value={userId}
                onChange={(e) => setUserId(e.target.value)}
                required
                min="1"
              />

              <Input
                label="Name (Optional)"
                type="text"
                placeholder="Enter your name"
                value={userName}
                onChange={(e) => setUserName(e.target.value)}
              />

              {error && (
                <div className="p-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
                  <p className="text-sm text-red-600 dark:text-red-400">{error}</p>
                </div>
              )}

              <Button type="submit" fullWidth>
                Continue as {selectedRole}
              </Button>
            </form>

            <div className="mt-6 pt-6 border-t border-gray-200 dark:border-gray-700">
              <p className="text-xs text-center text-gray-500 dark:text-gray-400">
                This is a demo application. Enter any user ID to continue.
              </p>
            </div>
          </Card>
        )}

        {/* Footer */}
        <p className="text-center text-sm text-gray-500 dark:text-gray-400 mt-6">
          Built with React + Vite + Tailwind CSS
        </p>
      </div>
    </div>
  )
}

export default Login
