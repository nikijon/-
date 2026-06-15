$d = Get-ChildItem .\.gradle\gradle | Where-Object { $_.PSIsContainer } | Select-Object -First 1
if (-not $d) { Write-Error 'No gradle dir found'; exit 2 }
Write-Output "Using: $($d.FullName)"
$gradle = Join-Path $d.FullName 'bin\gradle.bat'
if (-not (Test-Path $gradle)) { Write-Error "gradle.bat not found at $gradle"; exit 3 }
& $gradle 'build' '--no-daemon' '-x' 'test'
