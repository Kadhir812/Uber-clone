# Complete System Setup Guide

## Full Uber Ride Booking System Architecture

```
┌─────────────────┐
│   Frontend      │ (Port 3000)
│   React App     │
└────────┬────────┘
         │ HTTP Polling
         ▼
┌─────────────────┐     ┌──────────────────┐
│  Ride Service   │────▶│   Kafka Topics   │
│   Port 8081     │     │  - ride-events   │
└─────────────────┘     │  - driver-events │
                        └─────────┬────────┘
                                  │
                        ┌─────────▼────────┐
                        │ Matching Service │
                        │   Port 8083      │
                        └──────────────────┘
```

## Prerequisites

1. **Java 17+** installed
2. **Maven 3.6+** installed
3. **Node.js 18+** and npm installed
4. **Apache Kafka** installed (or Docker)

### Install Kafka (Windows)

Download from: https://kafka.apache.org/downloads

Extract to `C:\kafka` (or any location)

**Note**: This project now uses Docker with Kafka in KRaft mode (no Zookeeper needed). For Docker setup, see the Docker section below.

## Step-by-Step Startup

### Option A: Using Docker (Recommended)

#### Start all services with Docker Compose
```powershell
# Navigate to project root
cd "d:\New Folder\Uber"

# Start Kafka and PostgreSQL
docker-compose up -d kafka postgres

# Wait 30 seconds for Kafka to initialize
Start-Sleep -Seconds 30

# Start all services
docker-compose up -d
```

### Option B: Local Development (Manual Kafka)

#### Terminal 1: Start Kafka in KRaft Mode (No Zookeeper)
```powershell
cd C:\kafka

# Generate a cluster ID (first time only)
.\bin\windows\kafka-storage.bat random-uuid
# Copy the UUID output

# Format the storage directory (first time only)
.\bin\windows\kafka-storage.bat format -t <YOUR-UUID> -c .\config\kraft\server.properties

# Start Kafka in KRaft mode
.\bin\windows\kafka-server-start.bat .\config\kraft\server.properties
```

#### Terminal 2: Create Kafka Topics
```powershell
cd C:\kafka

# Create ride-events topic
.\bin\windows\kafka-topics.bat --create --topic ride-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

# Create driver-events topic
.\bin\windows\kafka-topics.bat --create --topic driver-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

# Verify topics created
.\bin\windows\kafka-topics.bat --list --bootstrap-server localhost:9092
```

**Note**: With Docker, topics are auto-created when services start.

### 2. Start Backend Services

#### Terminal 3: Ride Service (Port 8081)
```powershell
cd "d:\New Folder\Uber\Backend\ride-service"
mvn clean install
mvn spring-boot:run
```

Wait for: `Tomcat started on port(s): 8081`

#### Terminal 4: Matching Service (Port 8083)
```powershell
cd "d:\New Folder\Uber\Backend\matching-service"
mvn clean install
mvn spring-boot:run
```

Wait for: `Tomcat started on port(s): 8083`

#### Terminal 5 (Optional): Driver Service (Port 8082)
```powershell
cd "d:\New Folder\Uber\Backend\driver-service"
mvn clean install
mvn spring-boot:run
```

### 3. Start Frontend

#### Terminal 6: React Frontend (Port 3000)
```powershell
cd "d:\New Folder\Uber\frontend"
npm install  # Only first time
npm run dev
```

Open browser: http://localhost:3000

## Test Complete Flow

### Scenario 1: Successful Ride Request & Assignment

1. **Open Browser Tab 1 (Rider)**
   - Go to http://localhost:3000
   - Click "Rider"
   - Enter User ID: `1001`
   - Click "Continue as rider"

2. **Request Ride**
   - Pickup: "123 Main Street, New York"
   - Dropoff: "456 Park Avenue, New York"
   - Click "Request Ride"

3. **Watch the Magic! 🎯**
   
   **Ride Service Log (Terminal 4):**
   ```
   INFO  Creating new ride request for user: 1001
   INFO  Published RIDE_REQUESTED event for ride ID: 1
   ```

   **Matching Service Log (Terminal 5):**
   ```
   INFO  Received ride event: RIDE_REQUESTED for ride ID: 1
   INFO  Finding driver for ride: 1
   INFO  Found driver 2003 for ride 1
   INFO  Published driver assignment event for ride: 1
   ```

   **Ride Service Log (Terminal 4) - Again:**
   ```
   INFO  Received driver event: DRIVER_ASSIGNED
   INFO  Assigning driver 2003 to ride 1
   INFO  Ride 1 status updated to: ACCEPTED
   ```

4. **Frontend Updates**
   - Status changes: REQUESTED → ASSIGNED → ACCEPTED
   - Shows Driver ID: 2003
   - Shows estimated ETA

5. **Open Browser Tab 2 (Driver)**
   - Go to http://localhost:3000
   - Click "Driver"
   - Enter Driver ID: `2003`
   - Click "Continue as driver"
   - Should see the assigned ride
   - Click "Accept Ride" → "Start Ride" → "Complete Ride"

6. **Back to Tab 1 (Rider)**
   - Status updates to: ONGOING → COMPLETED
   - Payment button appears
   - Click "Pay"
   - Status changes to: PAID

## Monitor Kafka Messages in Real-Time

### Terminal 8: Monitor ride-events topic
```powershell
cd C:\kafka
.\bin\windows\kafka-console-consumer.bat --topic ride-events --from-beginning --bootstrap-server localhost:9092
```

### Terminal 9: Monitor driver-events topic
```powershell
cd C:\kafka
.\bin\windows\kafka-console-consumer.bat --topic driver-events --from-beginning --bootstrap-server localhost:9092
```

## Architecture Deep Dive

### Event Flow Diagram

```
Rider → [POST /api/rides] → Ride Service
                                 ↓
                          [RIDE_REQUESTED]
                                 ↓
                         (ride-events topic)
                                 ↓
                          Matching Service
                                 ↓
                       [Find Available Driver]
                                 ↓
                          [DRIVER_ASSIGNED]
                                 ↓
                        (driver-events topic)
                                 ↓
                           Ride Service
                                 ↓
                      [Update Ride Status]
                                 ↓
                  Frontend Polls & Updates UI
```

### Kafka Topics & Events

#### ride-events Topic
**Published by**: Ride Service  
**Consumed by**: Matching Service  
**Events**:
- `RIDE_REQUESTED` - New ride requested
- `RIDE_STATUS_UPDATED` - Status changed
- `RIDE_COMPLETED` - Ride finished
- `RIDE_CANCELLED` - Ride cancelled

#### driver-events Topic
**Published by**: Matching Service  
**Consumed by**: Ride Service  
**Events**:
- `DRIVER_ASSIGNED` - Driver matched to ride
- `DRIVER_ACCEPTED` - Driver accepted (manual)
- `DRIVER_REJECTED` - Driver rejected (manual)
- `NO_DRIVER_AVAILABLE` - No drivers found

## Troubleshooting

### Issue: "Connection refused" on Kafka
**Solution**: 
1. If using Docker: Ensure Kafka container is running (`docker ps`)
2. If using local Kafka: Ensure Kafka is running in KRaft mode
3. Wait 30-60 seconds for Kafka to fully initialize

### Issue: Ride stuck in REQUESTED status
**Solution**: 
1. Check Matching Service is running
2. Check Kafka topics exist
3. Look at Matching Service logs for errors

### Issue: Driver not assigned
**Solution**:
1. Verify Matching Service has initialized mock drivers (2001-2010)
2. Check logs: `Initialized 10 mock drivers`
3. Restart Matching Service

### Issue: Frontend not updating
**Solution**:
1. Check browser console for errors
2. Verify backend is on port 8081
3. Clear browser cache
4. Check localStorage has current ride ID

## Service URLs

- **Frontend**: http://localhost:3000 (Docker: http://localhost:3000)
- **Ride Service**: http://localhost:8081
  - Health: http://localhost:8081/actuator/health
  - API: http://localhost:8081/api/rides
- **Matching Service**: http://localhost:8083
- **Driver Service**: http://localhost:8082
- **Kafka**: localhost:9092 (external), kafka:9092 (internal)
- **Kafka Controller**: localhost:9093 (KRaft mode)
- **PostgreSQL**: localhost:5432

## Quick Verification Commands

```powershell
# Check if services are running
curl http://localhost:8081/api/rides
curl http://localhost:8083

# Check Kafka topics
cd C:\kafka
.\bin\windows\kafka-topics.bat --list --bootstrap-server localhost:9092

# Check topic messages count
.\bin\windows\kafka-run-class.bat kafka.tools.GetOffsetShell --broker-list localhost:9092 --topic ride-events
```

## Shutdown Sequence

### Docker Shutdown
```powershell
# Stop all services
docker-compose down

# Or stop and remove volumes
docker-compose down -v
```

### Local Development Shutdown
Shut down in reverse order:

1. Stop Frontend (Ctrl+C in Terminal 6)
2. Stop Driver Service (Ctrl+C in Terminal 5)
3. Stop Matching Service (Ctrl+C in Terminal 4)
4. Stop Ride Service (Ctrl+C in Terminal 3)
5. Stop Kafka (Ctrl+C in Terminal 1)

## Production Deployment Notes

For production, you would:

1. **Use Managed Kafka**: AWS MSK, Confluent Cloud, Azure Event Hubs
2. **Add Database**: PostgreSQL for Ride/Driver services
3. **Add Redis**: For caching driver locations
4. **Use Service Mesh**: Istio or Linkerd
5. **Add API Gateway**: Kong, AWS API Gateway
6. **Implement Real Geolocation**: Google Maps API
7. **Add Authentication**: JWT, OAuth2
8. **Add Monitoring**: Prometheus, Grafana, ELK Stack
9. **Use Docker & Kubernetes**: For orchestration
10. **Add Load Balancer**: Nginx, AWS ALB

## Next Steps

1. ✅ Test basic flow (Rider → Driver assignment)
2. ✅ Test driver acceptance flow
3. ✅ Test ride completion and payment
4. 🔄 Add real driver service integration
5. 🔄 Add geolocation-based matching
6. 🔄 Add WebSocket for real-time updates
7. 🔄 Add notification service
8. 🔄 Add payment service integration

Enjoy your fully functional event-driven Uber-like ride booking system! 🚗💨
