[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$url = "http://localhost:8080/api/v1/transliterate?text=pravin&lang=mr"
$r = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 10
$body = [System.Text.Encoding]::UTF8.GetString($r.Content)
Write-Host "pravin (mr): $body"

$url2 = "http://localhost:8080/api/v1/transliterate?text=namaskar&lang=mr"
$r2 = Invoke-WebRequest -Uri $url2 -UseBasicParsing -TimeoutSec 10
$body2 = [System.Text.Encoding]::UTF8.GetString($r2.Content)
Write-Host "namaskar (mr): $body2"

$url3 = "http://localhost:8080/api/v1/transliterate?text=pravin&lang=hi"
$r3 = Invoke-WebRequest -Uri $url3 -UseBasicParsing -TimeoutSec 10
$body3 = [System.Text.Encoding]::UTF8.GetString($r3.Content)
Write-Host "pravin (hi): $body3"

# Check response contains SUCCESS
if ($body.Contains("SUCCESS")) {
    Write-Host "`nTransliteration API working correctly!" -ForegroundColor Green
} else {
    Write-Host "`nAPI returned unexpected response" -ForegroundColor Red
}
