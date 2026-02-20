// Ride status constants
export const RIDE_STATUS = {
  REQUESTED: 'REQUESTED',
  ACCEPTED: 'ACCEPTED',
  IN_PROGRESS: 'IN_PROGRESS',
  COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED',
}

// Extended status for UI display
export const UI_STATUS = {
  REQUESTED: 'requested',
  ASSIGNED: 'assigned',
  ACCEPTED: 'accepted',
  ONGOING: 'ongoing',
  COMPLETED: 'completed',
  PAID: 'paid',
  CANCELLED: 'cancelled',
}

// Map backend status to UI status
export const mapBackendToUIStatus = (backendStatus, hasPaid = false) => {
  const statusMap = {
    REQUESTED: UI_STATUS.REQUESTED,
    ACCEPTED: UI_STATUS.ACCEPTED,
    IN_PROGRESS: UI_STATUS.ONGOING,
    COMPLETED: hasPaid ? UI_STATUS.PAID : UI_STATUS.COMPLETED,
    CANCELLED: UI_STATUS.CANCELLED,
  }
  return statusMap[backendStatus] || UI_STATUS.REQUESTED
}

// Status colors for UI
export const getStatusColor = (status) => {
  const colorMap = {
    [UI_STATUS.REQUESTED]: 'text-blue-600 bg-blue-50 dark:bg-blue-900/20',
    [UI_STATUS.ASSIGNED]: 'text-blue-600 bg-blue-50 dark:bg-blue-900/20',
    [UI_STATUS.ACCEPTED]: 'text-green-600 bg-green-50 dark:bg-green-900/20',
    [UI_STATUS.ONGOING]: 'text-yellow-600 bg-yellow-50 dark:bg-yellow-900/20',
    [UI_STATUS.COMPLETED]: 'text-purple-600 bg-purple-50 dark:bg-purple-900/20',
    [UI_STATUS.PAID]: 'text-green-600 bg-green-50 dark:bg-green-900/20',
    [UI_STATUS.CANCELLED]: 'text-red-600 bg-red-50 dark:bg-red-900/20',
  }
  return colorMap[status] || 'text-gray-600 bg-gray-50 dark:bg-gray-900/20'
}

// Status icons
export const getStatusIcon = (status) => {
  const iconMap = {
    [UI_STATUS.REQUESTED]: '🔍',
    [UI_STATUS.ASSIGNED]: '🚗',
    [UI_STATUS.ACCEPTED]: '✅',
    [UI_STATUS.ONGOING]: '🏃',
    [UI_STATUS.COMPLETED]: '🎯',
    [UI_STATUS.PAID]: '💰',
    [UI_STATUS.CANCELLED]: '❌',
  }
  return iconMap[status] || '📝'
}

// Format currency
export const formatCurrency = (amount) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  }).format(amount)
}

// Format date/time
export const formatDateTime = (dateString) => {
  if (!dateString) return 'N/A'
  
  const date = new Date(dateString)
  return new Intl.DateTimeFormat('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}

// Calculate ETA (mock - in production would use real calculation)
export const calculateETA = () => {
  const minutes = Math.floor(Math.random() * 15) + 5
  return `${minutes} min`
}

// Validate location input
export const validateLocation = (location) => {
  return location && location.trim().length >= 3
}
