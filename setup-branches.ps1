# ============================================
# Branch Setup Script
# Creates dev branch and configures workflow
# ============================================

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Branch Strategy Setup" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Check current branch
$currentBranch = git branch --show-current
Write-Host "Current branch: $currentBranch" -ForegroundColor Yellow
Write-Host ""

# Check if dev branch exists
$devExists = git branch --list dev
if ($devExists) {
    Write-Host "✅ 'dev' branch already exists" -ForegroundColor Green
} else {
    Write-Host "Creating 'dev' branch..." -ForegroundColor Yellow
    
    # Ensure we're on master
    if ($currentBranch -ne "master") {
        Write-Host "Switching to master branch first..." -ForegroundColor Yellow
        git checkout master
    }
    
    # Create and push dev branch
    git checkout -b dev
    git push -u origin dev
    
    Write-Host "✅ 'dev' branch created and pushed to GitHub" -ForegroundColor Green
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Branch Configuration" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Branches:" -ForegroundColor Yellow
Write-Host "  • master (production) - Full CI/CD pipeline" -ForegroundColor White
Write-Host "  • dev (development)   - Quick tests only" -ForegroundColor White
Write-Host ""

Write-Host "Workflow:" -ForegroundColor Yellow
Write-Host "  1. Push to 'dev' → test.yml runs" -ForegroundColor White
Write-Host "  2. Create PR (dev → master)" -ForegroundColor White
Write-Host "  3. Merge PR → ci-cd.yml runs" -ForegroundColor White
Write-Host ""

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Recommended Next Steps" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Protect master branch on GitHub:" -ForegroundColor Yellow
Write-Host "   Settings → Branches → Add rule" -ForegroundColor Gray
Write-Host "   - Branch name: master" -ForegroundColor Gray
Write-Host "   - ✅ Require pull request before merging" -ForegroundColor Gray
Write-Host "   - ✅ Require status checks to pass" -ForegroundColor Gray
Write-Host ""

Write-Host "2. Start working on dev branch:" -ForegroundColor Yellow
Write-Host "   git checkout dev" -ForegroundColor Gray
Write-Host "   # Make changes..." -ForegroundColor Gray
Write-Host "   git add ." -ForegroundColor Gray
Write-Host "   git commit -m 'feat: your feature'" -ForegroundColor Gray
Write-Host "   git push origin dev" -ForegroundColor Gray
Write-Host ""

Write-Host "3. When ready to release:" -ForegroundColor Yellow
Write-Host "   Create PR: dev → master on GitHub" -ForegroundColor Gray
Write-Host "   Review and merge" -ForegroundColor Gray
Write-Host ""

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "✅ Setup Complete!" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Cyan

# Show current status
Write-Host ""
Write-Host "Current Git Status:" -ForegroundColor Yellow
git branch -vv
Write-Host ""

$continue = Read-Host "Do you want to checkout dev branch now? (y/n)"
if ($continue -eq "y" -or $continue -eq "Y") {
    git checkout dev
    Write-Host "✅ Switched to dev branch" -ForegroundColor Green
}

Write-Host ""
Write-Host "📚 See BRANCH_STRATEGY.md for detailed workflow guide" -ForegroundColor Cyan
