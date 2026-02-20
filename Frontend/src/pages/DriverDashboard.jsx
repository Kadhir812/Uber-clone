import Header from '../components/shared/Header'
import ActiveRideCard from '../components/driver/ActiveRideCard'
import DriverStats from '../components/driver/DriverStats'
import CompletedRides from '../components/driver/CompletedRides'

const DriverDashboard = () => {
  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <Header title="Driver Dashboard" />
      
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Left Column */}
          <div>
            <ActiveRideCard />
            <DriverStats />
          </div>

          {/* Right Column */}
          <div>
            <CompletedRides />
          </div>
        </div>
      </main>
    </div>
  )
}

export default DriverDashboard
