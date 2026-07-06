# Example: per-PR database on a SHARED server (provision & teardown)

Reference examples for the cost-saving pattern discussed in Phase 3: instead of bootstrapping a whole
new database server per PR, each PR gets its **own database on one shared Arango server**, created when
the preview env comes up and dropped when the PR closes.

> These are **reference examples** (they use Helm `{{ }}` placeholders and aren't wired into the
> `qa-service` chart). To make them real, drop the hook Jobs into a chart's `templates/` folder and
> provide the `database.*` values. Shown with ArangoDB's REST API, but the shape is identical for
> Postgres (`CREATE DATABASE` / `CREATE SCHEMA` + `DROP`), MySQL, Mongo, etc.

## The three pieces

| File | Runs when | Job |
|---|---|---|
| `db-create-hook.yaml` | env comes up (Helm `pre-install`/`pre-upgrade` → ArgoCD **PreSync**) | create DB `pr-<n>` (idempotent) |
| `db-drop-hook.yaml` | env is deleted (ArgoCD **PostDelete**) | drop DB `pr-<n>` |
| `db-janitor-cronjob.yaml` | nightly | drop orphaned `pr-*` DBs whose PR is closed (backstop) |

## The gotcha worth remembering

**Helm delete-hooks do NOT run under ArgoCD.** ArgoCD never calls `helm uninstall` — it just deletes
the rendered manifests — so a Helm `pre-delete` hook is silently ignored. Use ArgoCD's own
**`PostDelete`** hook for teardown. Miss this and your databases leak forever.

- **Create:** Helm `pre-install`/`pre-upgrade` (ArgoCD maps it to `PreSync`) — works fine.
- **Drop:** ArgoCD `PostDelete` (needs ArgoCD ≥ 2.10).
- **Backstop:** the janitor CronJob, because hooks can fail.

## How the name flows

The DB name `pr-<n>` comes from the **same per-PR values** that set the namespace and Ingress host, and
is consumed in three places: the **app** (`DB_NAME` env), the **create Job**, and the **drop Job**. The
Jobs use **admin creds** (from a Secret) to the shared server; the app itself should get narrower,
DB-scoped credentials.

## Design notes

- **Idempotency:** create treats `409 already exists` as success; drop treats `404 not found` as success.
  This survives re-syncs and double-fires.
- **Isolation vs. cost:** one shared server, one small DB per PR = far cheaper than a server per PR,
  while still isolating each PR's data. The trade-off: a shared server is a shared blast radius.
- **Data seeding:** the create Job is also the natural place to load fixture/reference data so the
  preview env is immediately testable.
