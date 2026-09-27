# Personal Loan Officer

GitHub-ready Android project for a professional offline personal-loan sales utility.

## Build
GitHub Actions automatically builds a debug APK on push to `main`, or manually from:
Actions → Build Android APK → Run workflow.

## Important
The supplied rate table is included exactly as the initial lookup data. The current starter build focuses on the Android shell, calculator, rate lookup, insurance/fee rules, and callback notification receiver. The full Room persistence, import/export, PDF/PNG generation, exact alarm scheduling UI, and backup/restore layers should be completed before production use.

## Validation
Reference test:
₹8,80,000, 19.25%, 48 months with no charges should be approximately ₹26,428.32.
