# Test Gmail SMTP connection (will fail auth since we don't have real app password, but confirms connectivity)
try {
    $smtpHost = "smtp.gmail.com"
    $smtpPort = 587
    $username = "morepravin.work@gmail.com"
    $password = "fake-password-for-test"

    Write-Host "Testing SMTP connection to $smtpHost`:$smtpPort ..."

    $smtp = New-Object System.Net.Mail.SmtpClient($smtpHost, $smtpPort)
    $smtp.EnableSsl = $true
    $smtp.Credentials = New-Object System.Net.NetworkCredential($username, $password)
    $smtp.Timeout = 10000

    $mail = New-Object System.Net.Mail.MailMessage
    $mail.From = New-Object System.Net.Mail.MailAddress($username, "TestCaterer")
    $mail.To.Add("morepravin.work@gmail.com")
    $mail.Subject = "Test"
    $mail.Body = "Test"

    Write-Host "Sending..."
    $smtp.Send($mail)
    Write-Host "SUCCESS"
} catch {
    $msg = $_.Exception.Message
    if ($msg -match "5.7.8|Username and Password not accepted|BadCredentials|authentication") {
        Write-Host "AUTH FAILED (expected with fake password) - SMTP connection works!"
        Write-Host "You need a real Gmail App Password."
    } else {
        Write-Host "ERROR: $msg"
    }
    if ($_.Exception.InnerException) {
        Write-Host "Detail: $($_.Exception.InnerException.Message)"
    }
}
