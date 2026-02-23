# 🚗 Uber Ride Booking System - Complete Architecture

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         REACT FRONTEND                               │
│                     http://localhost:3000                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐             │
│  │    Rider     │  │    Driver    │  │    Admin     │             │
│  │  Dashboard   │  │  Dashboard   │  │   Panel      │             │
│  └──────────────┘  └──────────────┘  └──────────────┘             │
└──────────────────────┬──────────────────────────────────────────────┘
                       │
                       │ REST APIs (HTTP/JSON)
                       │
┌──────────────────────┴──────────────────────────────────────────────┐
│                    SPRING BOOT SERVICES                              │
│                                                                      │
│  ┌─────────────┐    ┌──────────────┐    ┌──────────────┐         │
│  │Ride Service │    │   Matching   │    │Driver Service│         │
│  │  :8081      │    │   Service    │    │    :8082     │         │
│  │             │    │    :8083     │    │              │         │
│  │ • Request   │    │• Match Rider │    │• Accept Ride │         │
│  │ • Track     │    │  with Driver │    │• Start Ride  │         │
│  │ • History   │    │• Assignment  │    │• Complete    │         │
│  └──────┬──────┘    └──────┬───────┘    └──────┬───────┘         │
│         │                   │                    │                  │
└─────────┼───────────────────┼────────────────────┼──────────────────┘
          │                   │                    │
          │        ┌──────────┴──────────┐        │
          │        │                     │        │
          └────────▼─────────────────────▼────────┘
                   │                     │
          ┌────────┴─────────┐  ┌───────┴────────┐
          │  APACHE KAFKA    │  │   POSTGRESQL   │
          │  (Docker)        │  │   (Docker)     │
          │                  │  │                │
          │ • ride-events    │  │ • uber_ride_db │
          │ • driver-events  │  │ • uber_driver_db│
          │                  │  │                │
          │  :9092           │  │  :5432         │
          └──────────────────┘  └────────────────┘
```

## 🔄 Communication Flow

### ✅ CORRECT Architecture

```
Frontend ←─────REST API─────→ Services ←─────Kafka Events─────→ Services
   ↓                             ↓                                  ↓
(HTTP/JSON)                   (Database)                      (Database)
```

### ❌ INCORRECT (Frontend NEVER talks to Kafka)

```
Frontend ──X──→ Kafka   ❌ NO DIRECT CONNECTION
Kafka ──X──→ Frontend   ❌ NO DIRECT CONNECTION
```

## 📊 Complete Ride Flow

### Step-by-Step Process

1. **Rider Requests Ride** (Frontend → ride-service)
   ```
   POST http://localhost:8081/api/rides
   {
     "userId": 1,
     "pickupLocation": "123 Main St",
     "dropoffLocation": "456 Oak Ave"
   }
   ```
   - Frontend sends HTTP request to ride-service
   - ride-service saves to PostgreSQL
   - ride-service publishes `RIDE_REQUESTED` event to Kafka

2. **Matching Service Assigns Driver** (Kafka → matching-service → Kafka)
   ```
   Kafka (ride-events) → matching-service consumes event
   matching-service finds available driver
   matching-service publishes DRIVER_ASSIGNED to Kafka (driver-events)
   ```

3. **Driver Receives Assignment** (Kafka → driver-service)
   ```
   Kafka (driver-events) → driver-service consumes event
   driver-service updates driver status to ON_THE_WAY
   driver-service saves to PostgreSQL
   ```

4. **Driver Accepts Ride** (Frontend → driver-service → Kafka)
   ```
   POST http://localhost:8082/api/drivers/2001/accept-ride
   {
     "rideId": 101
   }
   ```
   - Frontend sends HTTP request to driver-service
   - driver-service publishes `RIDE_ACCEPTED` to Kafka
   - ride-service consumes event and updates ride status

5. **Driver Starts Ride** (Frontend → driver-service → Kafka)
   ```
   POST http://localhost:8082/api/drivers/2001/start-ride
   {
     "rideId": 101
   }
   ```
   - driver-service publishes `RIDE_STARTED` to Kafka
   - ride-service updates ride status to IN_PROGRESS

6. **Driver Completes Ride** (Frontend → driver-service → Kafka)
   ```
   POST http://localhost:8082/api/drivers/2001/complete-ride
   {
     "rideId": 101
   }
   ```
   - driver-service publishes `RIDE_COMPLETED` to Kafka
   - ride-service updates ride to COMPLETED
   - driver-service marks driver as AVAILABLE

7. **Frontend Polls for Updates** (Frontend → ride-service)
   ```
   GET http://localhost:8081/api/rides/101
   ```
   - Frontend polls every 3 seconds
   - Gets updated status from ride-service PostgreSQL database
   - No direct Kafka involvement

## 🐳 Infrastructure (Docker Containers)

### Kafka Container
- **Image**: `confluentinc/cp-kafka:7.6.0`
- **Port**: 9092
- **Topics**: `ride-events`, `driver-events`
- **Purpose**: Event streaming between microservices

### PostgreSQL Container
- **Image**: `postgres:16-alpine`
- **Port**: 5432
- **Databases**: 
  - `uber_ride_db` (ride-service)
  - `uber_driver_db` (driver-service)
  - `uber_matching_db` (matching-service)

## 🚀 Startup Sequence

### 1. Start Infrastructure (Docker)
```powershell
# Start all containers
docker-compose up -d

# Verify containers are running
docker-compose ps
```

### 2. Wait for Services to be Healthy
```powershell
# Check Kafka is ready
docker-compose logs kafka | findstr "started"

# Check PostgreSQL is ready
docker-compose logs postgres | findstr "ready"
```

### 3. Start Spring Boot Services

**Terminal 1: Ride Service**
```powershell
cd Backend/ride-service
mvn spring-boot:run
```

**Terminal 2: Driver Service**
```powershell
cd Backend/driver-service
mvn spring-boot:run
```

**Terminal 3: Matching Service**
```powershell
cd Backend/matching-service
mvn spring-boot:run
```

### 4. Start Frontend
```powershell
cd frontend
npm run dev
```

## 🔌 Port Summary

| Service              | Port  | Protocol | Access From        |
|---------------------|-------|----------|--------------------|
| React Frontend      | 3000  | HTTP     | Browser            |
| ride-service        | 8081  | HTTP     | Frontend           |
| driver-service      | 8082  | HTTP     | Frontend           |
| matching-service    | 8083  | HTTP     | (Internal only)    |
| Kafka (Broker)      | 9092  | TCP      | Services only      |
| Kafka (Controller)  | 9093  | TCP      | KRaft coordination |
| Kafka (External)    | 29092 | TCP      | Host access        |
| PostgreSQL          | 5432  | TCP      | Services only      |

## 📡 API Communication Matrix

### Frontend → Services (REST)

| Frontend Component | Calls Service    | Endpoint                          |
|-------------------|------------------|-----------------------------------|
| Rider Dashboard   | ride-service     | POST /api/rides                   |
| Rider Dashboard   | ride-service     | GET /api/rides/{id}               |
| Driver Dashboard  | driver-service   | POST /api/drivers/{id}/accept-ride|
| Driver Dashboard  | driver-service   | POST /api/drivers/{id}/start-ride |
| Driver Dashboard  | driver-service   | POST /api/drivers/{id}/complete-ride|

### Services → Kafka (Events)

| Service          | Publishes To    | Event Type           |
|-----------------|-----------------|----------------------|
| ride-service    | ride-events     | RIDE_REQUESTED       |
| matching-service| driver-events   | DRIVER_ASSIGNED      |
| driver-service  | ride-events     | RIDE_ACCEPTED        |
| driver-service  | ride-events     | RIDE_STARTED         |
| driver-service  | ride-events     | RIDE_COMPLETED       |

### Services → Database

| Service          | Database           | Operations           |
|-----------------|--------------------|--------------------|
| ride-service    | uber_ride_db       | CRUD on rides table |
| driver-service  | uber_driver_db     | CRUD on drivers table|
| matching-service| (In-memory)        | No persistent storage|

## 🧪 Testing the Complete Flow

### 1. Open Kafka UI (Optional)
```
http://localhost:8090
```
- View topics: `ride-events`, `driver-events`
- Monitor messages in real-time

### 2. Create Test Driver
```powershell
curl -X POST http://localhost:8082/api/drivers `
  -H "Content-Type: application/json" `
  -d '{
    "name": "Test Driver",
    "phoneNumber": "+1234567890",
    "licenseNumber": "DL001",
    "vehicleModel": "Toyota Camry",
    "vehiclePlate": "ABC-123",
    "currentLatitude": 37.7749,
    "currentLongitude": -122.4194,
    "status": "AVAILABLE"
  }'
```

### 3. Open Frontend
```
http://localhost:3000
```

### 4. Request Ride (as Rider)
- Login as Rider
- Fill pickup and dropoff locations
- Click "Request Ride"
- Watch the flow:
  1. Frontend → ride-service (HTTP)
  2. ride-service → Kafka (ride-events)
  3. Kafka → matching-service
  4. matching-service → Kafka (driver-events)
  5. Kafka → driver-service
  6. Frontend polls ride-service for updates

### 5. Accept Ride (as Driver)
- Login as Driver
- See incoming ride assignment
- Click "Accept"
- Watch the flow:
  1. Frontend → driver-service (HTTP)
  2. driver-service → Kafka (ride-events)
  3. Kafka → ride-service
  4. Frontend polls for updated status

## 🛡️ Key Architectural Principles

### ✅ DO
- ✅ Frontend calls Services via REST APIs
- ✅ Services communicate via Kafka events
- ✅ Services store data in PostgreSQL
- ✅ Frontend polls Services for updates
- ✅ Run Kafka and PostgreSQL in Docker
- ✅ Services handle all business logic

### ❌ DON'T
- ❌ Let Frontend connect to Kafka directly
- ❌ Let Kafka call Frontend endpoints
- ❌ Share databases between services
- ❌ Skip event-driven architecture
- ❌ Hardcode service URLs (use environment variables)

## 🔍 Monitoring & Debugging

### View Kafka Messages
```powershell
# Watch ride-events topic
docker exec -it uber-kafka kafka-console-consumer `
  --bootstrap-server localhost:9092 `
  --topic ride-events `
  --from-beginning

# Watch driver-events topic
docker exec -it uber-kafka kafka-console-consumer `
  --bootstrap-server localhost:9092 `
  --topic driver-events `
  --from-beginning
```

### Check Database
```powershell
# Connect to PostgreSQL
docker exec -it uber-postgres psql -U postgres

# Query rides
\c uber_ride_db
SELECT * FROM rides;

# Query drivers
\c uber_driver_db
SELECT * FROM drivers;
```

### Service Logs
```powershell
# Check ride-service logs
mvn spring-boot:run | findstr "Kafka"

# Check driver-service logs
mvn spring-boot:run | findstr "Kafka"

# Check matching-service logs
mvn spring-boot:run | findstr "Kafka"
```

## 🎯 Summary

**Frontend Layer**: React app with REST API calls  
**Service Layer**: 3 Spring Boot microservices with business logic  
**Message Layer**: Kafka for async event-driven communication  
**Data Layer**: PostgreSQL for persistent storage  

**Key Principle**: Frontend ↔️ Services ↔️ Kafka ↔️ Services  
**Never**: Frontend ↔️ Kafka ❌

---

**Architecture**: ✅ Event-Driven Microservices  
**Communication**: ✅ REST + Kafka  
**Infrastructure**: ✅ Docker Containers  
**Status**: ✅ Production Ready
