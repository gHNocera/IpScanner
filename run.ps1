Write-Host "Starting IP Scanner..." -ForegroundColor Green
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location -Path $scriptPath
java -jar target/iptracker-1.0-SNAPSHOT-jar-with-dependencies.jar