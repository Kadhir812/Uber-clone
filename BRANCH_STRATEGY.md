# Branch Strategy & Workflow

## 🌿 Branch Structure

This project uses a **two-branch strategy** for development and deployment:

```
dev (development) ──PR──> master (production)
 ↓                          ↓
test.yml                  ci-cd.yml
(Quick Tests)            (Full Pipeline)
```

---

## 📋 Branch Details

### **`dev` Branch** (Development)
- **Purpose:** Active development and feature integration
- **Triggers:** `test.yml` workflow
- **Actions:** Quick tests, build validation
- **Protection:** Optional, recommended for team projects

### **`master` Branch** (Production)
- **Purpose:** Stable, production-ready code
- **Triggers:** `ci-cd.yml` workflow (full pipeline)
- **Actions:** Build, test, create Docker images, deploy
- **Protection:** ✅ Required (see setup below)

---

## 🔄 Development Workflow

### **Step 1: Work on `dev` Branch**
```bash
# Create/switch to dev branch
git checkout -b dev

# Make your changes
git add .
git commit -m "feat: Add new feature"

# Push to dev branch
git push origin dev
```

**What happens:**
```
Push to dev → Triggers test.yml
              ↓
              ✅ Backend tests
              ✅ Frontend tests
              ✅ Config validation
              (~5-10 minutes)
```

---

### **Step 2: Create Pull Request (dev → master)**
```bash
# From GitHub UI or CLI
gh pr create --base master --head dev --title "Release v1.0.0"
```

**What happens:**
```
PR Created → Triggers test.yml again
             ↓
             ✅ All tests must pass
             ✅ Review required (if configured)
             ✅ Ready to merge
```

---

### **Step 3: Merge PR to `master`**
```bash
# After approval, merge via GitHub UI
# Or from CLI:
gh pr merge --merge
```

**What happens:**
```
Merge to master → Triggers ci-cd.yml
                  ↓
                  ✅ Build all services
                  ✅ Run tests
                  ✅ Build Docker images
                  ✅ Deploy to Docker containers
                  ✅ Health checks
                  (~20-25 minutes)
```

---

## 🎯 Workflow Summary

| Action | Branch | Workflow | Duration | What Runs |
|--------|--------|----------|----------|-----------|
| **Push code** | `dev` | test.yml | 5-10 min | Quick tests |
| **Create PR** | dev → master | test.yml | 5-10 min | Quick tests |
| **Merge PR** | `master` | ci-cd.yml | 20-25 min | Full pipeline |

---

## 🛡️ Branch Protection Setup (Recommended)

### **Protect `master` Branch:**

1. Go to: **Settings → Branches → Add rule**
2. Branch name pattern: `master`
3. Enable these rules:

```
✅ Require a pull request before merging
   ✅ Require approvals (1 or more)
   ✅ Dismiss stale pull request approvals
   
✅ Require status checks to pass before merging
   ✅ Require branches to be up to date
   ✅ Status checks required:
      - test-backend
      - test-frontend
      - validate-config
      
✅ Do not allow bypassing the above settings

❌ Allow force pushes (keep disabled)
❌ Allow deletions (keep disabled)
```

---

## 📝 Complete Example Workflow

### **Scenario: Adding a new feature**

```bash
# 1. Start from updated master
git checkout master
git pull origin master

# 2. Create/switch to dev branch
git checkout dev
git pull origin dev
# OR create new: git checkout -b dev

# 3. Make your changes
# Edit files...
git add .
git commit -m "feat: Add ride cancellation feature"

# 4. Push to dev (triggers test.yml)
git push origin dev

# 5. Wait for tests to pass ✅
# Check: https://github.com/Kadhir812/Uber-clone/actions

# 6. Create PR from dev to master
gh pr create \
  --base master \
  --head dev \
  --title "feat: Add ride cancellation" \
  --body "Adds ability for riders to cancel rides before driver acceptance"

# 7. Wait for PR tests to pass ✅

# 8. Review and approve PR (via GitHub UI)

# 9. Merge PR to master (triggers ci-cd.yml)
gh pr merge --merge

# 10. Full CI/CD pipeline runs ✅
# - Builds all services
# - Creates Docker images
# - Deploys to containers
# - Runs health checks

# 11. Done! Feature is deployed 🎉
```

---

## 🚀 Quick Commands

### **For Daily Development:**
```bash
# Work on dev branch
git checkout dev
git pull origin dev

# Make changes
# ... edit files ...

# Commit and push (triggers tests)
git add .
git commit -m "feat: your feature"
git push origin dev
```

### **For Release:**
```bash
# Create PR from dev to master
gh pr create --base master --head dev --title "Release v1.x.x"

# Or via GitHub UI:
# https://github.com/Kadhir812/Uber-clone/compare/master...dev
```

---

## 🔍 Monitoring Workflows

### **Check Workflow Status:**
```bash
# View recent workflow runs
gh run list --branch dev      # For dev branch
gh run list --branch master   # For master branch

# Watch a specific run
gh run watch

# View logs if failed
gh run view --log-failed
```

### **GitHub UI:**
1. Go to: **Actions** tab
2. See all workflows
3. Click on any run for detailed logs

---

## 🎨 Workflow Badges

Add to your README.md:

```markdown
## CI/CD Status

[![Tests](https://github.com/Kadhir812/Uber-clone/actions/workflows/test.yml/badge.svg?branch=dev)](https://github.com/Kadhir812/Uber-clone/actions/workflows/test.yml)
[![CI/CD](https://github.com/Kadhir812/Uber-clone/actions/workflows/ci-cd.yml/badge.svg?branch=master)](https://github.com/Kadhir812/Uber-clone/actions/workflows/ci-cd.yml)
```

**Shows:**
- 🟢 Green badge = Tests passing
- 🔴 Red badge = Tests failing
- 🟡 Yellow badge = Tests running

---

## 💡 Best Practices

### **DO:**
✅ Always push to `dev` first
✅ Wait for tests to pass before creating PR
✅ Review PR changes before merging
✅ Use descriptive commit messages
✅ Keep `master` stable and deployable

### **DON'T:**
❌ Don't push directly to `master` (set up protection)
❌ Don't merge failing tests
❌ Don't skip PR reviews
❌ Don't force push to `master`

---

## 🐛 Troubleshooting

### **Tests failing on `dev`?**
```bash
# Run tests locally first
cd Backend/ride-service && mvn test
cd Frontend && npm test

# Fix issues, then push again
git add .
git commit -m "fix: resolve test failures"
git push origin dev
```

### **PR blocked from merging?**
```
Check:
1. Are all status checks passing? ✅
2. Do you have required approvals? ✅
3. Is branch up to date? ✅
4. Any merge conflicts? ❌
```

### **CI/CD failing on `master`?**
```bash
# Check logs in GitHub Actions
# Common issues:
- Docker build failures → Check Dockerfiles
- Service health checks timeout → Increase wait time
- Database connection issues → Check docker-compose.yml
```

---

## 📊 Workflow Visualization

```
Developer Workflow:
==================

Local → dev branch → test.yml (Quick Tests)
         ↓
    Tests Pass ✅
         ↓
    Create PR (dev → master)
         ↓
    test.yml runs on PR
         ↓
    Tests Pass ✅
         ↓
    Code Review & Approval
         ↓
    Merge to master
         ↓
    ci-cd.yml (Full Pipeline)
         ↓
    Build → Test → Docker → Deploy
         ↓
    Production Ready ✅
         ↓
    Users get new features 🎉
```

---

## 🎯 Summary

| You Want To... | Do This... | What Runs |
|----------------|------------|-----------|
| **Test changes** | Push to `dev` | test.yml |
| **Release to production** | PR dev → master, then merge | ci-cd.yml |
| **Hotfix** | Create fix on `dev`, PR to `master` | test.yml → ci-cd.yml |
| **Check status** | GitHub Actions tab | View all runs |

---

**Quick Start:**
```bash
# Setup branches
git checkout -b dev
git push -u origin dev

# Start developing
git checkout dev
# ... make changes ...
git push origin dev

# Release
# Create PR via GitHub UI: dev → master
# Merge after approval
```

Your two-branch workflow is ready! 🚀
