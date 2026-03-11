$vars = @('DB_URL','DB_USERNAME','DB_PASSWORD','JWT_SECRET','SMTP_HOST','SMTP_PORT','SMTP_USERNAME','SMTP_PASSWORD','SWAGGER_ENABLED','SESSION_SECURE','REMEMBER_ME_KEY','THYMELEAF_CACHE','UPLOAD_PATH','SECURITY_LOG_LEVEL','API_LOG_LEVEL','SERVICE_LOG_LEVEL','JWT_EXPIRATION','JWT_REFRESH_EXPIRATION')

Write-Host "=== Process Environment Variables ===" -ForegroundColor Cyan
foreach ($v in $vars) {
    $val = [System.Environment]::GetEnvironmentVariable($v, 'Process')
    if ($null -eq $val) {
        Write-Host "  $v = [NOT SET]" -ForegroundColor Gray
    } elseif ($val -eq '') {
        Write-Host "  $v = [EMPTY STRING]" -ForegroundColor Red
    } else {
        # Mask passwords
        if ($v -match 'PASSWORD|SECRET') {
            Write-Host "  $v = ****" -ForegroundColor Green
        } else {
            Write-Host "  $v = $val" -ForegroundColor Green
        }
    }
}
