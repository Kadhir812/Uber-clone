import { Clock, MapPin, DollarSign, User, CreditCard } from 'lucide-react'
import { useRide } from '../../context/RideContext'
import Card from '../shared/Card'
import StatusBadge from '../shared/StatusBadge'
import Button from '../shared/Button'
import { 
  mapBackendToUIStatus, 
  formatCurrency, 
  formatDateTime, 
  calculateETA,
  UI_STATUS 
} from '../../utils/helpers'

const RideStatusCard = () => {
  const { currentRide, updateRideStatus, loading } = useRide()

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
            Request a ride to get started
          </p>
        </div>
      </Card>
    )
  }

  const uiStatus = mapBackendToUIStatus(currentRide.status)
  const showPaymentButton = uiStatus === UI_STATUS.COMPLETED

  const handlePayment = async () => {
    try {
      await updateRideStatus(currentRide.id, 'PAID')
      alert('Payment successful! Thank you for riding with us.')
    } catch (error) {
      alert('Payment failed. Please try again.')
    }
  }

  // Status progression
  const statusSteps = [
    { key: UI_STATUS.REQUESTED, label: 'Requested' },
    { key: UI_STATUS.ASSIGNED, label: 'Assigned' },
    { key: UI_STATUS.ACCEPTED, label: 'Accepted' },
    { key: UI_STATUS.ONGOING, label: 'Ongoing' },
    { key: UI_STATUS.COMPLETED, label: 'Completed' },
    { key: UI_STATUS.PAID, label: 'Paid' },
  ]

  const currentStepIndex = statusSteps.findIndex(step => step.key === uiStatus)

  return (
    <Card title="Current Ride Status" className="mb-6">
      {/* Status Badge */}
      <div className="flex items-center justify-between mb-6">
        <StatusBadge status={uiStatus} size="lg" />
        <span className="text-sm text-gray-500 dark:text-gray-400">
          Ride ID: #{currentRide.id}
        </span>
      </div>

      {/* Progress Bar */}
      <div className="mb-6">
        <div className="flex items-center justify-between mb-2">
          {statusSteps.map((step, index) => (
            <div key={step.key} className="flex items-center">
              <div 
                className={`w-8 h-8 rounded-full flex items-center justify-center text-xs font-medium transition-all ${
                  index <= currentStepIndex
                    ? 'bg-primary-600 text-white'
                    : 'bg-gray-200 dark:bg-gray-700 text-gray-500'
                }`}
              >
                {index + 1}
              </div>
              {index < statusSteps.length - 1 && (
                <div 
                  className={`h-1 w-8 sm:w-12 mx-1 transition-all ${
                    index < currentStepIndex
                      ? 'bg-primary-600'
                      : 'bg-gray-200 dark:bg-gray-700'
                  }`}
                />
              )}
            </div>
          ))}
        </div>
        <div className="flex items-center justify-between text-xs text-gray-500 dark:text-gray-400 mt-2">
          {statusSteps.map((step, index) => (
            <span 
              key={step.key} 
              className={`${index <= currentStepIndex ? 'text-primary-600 font-medium' : ''} ${
                index === 0 || index === statusSteps.length - 1 ? 'text-center' : 'hidden sm:block'
              }`}
            >
              {step.label}
            </span>
          ))}
        </div>
      </div>

      {/* Ride Details */}
      <div className="space-y-4 border-t border-gray-200 dark:border-gray-700 pt-4">
        <div className="flex items-start gap-3">
          <MapPin className="w-5 h-5 text-primary-600 mt-0.5" />
          <div className="flex-1">
            <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Pickup</p>
            <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
              {currentRide.pickupLocation}
            </p>
          </div>
        </div>

        <div className="flex items-start gap-3">
          <MapPin className="w-5 h-5 text-red-600 mt-0.5" />
          <div className="flex-1">
            <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Dropoff</p>
            <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
              {currentRide.dropoffLocation}
            </p>
          </div>
        </div>

        {currentRide.driverId && currentRide.driverId !== 0 && (
          <div className="flex items-start gap-3">
            <User className="w-5 h-5 text-blue-600 mt-0.5" />
            <div className="flex-1">
              <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Driver</p>
              <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                Driver #{currentRide.driverId}
              </p>
              {(uiStatus === UI_STATUS.ACCEPTED || uiStatus === UI_STATUS.ONGOING) && (
                <p className="text-xs text-primary-600 mt-1">
                  ETA: {calculateETA()}
                </p>
              )}
            </div>
          </div>
        )}

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

      {/* Payment Button */}
      {showPaymentButton && (
        <div className="mt-6 pt-4 border-t border-gray-200 dark:border-gray-700">
          <Button 
            onClick={handlePayment} 
            variant="success" 
            fullWidth
            loading={loading}
          >
            <CreditCard className="w-5 h-5 mr-2 inline" />
            Pay {formatCurrency(currentRide.fare || 25.50)}
          </Button>
        </div>
      )}

      {/* Live Updates Indicator */}
      <div className="mt-4 flex items-center justify-center gap-2 text-xs text-gray-500 dark:text-gray-400">
        <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></div>
        Live updates enabled (every 3s)
      </div>
    </Card>
  )
}

export default RideStatusCard
