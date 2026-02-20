import { Clock, MapPin } from 'lucide-react'
import { useRide } from '../../context/RideContext'
import Card from '../shared/Card'
import StatusBadge from '../shared/StatusBadge'
import { mapBackendToUIStatus, formatCurrency, formatDateTime } from '../../utils/helpers'

const CompletedRides = () => {
  const { rideHistory } = useRide()

  const completedRides = rideHistory.filter(
    ride => ride.status === 'COMPLETED' || ride.status === 'PAID'
  )

  if (completedRides.length === 0) {
    return (
      <Card title="Completed Rides">
        <div className="text-center py-8">
          <Clock className="w-12 h-12 text-gray-300 dark:text-gray-600 mx-auto mb-3" />
          <p className="text-sm text-gray-500 dark:text-gray-400">
            No completed rides yet
          </p>
        </div>
      </Card>
    )
  }

  return (
    <Card 
      title="Completed Rides" 
      subtitle={`${completedRides.length} total`}
    >
      <div className="space-y-3 max-h-96 overflow-y-auto">
        {completedRides.slice().reverse().map((ride) => {
          const uiStatus = mapBackendToUIStatus(ride.status)
          
          return (
            <div 
              key={ride.id}
              className="p-4 bg-gray-50 dark:bg-gray-700/50 rounded-lg border border-gray-200 dark:border-gray-600 hover:border-primary-500 transition-colors"
            >
              <div className="flex items-start justify-between mb-3">
                <div>
                  <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                    Ride #{ride.id}
                  </p>
                  <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                    Customer: User #{ride.userId}
                  </p>
                </div>
                <StatusBadge status={uiStatus} size="sm" showIcon={false} />
              </div>

              <div className="space-y-2 text-sm">
                <div className="flex items-start gap-2">
                  <MapPin className="w-4 h-4 text-primary-600 mt-0.5 flex-shrink-0" />
                  <p className="text-gray-700 dark:text-gray-300 line-clamp-1">
                    {ride.pickupLocation}
                  </p>
                </div>
                <div className="flex items-start gap-2">
                  <MapPin className="w-4 h-4 text-red-600 mt-0.5 flex-shrink-0" />
                  <p className="text-gray-700 dark:text-gray-300 line-clamp-1">
                    {ride.dropoffLocation}
                  </p>
                </div>
              </div>

              <div className="flex items-center justify-between mt-3 pt-3 border-t border-gray-200 dark:border-gray-600">
                <p className="text-xs text-gray-500 dark:text-gray-400">
                  {formatDateTime(ride.createdAt)}
                </p>
                <p className="text-sm font-semibold text-green-600">
                  + {formatCurrency(ride.fare || 0)}
                </p>
              </div>
            </div>
          )
        })}
      </div>
    </Card>
  )
}

export default CompletedRides
