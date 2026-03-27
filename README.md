# Progress Pet MVP skeleton

A bare-bones Android architecture skeleton for a habit-delay game loop.

## Core behavior in this MVP

- Tap **"I indulged"** to log a lapse event.
- The app computes points earned from the delay since the previous lapse.
- **Progress-first rule:**
  - Current streak points reset to 0 on each lapse.
  - Lifetime points continue to accumulate.
  - Previously unlocked reward tiers remain unlocked forever.
- User can optionally add a short feeling note when they log a lapse.

## Kotlin package outline

- `data/model`: use logs, reward tiers, pet state.
- `domain`: reward/points calculation logic.
- `data/repo`: repository interface and in-memory MVP implementation.
- `ui`: screen state, view model, and Compose screen skeleton.

## UML

- Updated class diagram: `docs/updated-uml-class-diagram.md`
