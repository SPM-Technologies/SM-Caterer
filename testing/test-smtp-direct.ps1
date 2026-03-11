# Direct SMTP test using .NET SmtpClient
try {
    $smtpHost = "live.smtp.mailtrap.io"
    $smtpPort = 587
    $username = "api"
    $password = "e4d12c08e6b115c0ad42575aabd39a43"
    $fromEmail = "noreply@spmtech.com"
    $fromName = "TestCaterer"
    $toEmail = "morepravin.work@gmail.com"

    Write-Host "Testing SMTP connection to $smtpHost`:$smtpPort ..."

    $smtp = New-Object System.Net.Mail.SmtpClient($smtpHost, $smtpPort)
    $smtp.EnableSsl = $true
    $smtp.Credentials = New-Object System.Net.NetworkCredential($username, $password)
    $smtp.Timeout = 15000

    $mail = New-Object System.Net.Mail.MailMessage
    $mail.From = New-Object System.Net.Mail.MailAddress($fromEmail, $fromName)
    $mail.To.Add($toEmail)
    $mail.Subject = "Test Email from SM-Caterer"
    $mail.IsBodyHtml = $true
    $mail.Body = @"
<html><body>
<h2>Test Email</h2>
<p>This is a test email from SM-Caterer application.</p>
<p>If you received this, your SMTP configuration is working correctly!</p>
<p>SMTP Host: $smtpHost<br>Port: $smtpPort<br>From: $fromEmail</p>
<hr>
<p style="color:gray;font-size:12px">Sent at: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')</p>
</body></html>
"@

    Write-Host "Sending email to $toEmail ..."
    $smtp.Send($mail)
    Write-Host "SUCCESS: Email sent!" -ForegroundColor Green

    $mail.Dispose()
    $smtp.Dispose()
}
catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.InnerException) {
        Write-Host "Inner: $($_.Exception.InnerException.Message)" -ForegroundColor Red
    }
}
