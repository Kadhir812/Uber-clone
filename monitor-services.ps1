# ============================================
# Uber Services Monitoring Script
# Monitor PostgreSQL tables and Kafka topics
# ============================================

function Show-Menu {
    Clear-Host
    Write-Host "============================================" -ForegroundColor Cyan
    Write-Host "  Uber Services Monitoring Dashboard" -ForegroundColor Cyan
    Write-Host "============================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "PostgreSQL Database Monitoring:" -ForegroundColor Yellow
    Write-Host "  1. View all RIDES in rides table" -ForegroundColor White
    Write-Host "  2. View RIDES by status" -ForegroundColor White
    Write-Host "  3. View all DRIVERS in drivers table" -ForegroundColor White
    Write-Host "  4. View DRIVERS by status" -ForegroundColor White
    Write-Host "  5. Interactive PostgreSQL shell (rides DB)" -ForegroundColor White
    Write-Host "  6. Interactive PostgreSQL shell (driver DB)" -ForegroundColor White
    Write-Host ""
    Write-Host "Kafka Monitoring:" -ForegroundColor Yellow
    Write-Host "  7. List all Kafka topics" -ForegroundColor White
    Write-Host "  8. View RIDE-EVENTS topic (live)" -ForegroundColor White
    Write-Host "  9. View DRIVER-EVENTS topic (live)" -ForegroundColor White
    Write-Host "  10. View RIDE-MATCHING topic (live)" -ForegroundColor White
    Write-Host "  11. View all topics (live stream)" -ForegroundColor White
    Write-Host ""
    Write-Host "Quick Info:" -ForegroundColor Yellow
    Write-Host "  12. Show database statistics" -ForegroundColor White
    Write-Host "  13. Count records in all tables" -ForegroundColor White
    Write-Host "  14. Show recent ride activity" -ForegroundColor White
    Write-Host "  15. Show container status" -ForegroundColor White
    Write-Host ""
    Write-Host "  Q. Quit" -ForegroundColor Red
    Write-Host ""
    Write-Host "============================================" -ForegroundColor Cyan
}

function View-AllRides {
    Write-Host "`nFetching all rides..." -ForegroundColor Cyan
    docker exec -it uber-postgres psql -U postgres -d uber_ride_db -c "SELECT * FROM rides ORDER BY created_at DESC;"
    Pause
}

function View-RidesByStatus {
    Write-Host "`nAvailable statuses: REQUESTED, ASSIGNED, ACCEPTED, ONGOING, COMPLETED, CANCELLED, PAID" -ForegroundColor Yellow
    $status = Read-Host "Enter status to filter"
    Write-Host "`nFetching rides with status: $status..." -ForegroundColor Cyan
    docker exec -it uber-postgres psql -U postgres -d uber_ride_db -c "SELECT * FROM rides WHERE status = '$status' ORDER BY created_at DESC;"
    Pause
}

function View-AllDrivers {
    Write-Host "`nFetching all drivers..." -ForegroundColor Cyan
    docker exec -it uber-postgres psql -U postgres -d uber_driver_db -c "SELECT * FROM drivers ORDER BY id;"
    Pause
}

function View-DriversByStatus {
    Write-Host "`nAvailable statuses: AVAILABLE, BUSY, OFFLINE" -ForegroundColor Yellow
    $status = Read-Host "Enter status to filter"
    Write-Host "`nFetching drivers with status: $status..." -ForegroundColor Cyan
    docker exec -it uber-postgres psql -U postgres -d uber_driver_db -c "SELECT * FROM drivers WHERE status = '$status' ORDER BY id;"
    Pause
}

function Open-RidesDBShell {
    Write-Host "`nOpening PostgreSQL shell for uber_ride_db..." -ForegroundColor Cyan
    Write-Host "Useful commands:" -ForegroundColor Yellow
    Write-Host "  \dt              - List all tables" -ForegroundColor Gray
    Write-Host "  \d rides         - Describe rides table" -ForegroundColor Gray
    Write-Host "  SELECT * FROM rides LIMIT 10;" -ForegroundColor Gray
    Write-Host "  \q               - Quit" -ForegroundColor Gray
    Write-Host ""
    docker exec -it uber-postgres psql -U postgres -d uber_ride_db
}

function Open-DriverDBShell {
    Write-Host "`nOpening PostgreSQL shell for uber_driver_db..." -ForegroundColor Cyan
    Write-Host "Useful commands:" -ForegroundColor Yellow
    Write-Host "  \dt              - List all tables" -ForegroundColor Gray
    Write-Host "  \d drivers       - Describe drivers table" -ForegroundColor Gray
    Write-Host "  SELECT * FROM drivers LIMIT 10;" -ForegroundColor Gray
    Write-Host "  \q               - Quit" -ForegroundColor Gray
    Write-Host ""
    docker exec -it uber-postgres psql -U postgres -d uber_driver_db
}

function List-KafkaTopics {
    Write-Host "`nListing all Kafka topics..." -ForegroundColor Cyan
    docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --list
    Pause
}

function View-RideEventsTopic {
    Write-Host "`nStreaming RIDE-EVENTS topic (Press Ctrl+C to stop)..." -ForegroundColor Cyan
    Write-Host "This shows live ride status updates..." -ForegroundColor Yellow
    Write-Host ""
    docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic ride-events --from-beginning
}

function View-DriverEventsTopic {
    Write-Host "`nStreaming DRIVER-EVENTS topic (Press Ctrl+C to stop)..." -ForegroundColor Cyan
    Write-Host "This shows live driver status updates..." -ForegroundColor Yellow
    Write-Host ""
    docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic driver-events --from-beginning
}

function View-RideMatchingTopic {
    Write-Host "`nStreaming RIDE-MATCHING topic (Press Ctrl+C to stop)..." -ForegroundColor Cyan
    Write-Host "This shows ride matching events..." -ForegroundColor Yellow
    Write-Host ""
    docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic ride-matching --from-beginning
}

function View-AllTopicsLive {
    Write-Host "`nSelect topic to stream:" -ForegroundColor Yellow
    Write-Host "  1. ride-events" -ForegroundColor White
    Write-Host "  2. driver-events" -ForegroundColor White
    Write-Host "  3. ride-matching" -ForegroundColor White
    $choice = Read-Host "Enter choice"
    
    $topic = switch ($choice) {
        "1" { "ride-events" }
        "2" { "driver-events" }
        "3" { "ride-matching" }
        default { "ride-events" }
    }
    
    Write-Host "`nStreaming $topic (Press Ctrl+C to stop)..." -ForegroundColor Cyan
    docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic $topic --from-beginning
}

function Show-DatabaseStats {
    Write-Host "`n=== Database Statistics ===" -ForegroundColor Cyan
    
    Write-Host "`nRides Database:" -ForegroundColor Yellow
    docker exec -it uber-postgres psql -U postgres -d uber_ride_db -c "
        SELECT 
            status, 
            COUNT(*) as count,
            ROUND(AVG(fare), 2) as avg_fare,
            SUM(fare) as total_fare
        FROM rides 
        GROUP BY status 
        ORDER BY count DESC;
    "
    
    Write-Host "`nDrivers Database:" -ForegroundColor Yellow
    docker exec -it uber-postgres psql -U postgres -d uber_driver_db -c "
        SELECT 
            status, 
            COUNT(*) as count
        FROM drivers 
        GROUP BY status;
    "
    
    Pause
}

function Count-AllRecords {
    Write-Host "`n=== Record Counts ===" -ForegroundColor Cyan
    
    Write-Host "`nRides:" -ForegroundColor Yellow
    docker exec -it uber-postgres psql -U postgres -d uber_ride_db -c "SELECT COUNT(*) as total_rides FROM rides;"
    
    Write-Host "`nDrivers:" -ForegroundColor Yellow
    docker exec -it uber-postgres psql -U postgres -d uber_driver_db -c "SELECT COUNT(*) as total_drivers FROM drivers;"
    
    Pause
}

function Show-RecentActivity {
    Write-Host "`n=== Recent Ride Activity ===" -ForegroundColor Cyan
    docker exec -it uber-postgres psql -U postgres -d uber_ride_db -c "
        SELECT 
            id,
            user_id,
            driver_id,
            status,
            fare,
            created_at
        FROM rides 
        ORDER BY created_at DESC 
        LIMIT 10;
    "
    
    Write-Host "`n=== Active Drivers ===" -ForegroundColor Cyan
    docker exec -it uber-postgres psql -U postgres -d uber_driver_db -c "
        SELECT 
            id,
            name,
            phone,
            status,
            current_ride_id
        FROM drivers 
        WHERE status != 'OFFLINE'
        ORDER BY id;
    "
    
    Pause
}

function Show-ContainerStatus {
    Write-Host "`n=== Docker Container Status ===" -ForegroundColor Cyan
    Write-Host ""
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" --filter "name=uber-postgres" --filter "name=kafka" --filter "name=zookeeper"
    Write-Host ""
    Pause
}

# Main Loop
do {
    Show-Menu
    $choice = Read-Host "Enter your choice"
    
    switch ($choice) {
        "1" { View-AllRides }
        "2" { View-RidesByStatus }
        "3" { View-AllDrivers }
        "4" { View-DriversByStatus }
        "5" { Open-RidesDBShell }
        "6" { Open-DriverDBShell }
        "7" { List-KafkaTopics }
        "8" { View-RideEventsTopic }
        "9" { View-DriverEventsTopic }
        "10" { View-RideMatchingTopic }
        "11" { View-AllTopicsLive }
        "12" { Show-DatabaseStats }
        "13" { Count-AllRecords }
        "14" { Show-RecentActivity }
        "15" { Show-ContainerStatus }
        "q" { 
            Write-Host "`nExiting..." -ForegroundColor Green
            exit 
        }
        "Q" { 
            Write-Host "`nExiting..." -ForegroundColor Green
            exit 
        }
        default {
            Write-Host "`nInvalid choice. Please try again." -ForegroundColor Red
            Start-Sleep -Seconds 2
        }
    }
} while ($true)
