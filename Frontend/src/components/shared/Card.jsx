const Card = ({ 
  children, 
  className = '', 
  title = null,
  subtitle = null,
  hoverable = false,
  ...props 
}) => {
  const hoverClasses = hoverable ? 'hover:shadow-lg hover:border-gray-300 dark:hover:border-gray-600 cursor-pointer' : ''

  return (
    <div 
      className={`bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6 transition-all ${hoverClasses} ${className}`}
      {...props}
    >
      {(title || subtitle) && (
        <div className="mb-4">
          {title && (
            <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">
              {title}
            </h3>
          )}
          {subtitle && (
            <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
              {subtitle}
            </p>
          )}
        </div>
      )}
      {children}
    </div>
  )
}

export default Card
