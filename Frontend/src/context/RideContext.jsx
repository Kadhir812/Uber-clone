import { createContext, useContext, useState, useEffect, useCallback } from 'react'
import { useAuth } from './AuthContext'
import rideService from '../services/rideService'
import driverService from '../services/driverService'

const RideContext = createContext(null)

export const useRide = () => {
  const context = useContext(RideContext)
  if (!context) {
    throw new Error('useRide must be used within a RideProvider')
  }
  return context
}

export const RideProvider = ({ children }) => {
  const { user, isAuthenticated } = useAuth()
  const [currentRide, setCurrentRide] = useState(null)
  const [rideHistory, setRideHistory] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  // Polling interval - 3 seconds
  const POLLING_INTERVAL = 3000

  // Fetch current ride status
  const fetchRideStatus = useCallback(async (rideId) => {
    if (!rideId) return
    
    try {
      const ride = await rideService.getRide(rideId)
      setCurrentRide(ride)
      setError(null)
    } catch (err) {
      console.error('Error fetching ride status:', err)
      setError(err.message)
    }
  }, [])

  // Fetch ride history
  const fetchRideHistory = useCallback(async () => {
    if (!user) return

    try {
      const history = user.role === 'rider' 
        ? await rideService.getUserRides(user.id)
        : await rideService.getDriverRides(user.id)
      
      setRideHistory(history)
    } catch (err) {
      console.error('Error fetching ride history:', err)
    }
  }, [user])

  // Request a new ride (rider)
  const requestRide = async (pickupLocation, dropoffLocation) => {
    setLoading(true)
    setError(null)

    try {
      const rideData = {
        userId: user.id,
        pickupLocation,
        dropoffLocation,
        fare: 0 // Will be calculated by backend
      }

      const newRide = await rideService.requestRide(rideData)
      setCurrentRide(newRide)
      localStorage.setItem('currentRideId', newRide.id)
      return newRide
    } catch (err) {
      setError(err.message)
      throw err
    } finally {
      setLoading(false)
    }
  }

  // Assign driver to ride (driver)
  const acceptRide = async (rideId) => {
    setLoading(true)
    setError(null)

    try {
      // Call driver service to accept the ride
      await driverService.acceptRide(user.id, rideId)
      
      // Then fetch updated ride status
      const updatedRide = await rideService.getRide(rideId)
      setCurrentRide(updatedRide)
      localStorage.setItem('currentRideId', rideId)
      return updatedRide
    } catch (err) {
      setError(err.message)
      throw err
    } finally {
      setLoading(false)
    }
  }

  // Start ride (driver)
  const startRide = async (rideId) => {
    setLoading(true)
    setError(null)

    try {
      // Call driver service to start the ride
      await driverService.startRide(user.id, rideId)
      
      // Then fetch updated ride status
      const updatedRide = await rideService.getRide(rideId)
      setCurrentRide(updatedRide)
      return updatedRide
    } catch (err) {
      setError(err.message)
      throw err
    } finally {
      setLoading(false)
    }
  }

  // Complete ride (driver)
  const completeRide = async (rideId) => {
    setLoading(true)
    setError(null)

    try {
      // Call driver service to complete the ride
      await driverService.completeRide(user.id, rideId)
      
      // Then fetch updated ride status
      const updatedRide = await rideService.getRide(rideId)
      setCurrentRide(updatedRide)
      
      // Clear current ride if completed
      localStorage.removeItem('currentRideId')
      
      // Refresh ride history to show the completed ride
      await fetchRideHistory()
      
      return updatedRide
    } catch (err) {
      setError(err.message)
      throw err
    } finally {
      setLoading(false)
    }
  }

  // Update ride status
  const updateRideStatus = async (rideId, status) => {
    setLoading(true)
    setError(null)

    try {
      const updatedRide = await rideService.updateRideStatus(rideId, status)
      setCurrentRide(updatedRide)
      
      // Clear current ride if completed or cancelled
      if (['COMPLETED', 'CANCELLED', 'PAID'].includes(status)) {
        localStorage.removeItem('currentRideId')
        // Refresh ride history to show the completed ride
        await fetchRideHistory()
      }
      
      return updatedRide
    } catch (err) {
      setError(err.message)
      throw err
    } finally {
      setLoading(false)
    }
  }

  // Clear current ride
  const clearCurrentRide = () => {
    setCurrentRide(null)
    localStorage.removeItem('currentRideId')
  }

  // Polling effect for active ride
  useEffect(() => {
    if (!isAuthenticated) return

    const savedRideId = localStorage.getItem('currentRideId')
    if (savedRideId) {
      fetchRideStatus(savedRideId)
    }

    const interval = setInterval(() => {
      const rideId = localStorage.getItem('currentRideId')
      if (rideId) {
        fetchRideStatus(rideId)
      }
    }, POLLING_INTERVAL)

    return () => clearInterval(interval)
  }, [isAuthenticated, fetchRideStatus])

  // Fetch ride history on mount
  useEffect(() => {
    if (isAuthenticated) {
      fetchRideHistory()
    }
  }, [isAuthenticated, fetchRideHistory])

  const value = {
    currentRide,
    setCurrentRide,
    rideHistory,
    loading,
    error,
    requestRide,
    acceptRide,
    startRide,
    completeRide,
    updateRideStatus,
    fetchRideStatus,
    fetchRideHistory,
    clearCurrentRide,
  }

  return <RideContext.Provider value={value}>{children}</RideContext.Provider>
}
