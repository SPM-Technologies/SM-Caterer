Get-ChildItem 'D:\Projects\AI\Caterer\SM-Caterer\src\main\resources\db\migration' | Sort-Object Name | ForEach-Object { Write-Host $_.Name }
