for ($i = 0; $i -lt 12; $i++) {
    Start-Sleep 5
    try {
        $r = Invoke-WebRequest -Uri 'http://localhost:8080/login' -UseBasicParsing -TimeoutSec 5
        if ($r.StatusCode -eq 200) {
            Write-Host "App ready after $($($i+1)*5)s"
            exit 0
        }
    } catch {}
    Write-Host "Waiting... $($($i+1)*5)s"
}
Write-Host "Timeout"
exit 1
