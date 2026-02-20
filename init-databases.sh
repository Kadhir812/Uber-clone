#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Create ride service database
    CREATE DATABASE uber_ride_db;
    
    -- Create driver service database
    CREATE DATABASE uber_driver_db;
    
    -- Grant all privileges
    GRANT ALL PRIVILEGES ON DATABASE uber_ride_db TO postgres;
    GRANT ALL PRIVILEGES ON DATABASE uber_driver_db TO postgres;
EOSQL

echo "✅ All databases created successfully!"
