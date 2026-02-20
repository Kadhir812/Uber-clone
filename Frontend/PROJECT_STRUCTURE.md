# Project Structure

## Complete Folder Structure

```
frontend/
├── public/                          # Static assets
├── src/
│   ├── components/                  # React components
│   │   ├── shared/                 # Shared/reusable components
│   │   │   ├── Button.jsx
│   │   │   ├── Card.jsx
│   │   │   ├── DarkModeToggle.jsx
│   │   │   ├── Header.jsx
│   │   │   ├── Input.jsx
│   │   │   ├── ProtectedRoute.jsx
│   │   │   └── StatusBadge.jsx
│   │   ├── rider/                  # Rider-specific components
│   │   │   ├── RideHistory.jsx
│   │   │   ├── RideRequestForm.jsx
│   │   │   └── RideStatusCard.jsx
│   │   └── driver/                 # Driver-specific components
│   │       ├── ActiveRideCard.jsx
│   │       ├── CompletedRides.jsx
│   │       └── DriverStats.jsx
│   ├── context/                     # React Context providers
│   │   ├── AuthContext.jsx         # Authentication state
│   │   └── RideContext.jsx         # Ride management state
│   ├── pages/                       # Page components
│   │   ├── Login.jsx               # Login/role selection page
│   │   ├── RiderDashboard.jsx      # Rider dashboard page
│   │   └── DriverDashboard.jsx     # Driver dashboard page
│   ├── services/                    # API services
│   │   ├── api.js                  # Axios instance with interceptors
│   │   └── rideService.js          # Ride-related API calls
│   ├── utils/                       # Utility functions
│   │   └── helpers.js              # Helper functions
│   ├── App.jsx                      # Main app component
│   ├── main.jsx                     # Entry point
│   └── index.css                    # Global styles
├── .env.example                     # Environment variables template
├── .gitignore                       # Git ignore rules
├── index.html                       # HTML template
├── package.json                     # Dependencies
├── postcss.config.js               # PostCSS configuration
├── tailwind.config.js              # Tailwind CSS configuration
├── vite.config.js                  # Vite configuration
└── README.md                        # Project documentation
```

## Component Architecture

### Shared Components
- **Button**: Reusable button with variants (primary, secondary, success, danger, etc.)
- **Card**: Container component with optional title and subtitle
- **Input**: Form input with label, error, and helper text support
- **StatusBadge**: Displays ride status with color coding and icons
- **Header**: Top navigation bar with user info and logout
- **DarkModeToggle**: Toggle between light and dark themes
- **ProtectedRoute**: Route wrapper for authentication

### Rider Components
- **RideRequestForm**: Form to request new rides
- **RideStatusCard**: Displays current ride status with progress tracker
- **RideHistory**: List of past rides

### Driver Components
- **ActiveRideCard**: Shows current ride assignment with action buttons
- **DriverStats**: Performance statistics (total rides, earnings, etc.)
- **CompletedRides**: List of completed rides

## Context Providers

### AuthContext
- Manages user authentication state
- Handles login/logout
- Provides user role information

### RideContext
- Manages ride state (current ride, ride history)
- Handles ride requests, acceptance, and status updates
- Implements polling for real-time updates (every 3 seconds)

## Key Features

### Authentication Flow
1. User selects role (Rider or Driver)
2. Enters user ID and optional name
3. Redirected to role-specific dashboard

### Rider Flow
1. Request ride with pickup/dropoff locations
2. View real-time ride status
3. See driver assignment and ETA
4. Complete payment when ride is done

### Driver Flow
1. Receive ride assignments
2. Accept ride
3. Start ride when picking up customer
4. Complete ride when destination is reached

### Status Management
- **REQUESTED**: Ride has been requested
- **ACCEPTED**: Driver has accepted the ride
- **IN_PROGRESS**: Ride is ongoing
- **COMPLETED**: Ride is finished (waiting for payment)
- **PAID**: Payment completed

## Styling

### Tailwind CSS Classes
- Uses custom color scheme defined in `tailwind.config.js`
- Dark mode support with `dark:` variants
- Responsive design with `sm:`, `md:`, `lg:` breakpoints
- Custom animations for pulse effects

### Design System
- **Primary Color**: Green (#22c55e) - Success/active states
- **Blue**: Info/assigned states
- **Yellow**: Warning/ongoing states
- **Red**: Error/cancelled states
- **Purple**: Completed states before payment

## API Integration

### Endpoints Used
- `POST /api/rides` - Request new ride
- `GET /api/rides/:id` - Get ride details
- `GET /api/rides/user/:userId` - Get user's rides
- `GET /api/rides/driver/:driverId` - Get driver's rides
- `PUT /api/rides/:id/assign-driver` - Assign driver
- `PUT /api/rides/:id/status` - Update ride status

### Polling Strategy
- Implemented in `RideContext.jsx`
- Polls every 3 seconds when there's an active ride
- Updates UI automatically with new status

## State Management

### LocalStorage
- Stores user session data
- Persists current ride ID
- Remembers dark mode preference

### Context API
- `AuthContext`: User authentication state
- `RideContext`: Ride management and polling

## Development Workflow

1. **Install dependencies**: `npm install`
2. **Start dev server**: `npm run dev`
3. **Access at**: `http://localhost:3000`
4. **Backend should run on**: `http://localhost:8081`

## Production Build

```bash
npm run build
npm run preview
```

The build output will be in the `dist/` directory.
