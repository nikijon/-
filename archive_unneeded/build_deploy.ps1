Write-Output "=== ModStrany Build Deploy Script ==="
Write-Output ""
Write-Output "[1/4] Checking Git installation..."
$gitCheck = Get-Command git -ErrorAction SilentlyContinue
if ($gitCheck) {
    Write-Output "Git found at: $($gitCheck.Source)"
} else {
    Write-Output "Git not found. Installing..."
    winget install --id Git.Git -e
}

Write-Output ""
Write-Output "[2/4] Checking GitHub CLI installation..."
$ghCheck = Get-Command gh -ErrorAction SilentlyContinue
if ($ghCheck) {
    Write-Output "GitHub CLI found at: $($ghCheck.Source)"
} else {
    Write-Output "GitHub CLI not found. Installing..."
    winget install --id GitHub.cli -e
}

Write-Output ""
Write-Output "[3/4] Configuring Git repository..."
cd "D:\мод страны"
git config --global user.email "builder@modstrany.local"
git config --global user.name "ModBuilder"
git init
git checkout -b main
git add .
git commit -m "Initial commit"

Write-Output ""
Write-Output "[4/4] Pushing to GitHub..."
gh repo create --public --source=. --remote=origin --push

Write-Output ""
Write-Output "Build started on GitHub Actions!"
Write-Output "JAR will be available in Actions artifacts shortly."
