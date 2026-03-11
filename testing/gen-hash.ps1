$projectDir = "D:\Projects\AI\Caterer\SM-Caterer"
$m2 = "$env:USERPROFILE\.m2\repository"
$springSecCrypto = Get-ChildItem -Path "$m2\org\springframework\security\spring-security-crypto" -Filter "*.jar" -Recurse | Where-Object { $_.Name -notmatch "sources|javadoc" } | Select-Object -First 1
$commonsLogging = Get-ChildItem -Path "$m2\commons-logging\commons-logging" -Filter "*.jar" -Recurse | Where-Object { $_.Name -notmatch "sources|javadoc" } | Select-Object -First 1

if ($springSecCrypto -and $commonsLogging) {
    $cp = "$($springSecCrypto.FullName);$($commonsLogging.FullName)"
    Write-Host "Compiling..."
    & javac -cp $cp "$projectDir\testing\GenBcrypt.java" -d "$projectDir\testing"
    Write-Host "Running..."
    & java -cp "$projectDir\testing;$cp" GenBcrypt
} else {
    Write-Host "Spring Security jar not found. Using known hash."
    # Fallback: use Python to generate bcrypt hash
    & python -c "import bcrypt; print('BCRYPT_HASH=' + bcrypt.hashpw(b'test123', bcrypt.gensalt()).decode())"
}
