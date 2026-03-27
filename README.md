# Progress Pet MVP skeleton

A bare-bones Android architecture skeleton for a habit-delay game loop.

## Core behavior in this MVP

- Tap **"I indulged"** to log a lapse event.
- Tier unlocks are based on pure **delay duration** between lapses, and unlocked tiers stay unlocked forever.
- Before awarding new tier unlocks on a lapse, the user can confirm whether the last recorded use time is accurate.
- Points are separate from unlocks:
  - points are earned from delay intervals with an accelerating gain rate as delay grows
  - points are banked and spendable on prizes
  - spending uses negative point transactions
- Manual log entry supports a past timestamp and a point adjustment (positive or negative).

## Kotlin package outline

- `data/model`: logs, reward tiers, prizes, lapse preview, and point transactions.
- `domain`: point pacing and tier unlock logic.
- `data/repo`: repository interface and in-memory MVP implementation.
- `ui`: screen state, view model, and Compose screen skeleton.

## UML

- Updated class diagram: `docs/updated-uml-class-diagram.md`
