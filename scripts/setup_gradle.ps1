$distUrl='https://services.gradle.org/distributions/gradle-8.4.1-bin.zip'
$zip='gradle.zip'
Write-Output "Downloading $distUrl..."
Invoke-WebRequest $distUrl -OutFile $zip
Write-Output "Extracting..."
Expand-Archive $zip -DestinationPath ".\.gradle\gradle" -Force
$dir = Get-ChildItem ".\.gradle\gradle" | Where-Object { $_.PSIsContainer -and $_.Name -like 'gradle-*' } | Select-Object -First 1
if ($dir -ne $null) {
  Write-Output "Moving contents from $($dir.FullName)"
  Get-ChildItem $dir.FullName | Move-Item -Destination ".\.gradle\gradle" -Force
  Remove-Item -Recurse -Force $dir.FullName
}
Remove-Item $zip -Force
Write-Output "Gradle extracted"
Write-Output "Running ./gradlew build..."
& .\gradlew build
