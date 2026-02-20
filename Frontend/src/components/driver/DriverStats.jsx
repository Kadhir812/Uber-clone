import { TrendingUp, DollarSign, MapPin, Clock } from 'lucide-react'
import { useRide } from '../../context/RideContext'
import Card from '../shared/Card'
import { formatCurrency } from '../../utils/helpers'

const DriverStats = () => {
  const { rideHistory } = useRide()

  const completedRides = rideHistory.filter(
    ride => ride.status === 'COMPLETED' || ride.status === 'PAID'
  )

  const totalEarnings = completedRides.reduce(
    (sum, ride) => sum + (ride.fare || 0), 
    0
  )

  const todayRides = completedRides.filter(ride => {
    const today = new Date().toDateString()
    return new Date(ride.createdAt).toDateString() === today
  }).length

  const averageFare = completedRides.length > 0 
    ? totalEarnings / completedRides.length 
    : 0

  const stats = [
    {
      icon: MapPin,
      label: 'Total Rides',
      value: completedRides.length,
      color: 'text-blue-600',
      bgColor: 'bg-blue-50 dark:bg-blue-900/20',
    },
    {
      icon: DollarSign,
      label: 'Total Earnings',
      value: formatCurrency(totalEarnings),
      color: 'text-green-600',
      bgColor: 'bg-green-50 dark:bg-green-900/20',
    },
    {
      icon: TrendingUp,
      label: 'Avg. Fare',
      value: formatCurrency(averageFare),
      color: 'text-purple-600',
      bgColor: 'bg-purple-50 dark:bg-purple-900/20',
    },
    {
      icon: Clock,
      label: 'Today',
      value: `${todayRides} rides`,
      color: 'text-yellow-600',
      bgColor: 'bg-yellow-50 dark:bg-yellow-900/20',
    },
  ]

  return (
    <Card title="Your Stats" subtitle="Performance overview" className="mb-6">
      <div className="grid grid-cols-2 gap-4">
        {stats.map((stat, index) => {
          const Icon = stat.icon
          return (
            <div 
              key={index}
              className="p-4 bg-gray-50 dark:bg-gray-700/50 rounded-lg border border-gray-200 dark:border-gray-600"
            >
              <div className={`inline-flex p-2 rounded-lg ${stat.bgColor} mb-3`}>
                <Icon className={`w-5 h-5 ${stat.color}`} />
              </div>
              <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">
                {stat.label}
              </p>
              <p className="text-lg font-bold text-gray-900 dark:text-gray-100">
                {stat.value}
              </p>
            </div>
          )
        })}
      </div>
    </Card>
  )
}

export default DriverStats
