# 🚀 Quick Start Guide - Uber Ride Booking System

## ⚡ Start Everything in 5 Minutes

### Prerequisites Checklist
- ✅ Docker Desktop installed and running
- ✅ Java 17 installed
- ✅ Maven installed
- ✅ Node.js 18+ installed
- ✅ Git Bash or PowerShell

---

## 🐳 Step 1: Start Docker Infrastructure (2 minutes)

Open PowerShell in the project root:

```powershell
# Navigate to project root
cd "d:\New Folder\Uber"

# Start Kafka (KRaft mode), PostgreSQL
docker-compose up -d

# Wait for containers to be healthy (about 30 seconds)
Start-Sleep -Seconds 30

# Verify all containers are running
docker-compose ps
```

Expected output:
```
NAME              IMAGE                            STATUS
uber-kafka        confluentinc/cp-kafka:7.5.0      Up (healthy)
uber-postgres     postgres:16-alpine               Up (healthy)
```

**Note**: This project now uses Kafka with KRaft mode (no Zookeeper needed), which reduces CPU usage significantly.

---

## 🍃 Step 2: Start Backend Services (1 minute each)

### Terminal 1 - Ride Service (Port 8081)
```powershell
cd "d:\New Folder\Uber\Backend\ride-service"
mvn spring-boot:run
```
Wait for: `Started RideServiceApplication in X seconds`

### Terminal 2 - Driver Service (Port 8082)
```powershell
cd "d:\New Folder\Uber\Backend\driver-service"
mvn spring-boot:run
```
Wait for: `Started DriverApplication in X seconds`

### Terminal 3 - Matching Service (Port 8083)
```powershell
cd "d:\New Folder\Uber\Backend\matching-service"
mvn spring-boot:run
```
Wait for: `Started MatchingApplication in X seconds`

---

## ⚛️ Step 3: Start Frontend (1 minute)

### Terminal 4 - React Frontend (Port 3000)
```powershell
cd "d:\New Folder\Uber\frontend"
npm run dev
```

Wait for:
```
  VITE v7.3.1  ready in X ms

  ➜  Local:   http://localhost:3000/
  ➜  Network: use --host to expose
```

---

## 🧪 Step 4: Test the System

### Create a Test Driver

Open a new terminal:
```powershell
curl -X POST http://localhost:8082/api/drivers `
  -H "Content-Type: application/json" `
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
```

Expected response:
```json
{
  "id": 1,
  "name": "John Driver",
  "status": "AVAILABLE",
  "rating": 5.0,
  "totalRides": 0
}
```

### Test the Full Ride Flow

1. **Open Frontend**: http://localhost:3000

2. **Login as Rider**:
   - Click "Continue as Rider"
   - Username: `rider1`

3. **Request a Ride**:
   - Pickup: `123 Main Street`
   - Dropoff: `456 Oak Avenue`
   - Click "Request Ride"
   - Watch status change: PENDING → ASSIGNED → ACCEPTED → IN_PROGRESS

4. **Switch to Driver** (new browser tab):
   - Open http://localhost:3000
   - Click "Continue as Driver"
   - Username: `driver1`

5. **Accept the Ride**:
   - See the incoming ride
   - Click "Accept Ride"
   - Click "Start Ride"
   - Click "Complete Ride"

6. **Back to Rider Tab**:
   - Watch status update to COMPLETED
   - See ride in history

---

## 🔍 Monitoring Tools

### Kafka UI (Monitor Events)
```
http://localhost:8090
```
- View topics: `ride-events`, `driver-events`
- See messages in real-time
- Monitor consumer groups

### Check Backend APIs

**Ride Service Health**:
```powershell
curl http://localhost:8081/api/rides/1
```

**Driver Service Health**:
```powershell
curl http://localhost:8082/api/drivers/1
```

**Get Available Drivers**:
```powershell
curl http://localhost:8082/api/drivers/available
```

---

## 📊 Watch Kafka Messages Live

### Terminal 5 - Watch Ride Events
```powershell
docker exec -it uber-kafka kafka-console-consumer `
  --bootstrap-server localhost:9092 `
  --topic ride-events `
  --from-beginning
```

### Terminal 6 - Watch Driver Events
```powershell
docker exec -it uber-kafka kafka-console-consumer `
  --bootstrap-server localhost:9092 `
  --topic driver-events `
  --from-beginning
```

---

## 🛑 Stop Everything

### Stop Services (Ctrl+C in each terminal)
1. Close Frontend (Terminal 4)
2. Close Matching Service (Terminal 3)
3. Close Driver Service (Terminal 2)
4. Close Ride Service (Terminal 1)

### Stop Docker Containers
```powershell
cd "d:\New Folder\Uber"
docker-compose down
```

### Stop Docker and Remove Data
```powershell
docker-compose down -v  # Removes volumes (database data)
```

---

## 🐛 Troubleshooting

### Issue: Containers won't start
```powershell
# Check Docker is running
docker ps

# Check logs
docker-compose logs kafka
docker-compose logs postgres
```

### Issue: Services can't connect to Kafka
```powershell
# Restart Kafka
docker-compose restart kafka

# Wait 30 seconds
Start-Sleep -Seconds 30
```

### Issue: Database connection errors
```powershell
# Check PostgreSQL is ready
docker exec -it uber-postgres psql -U postgres -c "\l"

# Restart database
docker-compose restart postgres
```

### Issue: Port already in use
```powershell
# Find process using port
netstat -ano | findstr :9092  # Kafka
netstat -ano | findstr :5432  # PostgreSQL
netstat -ano | findstr :8081  # ride-service
netstat -ano | findstr :8082  # driver-service
netstat -ano | findstr :8083  # matching-service
netstat -ano | findstr :3000  # frontend

# Kill process by PID
taskkill /PID <PID> /F
```

### Issue: Frontend shows connection error
```powershell
# Verify services are running
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
```

---

## 📁 Project Structure

```
Uber/
├── docker-compose.yml          ← Start infrastructure
├── init-databases.sql          ← Database initialization
├── ARCHITECTURE.md             ← Architecture details
├── QUICK_START.md             ← This file
│
├── Backend/
│   ├── ride-service/          ← Port 8081
│   ├── driver-service/        ← Port 8082
│   └── matching-service/      ← Port 8083
│
└── frontend/                  ← Port 3000
    ├── src/
    │   ├── pages/
    │   ├── components/
    │   └── services/
    └── package.json
```

---

## ✅ Success Checklist

After starting everything, verify:

- [ ] Docker containers running (4 containers)
- [ ] ride-service accessible at http://localhost:8081
- [ ] driver-service accessible at http://localhost:8082
- [ ] matching-service accessible at http://localhost:8083
- [ ] Frontend accessible at http://localhost:3000
- [ ] Kafka UI accessible at http://localhost:8090
- [ ] Can create a driver via API
- [ ] Can request a ride from frontend
- [ ] Kafka events visible in Kafka UI
- [ ] Complete ride flow works end-to-end

---

## 🎯 What Each Component Does

| Component        | Purpose                              | You Access It Via        |
|-----------------|--------------------------------------|--------------------------|
| Frontend        | User interface (Rider/Driver)        | Browser: :3000           |
| ride-service    | Manage rides, handle requests        | REST API: :8081          |
| driver-service  | Manage drivers, ride lifecycle       | REST API: :8082          |
| matching-service| Match riders with drivers            | Kafka only (internal)    |
| Kafka           | Event streaming between services     | Services only            |
| PostgreSQL      | Store rides and drivers              | Services only            |
| Kafka UI        | Monitor Kafka topics                 | Browser: :8090           |

---

## 🎉 You're Ready!

**Frontend**: http://localhost:3000  
**Kafka UI**: http://localhost:8090  
**Architecture**: See [ARCHITECTURE.md](ARCHITECTURE.md)

**Happy Testing! 🚗💨**
