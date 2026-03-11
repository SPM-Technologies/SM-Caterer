$ssDir = "D:\Projects\AI\Caterer\SM-Caterer\testing\reports\screenshots\rbac"
$total = 0
Get-ChildItem $ssDir -Directory | ForEach-Object {
    $count = (Get-ChildItem $_.FullName -Filter "*.png").Count
    $total += $count
    Write-Host "  $($_.Name): $count screenshots"
}
Write-Host "Total RBAC screenshots: $total"
