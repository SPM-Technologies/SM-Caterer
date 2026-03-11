$patterns = @('DB_', 'JWT', 'SMTP', 'SWAGGER', 'SESSION', 'REMEMBER', 'THYMELEAF', 'UPLOAD', 'SECURITY_LOG', 'API_LOG', 'SERVICE_LOG')

Write-Host "`n=== User Environment Variables ===" -ForegroundColor Cyan
$userVars = [System.Environment]::GetEnvironmentVariables('User')
foreach ($key in ($userVars.Keys | Sort-Object)) {
    foreach ($p in $patterns) {
        if ($key -like "*$p*") {
            $val = $userVars[$key]
            if ([string]::IsNullOrEmpty($val)) {
                Write-Host "  $key = [EMPTY]" -ForegroundColor Red
            } else {
                Write-Host "  $key = $val" -ForegroundColor Green
            }
            break
        }
    }
}

Write-Host "`n=== System Environment Variables ===" -ForegroundColor Cyan
$sysVars = [System.Environment]::GetEnvironmentVariables('Machine')
foreach ($key in ($sysVars.Keys | Sort-Object)) {
    foreach ($p in $patterns) {
        if ($key -like "*$p*") {
            $val = $sysVars[$key]
            if ([string]::IsNullOrEmpty($val)) {
                Write-Host "  $key = [EMPTY]" -ForegroundColor Red
            } else {
                Write-Host "  $key = $val" -ForegroundColor Green
            }
            break
        }
    }
}
