import Header from '../components/shared/Header'
import RideRequestForm from '../components/rider/RideRequestForm'
import RideStatusCard from '../components/rider/RideStatusCard'
import RideHistory from '../components/rider/RideHistory'

const RiderDashboard = () => {
  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <Header title="Rider Dashboard" />
      
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Left Column */}
          <div>
            <RideRequestForm />
            <RideHistory />
          </div>

          {/* Right Column */}
          <div>
            <RideStatusCard />
          </div>
        </div>
      </main>
    </div>
  )
}

export default RiderDashboard
