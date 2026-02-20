const Input = ({ 
  label, 
  error, 
  helperText,
  className = '',
  ...props 
}) => {
  return (
    <div className={`w-full ${className}`}>
      {label && (
        <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
          {label}
          {props.required && <span className="text-red-500 ml-1">*</span>}
        </label>
      )}
      <input
        className={`w-full px-4 py-3 bg-white dark:bg-gray-700 border ${
          error 
            ? 'border-red-500 focus:ring-red-500' 
            : 'border-gray-300 dark:border-gray-600 focus:ring-primary-500'
        } rounded-lg focus:ring-2 focus:border-transparent outline-none transition-all placeholder:text-gray-400`}
        {...props}
      />
      {error && (
        <p className="mt-1 text-sm text-red-600 dark:text-red-400">{error}</p>
      )}
      {helperText && !error && (
        <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">{helperText}</p>
      )}
    </div>
  )
}

export default Input
