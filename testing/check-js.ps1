try {
    $r = Invoke-WebRequest -Uri 'http://localhost:8080/js/transliteration.js' -UseBasicParsing -TimeoutSec 5
    Write-Host "Status: $($r.StatusCode)"
    Write-Host "Contains auto-init: $($r.Content.Contains('Auto-converts'))"
    Write-Host "Contains EXCLUDED_TYPES: $($r.Content.Contains('EXCLUDED_TYPES'))"
    Write-Host "Size: $($r.Content.Length) bytes"
} catch {
    Write-Host "Error: $($_.Exception.Message)"
}
