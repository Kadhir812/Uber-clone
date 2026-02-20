import apiClient from './api'

const rideService = {
  // Request a new ride
  requestRide: async (rideData) => {
    const response = await apiClient.post('/api/rides', rideData)
    return response.data
  },

  // Get ride by ID
  getRide: async (rideId) => {
    const response = await apiClient.get(`/api/rides/${rideId}`)
    return response.data
  },

  // Get user's ride history
  getUserRides: async (userId) => {
    const response = await apiClient.get(`/api/rides/user/${userId}`)
    return response.data
  },

  // Get driver's ride history
  getDriverRides: async (driverId) => {
    const response = await apiClient.get(`/api/rides/driver/${driverId}`)
    return response.data
  },

  // Assign driver to ride
  assignDriver: async (rideId, driverId) => {
    const response = await apiClient.put(`/api/rides/${rideId}/assign-driver?driverId=${driverId}`)
    return response.data
  },

  // Update ride status
  updateRideStatus: async (rideId, status) => {
    const response = await apiClient.put(`/api/rides/${rideId}/status?status=${status}`)
    return response.data
  },
}

export default rideService
