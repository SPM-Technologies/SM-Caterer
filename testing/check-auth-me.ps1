# Diagnose /auth/me issue
$logFile = "D:\Projects\AI\Caterer\SM-Caterer\app-start.log"

# Search for auth/me related errors
Write-Host "=== Auth/Me Errors in Log ===" -ForegroundColor Yellow
$meErrors = Select-String -Path $logFile -Pattern "/auth/me|getCurrentUser|userMapper" | ForEach-Object { $_.Line.Trim() }
foreach ($line in $meErrors) {
    Write-Host $line
}

# Search for transaction/commit errors
Write-Host "`n=== Transaction Errors ===" -ForegroundColor Yellow
$txErrors = Select-String -Path $logFile -Pattern "TransactionSystemException|Could not commit|ConstraintViolation|PropertyValueException" | ForEach-Object { $_.Line.Trim() }
foreach ($line in $txErrors) {
    Write-Host $line
}

# Search for NullPointer or mapping issues
Write-Host "`n=== Null/Mapping Errors ===" -ForegroundColor Yellow
$npErrors = Select-String -Path $logFile -Pattern "NullPointer|toDto|mapstruct|mapping" -CaseSensitive:$false | ForEach-Object { $_.Line.Trim() }
foreach ($line in $npErrors) {
    Write-Host $line
}

# Get last 30 lines of log
Write-Host "`n=== Last 30 Lines of App Log ===" -ForegroundColor Yellow
Get-Content $logFile -Tail 30 | ForEach-Object { Write-Host $_ }
