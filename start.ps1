$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$sources = Get-ChildItem -Path src_2 -Filter *.java -Recurse | ForEach-Object { $_.FullName }
& javac -d out $sources
if ($LASTEXITCODE -ne 0) { throw 'Java compilation failed.' }
Write-Host 'Open http://localhost:8080 to use Exchange Lab. Press Ctrl+C to stop.'
& java -cp out Main
