# PowerShell script to populate drivers 2001-2010 in the database

$baseUrl = "http://localhost:8082/api/drivers"

for ($i = 2001; $i -le 2010; $i++) {
    $body = @{
        id = $i
        name = "Driver-$i"
        phoneNumber = "+1234567$($i + 8000 - 2001)"
        licenseNumber = "DL$i"
        vehicleInfo = "Toyota Camry - ABC$i"
        status = "AVAILABLE"
        latitude = 0.0
        longitude = 0.0
    } | ConvertTo-Json

    try {
        $response = Invoke-RestMethod -Uri $baseUrl -Method Post -Body $body -ContentType "application/json"
        Write-Host "✓ Created driver $i" -ForegroundColor Green
    } catch {
        Write-Host "✗ Failed to create driver $i : $_" -ForegroundColor Red
    }
}

Write-Host "`n✓ All drivers created successfully!" -ForegroundColor Green
