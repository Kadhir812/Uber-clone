#!/bin/bash

# Script to populate drivers 2001-2010 in the database

BASE_URL="http://localhost:8082/api/drivers"

for i in {2001..2010}; do
  curl -X POST "$BASE_URL" \
    -H "Content-Type: application/json" \
    -d "{
      \"id\": $i,
      \"name\": \"Driver-$i\",
      \"phoneNumber\": \"+1234567$((8000 + i))\",
      \"licenseNumber\": \"DL$i\",
      \"vehicleInfo\": \"Toyota Camry - ABC$i\",
      \"status\": \"AVAILABLE\",
      \"latitude\": 0.0,
      \"longitude\": 0.0
    }"
  echo ""
  echo "Created driver $i"
done

echo "All drivers created successfully!"
