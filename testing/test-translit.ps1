# Test transliteration proxy
$tests = @(
    @{ text = "pravin"; lang = "mr"; expected = "प्रवीण" },
    @{ text = "namaskar"; lang = "mr"; expected = "नमस्कार" },
    @{ text = "dhanyavaad"; lang = "hi"; expected = "धन्यवाद" }
)

foreach ($t in $tests) {
    try {
        $url = "http://localhost:8080/api/v1/transliterate?text=$($t.text)&lang=$($t.lang)"
        $r = Invoke-RestMethod -Uri $url -TimeoutSec 10
        if ($r[0] -eq "SUCCESS") {
            $suggestions = $r[1][0][1] -join ", "
            Write-Host "  $($t.text) -> $suggestions" -ForegroundColor Green
        } else {
            Write-Host "  $($t.text) -> API returned: $($r[0])" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "  $($t.text) -> ERROR: $($_.Exception.Message.Substring(0,80))" -ForegroundColor Red
    }
}
