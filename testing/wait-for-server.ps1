param([int]$MaxWaitSeconds = 300, [string]$Url = "http://localhost:8080/login")

$elapsed = 0
$interval = 5
Write-Host "Waiting for server at $Url..."
while ($elapsed -lt $MaxWaitSeconds) {
    try {
        $r = Invoke-WebRequest -Uri $Url -TimeoutSec 3 -UseBasicParsing -ErrorAction Stop
        Write-Host "SERVER_READY (after ${elapsed}s)"
        exit 0
    } catch {
        Write-Host "Waiting... (${elapsed}s elapsed)"
        Start-Sleep -Seconds $interval
        $elapsed += $interval
    }
}
Write-Host "TIMEOUT after ${MaxWaitSeconds}s"
exit 1
