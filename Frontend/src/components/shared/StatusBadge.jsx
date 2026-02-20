import { getStatusColor, getStatusIcon } from '../../utils/helpers'

const StatusBadge = ({ status, size = 'md', showIcon = true }) => {
  const sizeClasses = {
    sm: 'px-2 py-1 text-xs',
    md: 'px-3 py-1.5 text-sm',
    lg: 'px-4 py-2 text-base',
  }

  const colorClasses = getStatusColor(status)
  const icon = getStatusIcon(status)

  return (
    <span 
      className={`inline-flex items-center gap-2 font-medium rounded-full ${sizeClasses[size]} ${colorClasses} uppercase tracking-wide`}
    >
      {showIcon && <span>{icon}</span>}
      {status.replace('_', ' ')}
    </span>
  )
}

export default StatusBadge
