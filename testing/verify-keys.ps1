$basePath = "D:\Projects\AI\Caterer\SM-Caterer\src\main\resources\messages"

function Get-Keys($file) {
    Get-Content $file | Where-Object { $_ -match '^([a-zA-Z][a-zA-Z0-9._-]*)=' } | ForEach-Object { ($_ -split '=',2)[0] } | Sort-Object -Unique
}

$enKeys = Get-Keys "$basePath\messages.properties"
$mrKeys = Get-Keys "$basePath\messages_mr.properties"
$hiKeys = Get-Keys "$basePath\messages_hi.properties"

Write-Host "Unique keys - EN: $($enKeys.Count), MR: $($mrKeys.Count), HI: $($hiKeys.Count)" -ForegroundColor Cyan

# Keys in Hindi but not English
$hiOnly = $hiKeys | Where-Object { $_ -notin $enKeys }
if ($hiOnly.Count -gt 0) {
    Write-Host "`nKeys in Hindi but NOT in English ($($hiOnly.Count)):" -ForegroundColor Yellow
    $hiOnly | ForEach-Object { Write-Host "  $_" }
} else {
    Write-Host "`nNo keys missing from English" -ForegroundColor Green
}

# Keys in English but not Hindi
$enOnly = $enKeys | Where-Object { $_ -notin $hiKeys }
if ($enOnly.Count -gt 0) {
    Write-Host "`nKeys in English but NOT in Hindi ($($enOnly.Count)):" -ForegroundColor Yellow
    $enOnly | ForEach-Object { Write-Host "  $_" }
} else {
    Write-Host "No keys missing from Hindi" -ForegroundColor Green
}

# Keys in English but not Marathi
$enNotMr = $enKeys | Where-Object { $_ -notin $mrKeys }
if ($enNotMr.Count -gt 0) {
    Write-Host "`nKeys in English but NOT in Marathi ($($enNotMr.Count)):" -ForegroundColor Yellow
    $enNotMr | ForEach-Object { Write-Host "  $_" }
} else {
    Write-Host "No keys missing from Marathi" -ForegroundColor Green
}
