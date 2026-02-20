import axios from 'axios'

// Empty baseURL to use Vite proxy (works for both laptop and mobile)
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
})

// Request interceptor
apiClient.interceptors.request.use(
  (config) => {
    // Add any auth tokens here if needed
    const user = localStorage.getItem('user')
    if (user) {
      const userData = JSON.parse(user)
      config.headers['X-User-Id'] = userData.id
      config.headers['X-User-Role'] = userData.role
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      // Server responded with error
      const message = error.response.data?.message || error.response.statusText
      console.error('API Error:', message)
      return Promise.reject(new Error(message))
    } else if (error.request) {
      // Request made but no response
      console.error('Network Error:', error.message)
      return Promise.reject(new Error('Network error. Please check your connection.'))
    } else {
      // Something else happened
      console.error('Error:', error.message)
      return Promise.reject(error)
    }
  }
)

export default apiClient
