# coffee-booking-app (module: app)

Interface of an Android app used to book coffee. This README describes the app features, how the module is organised, and contains placeholders for demo and screenshots (you can replace the placeholders with your images later).

This module was prepared for a 1-week midterm and the UI closely follows a provided Figma design.

## Demo

Replace the URL and thumbnail below with your demo video when ready.

[![Demo video](https://img.youtube.com/vi/VIDEO_ID/0.jpg)](https://www.youtube.com/watch?v=VIDEO_ID)

## Quick description

- Platform: Android
- UI toolkit: Jetpack Compose
- Build system: Gradle (Kotlin DSL)

## Features

- Browse products (coffee, drinks, add-ons)
- Product details and 3D previews (AR preview assets included)
- Authentication (sign-in / sign-up screens)
- Cart management and checkout flow
- Order history and order details
- Reward points, vouchers and redeem flow
- Recommendations and discovery
- Local assets only (no backend in this module)

## Technical stuff

Made with Jetpack Compose and follows recommended architecture: UI layer -> ViewModels -> single source of truth (data repository).

- ViewModels mediate between UI and repository.
- Repository is the SSOT (single source of truth) and handles business logic.
- Uses unidirectional data flow (UDF) so data flows from repository -> view models -> UI and events flow the other way.
- Navigation between screens uses route arguments when passing data.

### Secrets and local config

This repo intentionally does not include secrets. The Groq API key and other credentials are kept out of the repository.

- Local copy of the Groq API key (kept out of Git) is at: `.secrets/groq_api_key.txt` (ignored by `.gitignore`).
- Alternative: set API keys in `local.properties` or use environment variables / CI secrets.

## Project layout (module `app`)

- `src/main/java/...` — app source code (UI, viewmodels, repo, models)
- `src/main/res` — resources (drawables, layouts, values)
- `src/main/assets` — assets (3D models, small data files)
- `build.gradle.kts` — module build file

## How to run (developer)

1. Open the project in Android Studio.
2. Ensure you have the Android SDK and required Compose/Gradle toolchain installed.
3. Add local secrets before running (example):

```bash
# Option A: create .secrets and place the key there (this repo ignores .secrets/)
mkdir -p .secrets
# paste your API key into .secrets/groq_api_key.txt

# Option B: add to local.properties (ignored)
# groq.api.key=YOUR_KEY
```

4. Sync Gradle and run on an emulator or device.

## Screenshots (placeholders)

Replace the paths below with actual screenshots you will add later.

### Light

<p float="left">
  <img src="/screenshots/light/home.png" width="120" />
  <img src="/screenshots/light/details.png" width="120" /> 
  <img src="/screenshots/light/cart.png" width="120" />
  <img src="/screenshots/light/rewards.png" width="120" />
  <img src="/screenshots/light/orders.png" width="120" />
  <img src="/screenshots/light/profile.png" width="120" />
</p>

### Dark

<p float="left">
  <img src="/screenshots/dark/home.png" width="120" />
  <img src="/screenshots/dark/details.png" width="120" /> 
  <img src="/screenshots/dark/cart.png" width="120" />
  <img src="/screenshots/dark/rewards.png" width="120" />
  <img src="/screenshots/dark/orders.png" width="120" />
  <img src="/screenshots/dark/profile.png" width="120" />
</p>

## Notes

- This `src-only` branch intentionally contains only the `src/` tree and `build.gradle.kts` for a minimal remote snapshot. The full project (other metadata, configs, or assets) may exist in other branches or locally.
- If you want `main` to be replaced with this minimal tree, confirm and I will force-push it (destructive).

---

If you want, I can also add a `/screenshots` directory with placeholder PNGs, or create a short CONTRIBUTING section describing how to add images and rotate the Groq key.
