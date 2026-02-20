import { useState } from 'react'
import { MapPin } from 'lucide-react'
import { useRide } from '../../context/RideContext'
import Card from '../shared/Card'
import Button from '../shared/Button'
import Input from '../shared/Input'
import { validateLocation } from '../../utils/helpers'

const RideRequestForm = () => {
  const { requestRide, loading } = useRide()
  const [pickupLocation, setPickupLocation] = useState('')
  const [dropoffLocation, setDropoffLocation] = useState('')
  const [errors, setErrors] = useState({})

  const handleSubmit = async (e) => {
    e.preventDefault()
    
    // Validate inputs
    const newErrors = {}
    if (!validateLocation(pickupLocation)) {
      newErrors.pickup = 'Please enter a valid pickup location (min 3 characters)'
    }
    if (!validateLocation(dropoffLocation)) {
      newErrors.dropoff = 'Please enter a valid dropoff location (min 3 characters)'
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors)
      return
    }

    try {
      await requestRide(pickupLocation, dropoffLocation)
      // Clear form on success
      setPickupLocation('')
      setDropoffLocation('')
      setErrors({})
    } catch (error) {
      console.error('Failed to request ride:', error)
    }
  }

  return (
    <Card title="Request a Ride" className="mb-6">
      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="relative">
          <MapPin className="absolute left-3 top-[46px] w-5 h-5 text-primary-600" />
          <Input
            label="Pickup Location"
            type="text"
            placeholder="Enter pickup address"
            value={pickupLocation}
            onChange={(e) => {
              setPickupLocation(e.target.value)
              setErrors(prev => ({ ...prev, pickup: '' }))
            }}
            error={errors.pickup}
            className="pl-10"
            required
          />
        </div>

        <div className="relative">
          <MapPin className="absolute left-3 top-[46px] w-5 h-5 text-red-600" />
          <Input
            label="Dropoff Location"
            type="text"
            placeholder="Enter destination address"
            value={dropoffLocation}
            onChange={(e) => {
              setDropoffLocation(e.target.value)
              setErrors(prev => ({ ...prev, dropoff: '' }))
            }}
            error={errors.dropoff}
            className="pl-10"
            required
          />
        </div>

        <Button 
          type="submit" 
          fullWidth 
          loading={loading}
          disabled={!pickupLocation || !dropoffLocation}
        >
          Request Ride
        </Button>
      </form>
    </Card>
  )
}

export default RideRequestForm
