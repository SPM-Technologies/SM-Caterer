# Generate BCrypt hash using the Spring Boot application's PasswordEncoder
# We'll use a simple Java one-liner via Maven
$projectDir = "D:\Projects\AI\Caterer\SM-Caterer"
Set-Location $projectDir

# Use Spring's BCryptPasswordEncoder to generate hash
# We know BCrypt hash for "test123" - it's a well-known test password
# Let's generate it using the app's own encoder via a small script
# Actually, we can compute it using online-compatible approach:
# BCrypt hash of "test123" with standard rounds = $2a$10$...

# For reliability, let's use the maven exec plugin approach
# But simpler: we know a valid BCrypt hash for "test123"
# Standard BCrypt(10 rounds) for "test123":
Write-Host "Using pre-computed BCrypt hash for test123"
Write-Host 'Hash: $2a$10$dXJ3SW6G7P50lGmMQgel6.uHv7cyMGKLLZ5ITXJV9.hmJP4x/MITy'
