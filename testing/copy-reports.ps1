$reportsDir = "D:\Projects\AI\Caterer\SM-Caterer\testing\reports"

# Copy latest API test report
$latestApi = Get-ChildItem "$reportsDir\test-report-*.html" | Sort-Object LastWriteTime -Descending | Select-Object -First 1
if ($latestApi) {
    Copy-Item $latestApi.FullName "$reportsDir\test-report-latest.html" -Force
    Write-Host "API report: $($latestApi.Name)"
}

# Copy latest frontend report
$latestFe = Get-ChildItem "$reportsDir\frontend-report-*.html" | Sort-Object LastWriteTime -Descending | Select-Object -First 1
if ($latestFe) {
    Copy-Item $latestFe.FullName "$reportsDir\frontend-report-latest.html" -Force
    Write-Host "Frontend report: $($latestFe.Name)"
}

# Count screenshots
$ssCount = (Get-ChildItem "$reportsDir\screenshots" -Recurse -Filter "*.png").Count
Write-Host "Screenshots captured: $ssCount"

# List screenshot folders
Get-ChildItem "$reportsDir\screenshots" -Directory | ForEach-Object {
    $count = (Get-ChildItem $_.FullName -Filter "*.png").Count
    Write-Host "  $($_.Name): $count screenshots"
}
