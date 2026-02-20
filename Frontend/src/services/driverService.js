import axios from 'axios'

// Empty baseURL to use Vite proxy (works for both laptop and mobile)
const DRIVER_API_BASE_URL = import.meta.env.VITE_DRIVER_API_BASE_URL || ''

const driverApiClient = axios.create({
  baseURL: DRIVER_API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
})

// Request interceptor
driverApiClient.interceptors.request.use(
  (config) => {
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
driverApiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      const message = error.response.data?.message || error.response.statusText
      console.error('Driver API Error:', message)
      return Promise.reject(new Error(message))
    } else if (error.request) {
      console.error('Network Error:', error.message)
      return Promise.reject(new Error('Network error. Please check your connection.'))
    } else {
      console.error('Error:', error.message)
      return Promise.reject(error)
    }
  }
)

const driverService = {
  // Get driver by ID
  getDriver: async (driverId) => {
    const response = await driverApiClient.get(`/api/drivers/${driverId}`)
    return response.data
  },

  // Get all drivers
  getAllDrivers: async () => {
    const response = await driverApiClient.get('/api/drivers')
    return response.data
  },

  // Get available drivers
  getAvailableDrivers: async (latitude = null, longitude = null, radiusKm = 5.0) => {
    const params = {}
    if (latitude && longitude) {
      params.latitude = latitude
      params.longitude = longitude
      params.radiusKm = radiusKm
    }
    const response = await driverApiClient.get('/api/drivers/available', { params })
    return response.data
  },

  // Create new driver
  createDriver: async (driverData) => {
    const response = await driverApiClient.post('/api/drivers', driverData)
    return response.data
  },

  // Accept ride
  acceptRide: async (driverId, rideId) => {
    const response = await driverApiClient.post(`/api/drivers/${driverId}/accept-ride`, {
      rideId: rideId
    })
    return response.data
  },

  // Start ride
  startRide: async (driverId, rideId) => {
    const response = await driverApiClient.post(`/api/drivers/${driverId}/start-ride`, {
      rideId: rideId
    })
    return response.data
  },

  // Complete ride
  completeRide: async (driverId, rideId) => {
    const response = await driverApiClient.post(`/api/drivers/${driverId}/complete-ride`, {
      rideId: rideId
    })
    return response.data
  },

  // Update driver location
  updateLocation: async (driverId, latitude, longitude) => {
    const response = await driverApiClient.put(`/api/drivers/${driverId}/location`, {
      latitude: latitude,
      longitude: longitude
    })
    return response.data
  },

  // Update driver status
  updateStatus: async (driverId, status) => {
    const response = await driverApiClient.put(`/api/drivers/${driverId}/status`, {
      status: status
    })
    return response.data
  },
}

export default driverService
