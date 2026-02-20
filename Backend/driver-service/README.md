# Driver Service 🚗

[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-Latest-black.svg)](https://kafka.apache.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Latest-blue.svg)](https://www.postgresql.org/)

The **Driver Service** is a core microservice in the Uber-like ride booking system that manages driver operations, availability, and ride lifecycle management. It handles driver registration, location tracking, ride assignments, and communicates with other services via Apache Kafka.

## 🎯 Features

### Driver Management
- ✅ **Driver Registration**: Create and manage driver profiles with vehicle details
- ✅ **Location Tracking**: Real-time driver location updates
- ✅ **Availability Management**: Track driver status (AVAILABLE, BUSY, OFFLINE, ON_THE_WAY)
- ✅ **Rating System**: Maintain driver ratings and total ride counts

### Ride Operations
- ✅ **Accept Rides**: Drivers can accept assigned rides
- ✅ **Start Rides**: Mark when passenger is picked up
- ✅ **Complete Rides**: Finalize rides and update driver stats
- ✅ **Cancel Rides**: Handle ride cancellations

### Event-Driven Communication
- ✅ **Kafka Consumer**: Listens to `driver-events` topic for ride assignments
- ✅ **Kafka Producers**: Publishes ride lifecycle events (ACCEPTED, STARTED, COMPLETED)

## 🏗️ Architecture

```
driver-service/
├── controller/          # REST API endpoints
├── service/            # Business logic
├── repository/         # Database access layer (JPA)
├── model/             # Entity classes
├── dto/               # Data Transfer Objects
├── kafka/
│   ├── consumer/      # Kafka event consumers
│   └── producer/      # Kafka event producers
└── config/            # Configuration classes
```

## 🚀 Technology Stack

- **Java 17**: Programming language
- **Spring Boot 4.0.3**: Application framework
- **Spring Data JPA**: Database abstraction
- **PostgreSQL**: Relational database
- **Apache Kafka**: Event streaming platform
- **Lombok**: Boilerplate code reduction
- **Maven**: Dependency management

## 📋 Prerequisites

Before running the driver service, ensure you have:

1. **Java 17** or higher installed
2. **Maven 3.6+** for building the project
3. **PostgreSQL** database running
4. **Apache Kafka** and Zookeeper running
5. **Kafka Topics** created:
   - `driver-events` (for receiving ride assignments)
   - `ride-events` (for publishing ride status updates)

## ⚙️ Configuration

### Database Setup

Create a PostgreSQL database:

```bash
createdb uber_driver_db
```

### Application Configuration

Update `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/uber_driver_db
    username: postgres
    password: your_password
  kafka:
    bootstrap-servers: localhost:9092
server:
  port: 8082
```

## 📦 Installation & Running

### 1. Build the Project

```bash
cd Backend/driver-service
mvn clean install
```

### 2. Run the Service

```bash
mvn spring-boot:run
```

Or run the JAR directly:

```bash
java -jar target/driver-0.0.1-SNAPSHOT.jar
```

The service will start on **port 8082**.

## 🔌 API Endpoints

### Driver Management

#### Create Driver
```http
POST http://localhost:8082/api/drivers
Content-Type: application/json

{
  "name": "John Doe",
  "phoneNumber": "+1234567890",
  "licenseNumber": "DL12345",
  "vehicleModel": "Toyota Camry",
  "vehiclePlate": "ABC-1234",
  "currentLatitude": 37.7749,
  "currentLongitude": -122.4194,
  "status": "AVAILABLE"
}
```

#### Get Driver by ID
```http
GET http://localhost:8082/api/drivers/{driverId}
```

#### Get All Drivers
```http
GET http://localhost:8082/api/drivers
```

#### Get Available Drivers
```http
GET http://localhost:8082/api/drivers/available
```

#### Get Available Drivers Near Location
```http
GET http://localhost:8082/api/drivers/available?latitude=37.7749&longitude=-122.4194&radiusKm=5
```

### Ride Operations

#### Accept Ride
```http
POST http://localhost:8082/api/drivers/{driverId}/accept-ride
Content-Type: application/json

{
  "rideId": 101
}
```

#### Start Ride
```http
POST http://localhost:8082/api/drivers/{driverId}/start-ride
Content-Type: application/json

{
  "rideId": 101
}
```

#### Complete Ride
```http
POST http://localhost:8082/api/drivers/{driverId}/complete-ride
Content-Type: application/json

{
  "rideId": 101
}
```

#### Update Driver Location
```http
PUT http://localhost:8082/api/drivers/{driverId}/location
Content-Type: application/json

{
  "latitude": 37.7749,
  "longitude": -122.4194
}
```

#### Update Driver Status
```http
PUT http://localhost:8082/api/drivers/{driverId}/status
Content-Type: application/json

{
  "status": "OFFLINE"
}
```

## 📨 Kafka Events

### Consumed Events (from `driver-events`)

#### DRIVER_ASSIGNED
```json
{
  "rideId": 101,
  "driverId": 2001,
  "eventType": "DRIVER_ASSIGNED",
  "status": "ASSIGNED",
  "pickupLocation": "123 Main St",
  "dropoffLocation": "456 Oak Ave",
  "fare": 25.50,
  "timestamp": 1708470000000
}
```

#### RIDE_CANCELLED
```json
{
  "rideId": 101,
  "driverId": 2001,
  "eventType": "RIDE_CANCELLED",
  "status": "CANCELLED",
  "timestamp": 1708470000000
}
```

### Published Events (to `ride-events`)

#### RIDE_ACCEPTED
```json
{
  "rideId": 101,
  "driverId": 2001,
  "eventType": "RIDE_ACCEPTED",
  "status": "ACCEPTED",
  "message": "Driver accepted the ride",
  "timestamp": 1708470000000
}
```

#### RIDE_STARTED
```json
{
  "rideId": 101,
  "driverId": 2001,
  "eventType": "RIDE_STARTED",
  "status": "IN_PROGRESS",
  "message": "Driver has picked up the passenger",
  "timestamp": 1708470000000
}
```

#### RIDE_COMPLETED
```json
{
  "rideId": 101,
  "driverId": 2001,
  "eventType": "RIDE_COMPLETED",
  "status": "COMPLETED",
  "fare": 25.50,
  "message": "Driver has completed the ride",
  "timestamp": 1708470000000
}
```

## 🔄 Event Flow

```
1. Matching Service → driver-events → DRIVER_ASSIGNED
   ↓
2. Driver Service receives assignment
   ↓
3. Driver accepts via REST API
   ↓
4. Driver Service → ride-events → RIDE_ACCEPTED
   ↓
5. Driver starts ride via REST API
   ↓
6. Driver Service → ride-events → RIDE_STARTED
   ↓
7. Driver completes ride via REST API
   ↓
8. Driver Service → ride-events → RIDE_COMPLETED
```

## 🗄️ Database Schema

### Drivers Table
```sql
CREATE TABLE drivers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    license_number VARCHAR(50) UNIQUE NOT NULL,
    vehicle_model VARCHAR(100) NOT NULL,
    vehicle_plate VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    current_latitude DOUBLE PRECISION,
    current_longitude DOUBLE PRECISION,
    rating DOUBLE PRECISION,
    total_rides INTEGER DEFAULT 0,
    current_ride_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

## 🧪 Testing

### Create Test Drivers

```bash
# Create driver 1
curl -X POST http://localhost:8082/api/drivers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Driver",
    "phoneNumber": "+1234567890",
    "licenseNumber": "DL001",
    "vehicleModel": "Toyota Camry",
    "vehiclePlate": "ABC-123",
    "currentLatitude": 37.7749,
    "currentLongitude": -122.4194,
    "status": "AVAILABLE"
  }'

# Create driver 2
curl -X POST http://localhost:8082/api/drivers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Driver",
    "phoneNumber": "+1234567891",
    "licenseNumber": "DL002",
    "vehicleModel": "Honda Accord",
    "vehiclePlate": "XYZ-789",
    "currentLatitude": 37.7849,
    "currentLongitude": -122.4094,
    "status": "AVAILABLE"
  }'
```

### Test Ride Flow

```bash
# 1. Accept ride
curl -X POST http://localhost:8082/api/drivers/1/accept-ride \
  -H "Content-Type: application/json" \
  -d '{"rideId": 101}'

# 2. Start ride
curl -X POST http://localhost:8082/api/drivers/1/start-ride \
  -H "Content-Type: application/json" \
  -d '{"rideId": 101}'

# 3. Complete ride
curl -X POST http://localhost:8082/api/drivers/1/complete-ride \
  -H "Content-Type: application/json" \
  -d '{"rideId": 101}'
```

## 🐛 Troubleshooting

### Issue: Cannot connect to PostgreSQL
**Solution**: Ensure PostgreSQL is running and credentials in `application.yml` are correct.

```bash
psql -U postgres -c "\l"  # List databases
```

### Issue: Kafka connection errors
**Solution**: Verify Kafka and Zookeeper are running:

```bash
# Check Zookeeper
netstat -an | findstr 2181

# Check Kafka
netstat -an | findstr 9092
```

### Issue: Driver not receiving ride assignments
**Solution**: Check Kafka topic exists and consumer group is active:

```bash
kafka-topics.sh --list --bootstrap-server localhost:9092
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group driver-service-group
```

## 📊 Monitoring

View application logs:
```bash
tail -f logs/driver-service.log
```

Monitor Kafka consumer lag:
```bash
kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --describe --group driver-service-group
```

## 🔗 Integration with Other Services

- **matching-service**: Receives ride assignments from matching service
- **ride-service**: Publishes ride status updates to ride service
- **frontend**: REST APIs consumed by driver dashboard

## 📝 Driver Status Flow

```
AVAILABLE → ON_THE_WAY → BUSY → AVAILABLE
     ↓                              ↑
   OFFLINE ──────────────────────────┘
```

- **AVAILABLE**: Driver is ready to accept rides
- **ON_THE_WAY**: Driver is going to pickup location
- **BUSY**: Driver is currently on a ride
- **OFFLINE**: Driver is not available

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

This project is part of the Uber Ride Booking System.

---

**Service Status**: ✅ Ready for Production  
**Port**: 8082  
**Database**: PostgreSQL (uber_driver_db)  
**Kafka Topics**: driver-events (consumer), ride-events (producer)
