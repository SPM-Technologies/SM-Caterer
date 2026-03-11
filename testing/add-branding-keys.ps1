$basePath = "D:\Projects\AI\Caterer\SM-Caterer\src\main\resources\messages"

$enKeys = @"

# Branding Settings (additional)
settings.branding.logoUploaded=Logo uploaded
settings.branding.noLogo=No logo uploaded
settings.branding.otherSettings=Other Settings
"@

$mrKeys = @"

# Branding Settings (additional)
settings.branding.logoUploaded=\u0932\u094B\u0917\u094B \u0905\u092A\u0932\u094B\u0921 \u0915\u0947\u0932\u093E
settings.branding.noLogo=\u0932\u094B\u0917\u094B \u0905\u092A\u0932\u094B\u0921 \u0928\u093E\u0939\u0940
settings.branding.otherSettings=\u0907\u0924\u0930 \u0938\u0947\u091F\u093F\u0902\u0917\u094D\u091C
"@

$hiKeys = @"

# Branding Settings (additional)
settings.branding.logoUploaded=\u0932\u094B\u0917\u094B \u0905\u092A\u0932\u094B\u0921 \u0915\u093F\u092F\u093E \u0917\u092F\u093E
settings.branding.noLogo=\u0932\u094B\u0917\u094B \u0905\u092A\u0932\u094B\u0921 \u0928\u0939\u0940\u0902 \u0939\u0948
settings.branding.otherSettings=\u0905\u0928\u094D\u092F \u0938\u0947\u091F\u093F\u0902\u0917\u094D\u0938
"@

Add-Content -Path "$basePath\messages.properties" -Value $enKeys -Encoding UTF8
Add-Content -Path "$basePath\messages_mr.properties" -Value $mrKeys -Encoding UTF8
Add-Content -Path "$basePath\messages_hi.properties" -Value $hiKeys -Encoding UTF8

$en = (Get-Content "$basePath\messages.properties" | Where-Object { $_ -match '^[a-zA-Z]' }).Count
$mr = (Get-Content "$basePath\messages_mr.properties" | Where-Object { $_ -match '^[a-zA-Z]' }).Count
$hi = (Get-Content "$basePath\messages_hi.properties" | Where-Object { $_ -match '^[a-zA-Z]' }).Count
Write-Host "Final key counts - English: $en, Marathi: $mr, Hindi: $hi" -ForegroundColor Cyan
