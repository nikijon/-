# Script to install git, configure repo, and push to GitHub for CI build

Write-Output "=== ModStrany Build & Deploy Script ==="

# 1. Check/Install Git
Write-Output "`n[1/4] Checking Git installation..."
$gitCheck = Get-Command git -ErrorAction SilentlyContinue
if ($gitCheck) {
    Write-Output "✓ Git is already installed at: $($gitCheck.Source)"
} else {
    Write-Output "✗ Git not found. Installing Git..."
    Start-Process -FilePath "powershell" -ArgumentList "-NoProfile", "-Command", "winget install --id Git.Git -e" -WindowStyle Normal -Wait
    Write-Output "✓ Git installation launched. Please wait for it to complete, then restart PowerShell."
    Read-Host "Press Enter after installing git and restarting PowerShell"
}

# 2. Check/Install GitHub CLI
Write-Output "`n[2/4] Checking GitHub CLI installation..."
$ghCheck = Get-Command gh -ErrorAction SilentlyContinue
if ($ghCheck) {
    Write-Output "✓ GitHub CLI is already installed at: $($ghCheck.Source)"
} else {
    Write-Output "✗ GitHub CLI not found. Installing GitHub CLI..."
    Start-Process -FilePath "powershell" -ArgumentList "-NoProfile", "-Command", "winget install --id GitHub.cli -e" -WindowStyle Normal -Wait
    Write-Output "✓ GitHub CLI installation launched. Please wait for it to complete, then restart PowerShell."
    Read-Host "Press Enter after installing GitHub CLI and restarting PowerShell"
}

# 3. Verify we're in the right directory
Write-Output "`n[3/4] Configuring Git repository..."
Push-Location
cd "D:\мод страны"
Write-Output "Current directory: $(Get-Location)"

# Configure git
git config --global user.email "builder@modstrany.local"
git config --global user.name "ModStrany Builder"

# Initialize repo if needed
if (-not (Test-Path .git)) {
    git init
    git checkout -b main
}

# Add all files and commit
git add .
$commitOutput = git commit -m "Initial commit" 2>&1
Write-Output $commitOutput

# 4. Create remote and push
Write-Output "`n[4/4] Pushing to GitHub..."
Write-Output "Ensure you are logged in to GitHub. Run 'gh auth login' if needed."
Read-Host "Press Enter to continue with 'gh repo create' (or Ctrl+C to cancel)"

gh repo create --public --source=. --remote=origin --push

Write-Output "`n✓ Repository pushed successfully!"
Write-Output "GitHub Actions will now build your mod JAR."
Write-Output "Check https://github.com/[username]/modstrany/actions for build status."
Write-Output "Once complete, download the artifact from the Actions workflow."

Pop-Location
