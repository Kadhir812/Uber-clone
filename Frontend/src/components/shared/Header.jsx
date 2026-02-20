import { useAuth } from '../../context/AuthContext'
import { LogOut } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import DarkModeToggle from './DarkModeToggle'

const Header = ({ title }) => {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  return (
    <header className="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 sticky top-0 z-10">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <div className="flex items-center gap-4">
            <div className="text-2xl font-bold text-primary-600">🚗</div>
            <div>
              <h1 className="text-xl font-semibold text-gray-900 dark:text-gray-100">
                {title}
              </h1>
              <p className="text-xs text-gray-500 dark:text-gray-400 capitalize">
                {user?.role} Mode
              </p>
            </div>
          </div>
          
          <div className="flex items-center gap-4">
            <div className="text-right">
              <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                {user?.name || `User #${user?.id}`}
              </p>
              <p className="text-xs text-gray-500 dark:text-gray-400">
                ID: {user?.id}
              </p>
            </div>
            
            <DarkModeToggle />
            
            <button
              onClick={handleLogout}
              className="p-2 rounded-lg bg-red-50 dark:bg-red-900/20 text-red-600 hover:bg-red-100 dark:hover:bg-red-900/30 transition-colors"
              aria-label="Logout"
            >
              <LogOut className="w-5 h-5" />
            </button>
          </div>
        </div>
      </div>
    </header>
  )
}

export default Header
