# Matching Service

The Matching Service is responsible for matching ride requests with available drivers in the Uber-like ride booking system.

## Architecture

This service acts as the bridge between riders and drivers:

```
Rider Request → Ride Service → [Kafka: ride-events] → Matching Service → [Kafka: driver-events] → Ride Service
```

## Responsibilities

1. **Listen to Ride Requests**: Consumes `RIDE_REQUESTED` events from the `ride-events` Kafka topic
2. **Find Available Drivers**: Searches for the best available driver (currently mock implementation)
3. **Assign Drivers**: Publishes `DRIVER_ASSIGNED` events to the `driver-events` Kafka topic
4. **Manage Driver Availability**: Tracks which drivers are available or busy
5. **Handle Ride Completion**: Releases drivers back to the available pool when rides are completed

## Kafka Topics

### Consumes From:
- **ride-events**: Listens for:
  - `RIDE_REQUESTED` - New ride request from rider
  - `RIDE_COMPLETED` - Ride finished, release driver
  - `RIDE_CANCELLED` - Ride cancelled, release driver

### Produces To:
- **driver-events**: Publishes:
  - `DRIVER_ASSIGNED` - Driver assigned to ride
  - `DRIVER_REJECTED` - Driver rejected the assignment
  - `NO_DRIVER_AVAILABLE` - No drivers available

## Technology Stack

- **Spring Boot 4.0.3**
- **Spring Kafka** - Event streaming
- **Java 17**
- **Lombok** - Reduce boilerplate

## Running the Service

### Prerequisites

1. Kafka running on `localhost:9092`
2. Topics created:
   ```bash
   kafka-topics.bat --create --topic ride-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
   kafka-topics.bat --create --topic driver-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
   ```

### Start the Service

```bash
cd Backend/matching-service
mvn clean install
mvn spring-boot:run
```

The service will start on **port 8083**.

## Configuration

Edit `src/main/resources/application.yml`:

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: matching-service-group
    
server:
  port: 8083
```

## How It Works

### Flow Diagram

```
1. Rider requests ride via frontend
   ↓
2. Ride Service creates ride (status: REQUESTED)
   ↓
3. Ride Service publishes RIDE_REQUESTED to ride-events
   ↓
4. Matching Service consumes event
   ↓
5. Matching Service finds available driver (mock: driver 2001-2010)
   ↓
6. Matching Service publishes DRIVER_ASSIGNED to driver-events
   ↓
7. Ride Service consumes event and updates ride
   ↓
8. Frontend polls ride status and sees driver assigned
```

### Mock Driver Pool

Currently uses mock drivers (IDs: 2001-2010) for testing. In production, this would:
- Query the Driver Service API for real available drivers
- Use geolocation to find nearest drivers
- Consider driver ratings, acceptance rates, etc.
- Implement surge pricing logic

## API Integration (Future)

In production, the matching service would integrate with:

```java
// Call Driver Service to get available drivers
GET http://localhost:8082/api/drivers/available?lat={lat}&lng={lng}&radius={radius}

// Notify driver of new assignment
POST http://localhost:8082/api/drivers/{driverId}/assignments
```

## Testing

### Manual Testing

1. **Start services in order:**
   ```bash
   # Terminal 1: Kafka (KRaft mode - no Zookeeper needed)
   # With Docker: docker-compose up -d kafka
   # Or local: bin/kafka-server-start.sh config/kraft/server.properties
   
   # Terminal 2: Ride Service
   cd Backend/ride-service && mvn spring-boot:run
   
   # Terminal 3: Matching Service
   cd Backend/matching-service && mvn spring-boot:run
   
   # Terminal 4: Frontend
   cd frontend && npm run dev
   ```

2. **Test flow:**
   - Login as Rider (ID: 1001)
   - Request a ride
   - Check logs: Matching service should find driver 2001-2010
   - Ride status should update to ASSIGNED

### Monitor Kafka Messages

```bash
# Monitor ride-events topic
kafka-console-consumer.bat --topic ride-events --from-beginning --bootstrap-server localhost:9092

# Monitor driver-events topic
kafka-console-consumer.bat --topic driver-events --from-beginning --bootstrap-server localhost:9092
```

## Logs

The service logs show:

```
INFO  Received ride event: RIDE_REQUESTED for ride ID: 1
INFO  Processing ride request for ride ID: 1
INFO  Found driver 2003 for ride 1
INFO  Published driver assignment event for ride: 1
```

## Future Enhancements

1. **Real Driver Integration**: Query actual driver service
2. **Geolocation Matching**: Use lat/lng for distance calculation
3. **Smart Matching Algorithm**:
   - Driver rating
   - Acceptance rate
   - Estimated arrival time
   - Surge pricing zones
4. **Driver Preferences**: Car type, long trips, etc.
5. **Retry Logic**: If driver rejects, find another
6. **Timeout Handling**: Cancel if no driver found in X minutes
7. **Monitoring**: Metrics for match success rate, time to assign

## Troubleshooting

**Issue**: No driver assigned
- Check if Kafka is running
- Verify topics exist
- Check Matching Service logs
- Ensure mock drivers are initialized

**Issue**: Events not consumed
- Verify Kafka consumer group ID
- Check topic names match exactly
- Ensure JSON deserialization works

**Issue**: Driver not released after ride
- Check RIDE_COMPLETED event is published
- Verify status updates trigger driver release

## Project Structure

```
matching-service/
├── src/main/java/com/uber/matching/
│   ├── MatchingApplication.java          # Main entry point
│   ├── config/
│   │   └── KafkaConfig.java             # Kafka configuration
│   ├── dto/
│   │   └── RideEventDTO.java            # Event data transfer object
│   ├── service/
│   │   └── MatchingService.java         # Core matching logic
│   └── kafka/
│       ├── consumer/
│       │   └── RideRequestedConsumer.java  # Consume ride requests
│       └── producer/
│           └── RideAssignedProducer.java   # Publish assignments
├── src/main/resources/
│   └── application.yml                   # Configuration
└── pom.xml                              # Maven dependencies
```

## License

MIT
