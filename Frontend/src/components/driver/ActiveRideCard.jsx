import { MapPin, User, DollarSign, Clock, CheckCircle, PlayCircle } from 'lucide-react'
import { useRide } from '../../context/RideContext'
import { useEffect, useState } from 'react'
import Card from '../shared/Card'
import Button from '../shared/Button'
import StatusBadge from '../shared/StatusBadge'
import driverService from '../../services/driverService'
import rideService from '../../services/rideService'
import { 
  mapBackendToUIStatus, 
  formatCurrency, 
  formatDateTime,
  calculateETA,
  UI_STATUS 
} from '../../utils/helpers'

const ActiveRideCard = () => {
  const { currentRide, setCurrentRide, acceptRide, startRide, completeRide, loading } = useRide()
  const [checking, setChecking] = useState(false)

  // Poll for assigned rides
  useEffect(() => {
    const user = JSON.parse(localStorage.getItem('user'))
    if (!user || user.role !== 'driver') return

    const checkForAssignedRide = async () => {
      try {
        setChecking(true)
        // Get driver details to check if they have a currentRideId
        const driverData = await driverService.getDriver(user.id)
        
        if (driverData.currentRideId && (!currentRide || currentRide.id !== driverData.currentRideId)) {
          // Driver has been assigned a ride, fetch ride details
          const rideData = await rideService.getRide(driverData.currentRideId)
          setCurrentRide(rideData)
          localStorage.setItem('currentRideId', rideData.id)
        } else if (!driverData.currentRideId && currentRide) {
          // Ride completed or cancelled
          setCurrentRide(null)
          localStorage.removeItem('currentRideId')
        }
      } catch (error) {
        console.error('Error checking for assigned rides:', error)
      } finally {
        setChecking(false)
      }
    }

    // Check immediately
    checkForAssignedRide()

    // Poll every 3 seconds
    const interval = setInterval(checkForAssignedRide, 3000)

    return () => clearInterval(interval)
  }, [currentRide, setCurrentRide])

  if (!currentRide) {
    return (
      <Card className="mb-6">
        <div className="text-center py-8">
          <div className="w-16 h-16 bg-gray-100 dark:bg-gray-700 rounded-full flex items-center justify-center mx-auto mb-4">
            <MapPin className="w-8 h-8 text-gray-400" />
          </div>
          <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">
            No Active Ride
          </h3>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Waiting for ride assignments...
          </p>
          <div className="mt-4 flex items-center justify-center gap-2">
            <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></div>
            <span className="text-xs text-gray-500 dark:text-gray-400">
              {checking ? 'Checking for rides...' : 'Online & Available'}
            </span>
          </div>
        </div>
      </Card>
    )
  }

  const uiStatus = mapBackendToUIStatus(currentRide.status)
  const isAccepted = uiStatus === UI_STATUS.ACCEPTED
  const isOngoing = uiStatus === UI_STATUS.ONGOING
  const isCompleted = uiStatus === UI_STATUS.COMPLETED

  const handleAccept = async () => {
    try {
      await acceptRide(currentRide.id)
    } catch (error) {
      alert('Failed to accept ride. Please try again.')
    }
  }

  const handleStartRide = async () => {
    try {
      await startRide(currentRide.id)
    } catch (error) {
      alert('Failed to start ride. Please try again.')
    }
  }

  const handleCompleteRide = async () => {
    try {
      await completeRide(currentRide.id)
      alert('Ride completed successfully!')
    } catch (error) {
      alert('Failed to complete ride. Please try again.')
    }
  }

  return (
    <Card title="Current Assignment" className="mb-6">
      {/* Status Badge */}
      <div className="flex items-center justify-between mb-6">
        <StatusBadge status={uiStatus} size="lg" />
        <span className="text-sm text-gray-500 dark:text-gray-400">
          Ride ID: #{currentRide.id}
        </span>
      </div>

      {/* Ride Details */}
      <div className="space-y-4 mb-6">
        <div className="flex items-start gap-3">
          <User className="w-5 h-5 text-blue-600 mt-0.5" />
          <div className="flex-1">
            <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Customer</p>
            <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
              User #{currentRide.userId}
            </p>
          </div>
        </div>

        <div className="flex items-start gap-3">
          <MapPin className="w-5 h-5 text-primary-600 mt-0.5" />
          <div className="flex-1">
            <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Pickup Location</p>
            <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
              {currentRide.pickupLocation}
            </p>
            {isAccepted && (
              <p className="text-xs text-primary-600 mt-1">
                ETA: {calculateETA()}
              </p>
            )}
          </div>
        </div>

        <div className="flex items-start gap-3">
          <MapPin className="w-5 h-5 text-red-600 mt-0.5" />
          <div className="flex-1">
            <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Dropoff Location</p>
            <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
              {currentRide.dropoffLocation}
            </p>
          </div>
        </div>

        <div className="flex items-start gap-3">
          <DollarSign className="w-5 h-5 text-green-600 mt-0.5" />
          <div className="flex-1">
            <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Fare</p>
            <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
              {formatCurrency(currentRide.fare || 25.50)}
            </p>
          </div>
        </div>

        <div className="flex items-start gap-3">
          <Clock className="w-5 h-5 text-gray-600 mt-0.5" />
          <div className="flex-1">
            <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Requested</p>
            <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
              {formatDateTime(currentRide.createdAt)}
            </p>
          </div>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="space-y-3 pt-4 border-t border-gray-200 dark:border-gray-700">
        {!isAccepted && !isOngoing && !isCompleted && (
          <Button 
            onClick={handleAccept} 
            variant="success" 
            fullWidth
            loading={loading}
          >
            <CheckCircle className="w-5 h-5 mr-2 inline" />
            Accept Ride
          </Button>
        )}

        {isAccepted && (
          <Button 
            onClick={handleStartRide} 
            variant="primary" 
            fullWidth
            loading={loading}
          >
            <PlayCircle className="w-5 h-5 mr-2 inline" />
            Start Ride
          </Button>
        )}

        {isOngoing && (
          <Button 
            onClick={handleCompleteRide} 
            variant="success" 
            fullWidth
            loading={loading}
          >
            <CheckCircle className="w-5 h-5 mr-2 inline" />
            Complete Ride
          </Button>
        )}

        {isCompleted && (
          <div className="text-center py-3 bg-green-50 dark:bg-green-900/20 rounded-lg">
            <p className="text-sm font-medium text-green-600">
              ✓ Ride completed! Waiting for customer payment...
            </p>
          </div>
        )}
      </div>

      {/* Live Updates Indicator */}
      <div className="mt-4 flex items-center justify-center gap-2 text-xs text-gray-500 dark:text-gray-400">
        <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></div>
        Auto-updating status
      </div>
    </Card>
  )
}

export default ActiveRideCard
