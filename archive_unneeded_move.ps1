$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location -LiteralPath $scriptDir
$items = @('build_and_deploy.ps1','build_deploy.ps1','scripts','gradle','.gradle','gradlew','gradlew.bat')
New-Item -ItemType Directory -Force -Path archive_unneeded | Out-Null
foreach ($i in $items) {
    if (Test-Path $i) {
        try {
            Move-Item -LiteralPath $i -Destination archive_unneeded -Force -ErrorAction Stop
            Write-Output ("Moved: " + $i)
        } catch {
            Write-Output ("Failed to move: " + $i + " - " + $_.Exception.Message)
        }
    } else {
        Write-Output ("Not found: " + $i)
    }
}
Write-Output "--- Archive contents:"
Get-ChildItem -Force archive_unneeded | Select-Object Name,Mode,Length | Format-Table -AutoSize
