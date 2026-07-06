# Hands-On Plan: k3s + Helm + ArgoCD + GitHub PR Environments

**Goal:** Build a genuinely real (small) version of the workflow, locally, so it's true experience rather than theory. Target: a tiny Kotlin/Spring Boot service, deployed via GitOps, with per-PR ephemeral environments and a GitHub branch-protection check that blocks merge on failed deploy/tests.

**Read this whole file before starting.** Work through phases in order; each depends on the previous one working. Verify each phase before moving on — don't stack unverified steps.

---

## Phase 0 — Prerequisites
- [ ] Docker installed and running
- [ ] `kubectl` installed
- [ ] `helm` installed
- [ ] `k3d` installed (`brew install k3d` / see k3d.io)
- [ ] `argocd` CLI installed
- [ ] A GitHub account + a new (empty) public or private repo for this project
- [ ] JDK + existing Kotlin/Spring Boot toolchain already in place (Karsten has this)

## Phase 1 — Local k3s cluster
- [ ] Create a k3d cluster: `k3d cluster create qa-practice --port "80:80@loadbalancer" --port "443:443@loadbalancer"`
- [ ] Verify: `kubectl get nodes` shows the k3d node Ready
- [ ] Confirm Traefik (bundled ingress controller) is running: `kubectl get pods -n kube-system`

## Phase 2 — Minimal app
- [ ] Create a minimal Kotlin + Spring Boot service with one endpoint (e.g. `GET /hello` returning a version string — make the version string configurable via env var, useful later for visually confirming rolling updates)
- [ ] Add a `Dockerfile` (multi-stage build: Gradle/Maven build stage → slim JRE runtime stage)
- [ ] Build and run the image locally with plain `docker run` to confirm it works before involving Kubernetes at all

## Phase 3 — Helm chart
- [ ] `helm create qa-service` to scaffold a chart
- [ ] Trim it down to: Deployment, Service, Ingress (strip out things you don't need yet, e.g. HPA, ServiceAccount customization)
- [ ] Parameterize `image.repository`, `image.tag`, and `ingress.host` in `values.yaml`
- [ ] `helm install qa-service ./qa-service --set image.tag=<local-tag>` — confirm it deploys and `kubectl get pods` shows it Running
- [ ] Add the host to `/etc/hosts` (or use sslip.io, see Phase 6) and curl the endpoint through the Ingress

## Phase 4 — ArgoCD, tracking main
- [ ] Install ArgoCD into the cluster (`kubectl create namespace argocd` + apply ArgoCD's install manifest)
- [ ] Push the Helm chart to the GitHub repo
- [ ] Create an ArgoCD `Application` pointing at the repo, `targetRevision: main`, pointing at the chart path
- [ ] Delete the manual `helm install` from Phase 3, let ArgoCD deploy it instead — confirm it shows `Synced`/`Healthy` in `argocd app get`
- [ ] Make a small change (bump the version string), commit + push to main, watch ArgoCD pick it up and roll the pods automatically — **this is the core GitOps loop, make sure you've actually watched it happen**

## Phase 5 — GitHub Actions CI (build + push)
- [ ] Add a workflow triggered on `push` to any branch: build the Docker image, tag it with the short commit SHA, push to GitHub Container Registry (ghcr.io — simplest, no extra account needed)
- [ ] Confirm a pushed commit produces a new image in the repo's Packages tab

## Phase 6 — Ephemeral per-PR environments
- [ ] Convert the single ArgoCD `Application` into an `ApplicationSet` with a **Pull Request generator** (points at the GitHub repo, watches open PRs)
- [ ] Template the release name, namespace, and Ingress host per PR, e.g. host: `pr-{{number}}.127.0.0.1.sslip.io`
- [ ] Open a test PR from a feature branch, watch the ApplicationSet spin up a new isolated `Application` + namespace automatically
- [ ] Curl the PR's unique hostname to confirm it's live and isolated from the `main` deployment
- [ ] Close/merge the PR, confirm the ephemeral Application + namespace get cleaned up automatically

## Phase 7 — Required status check blocking merge
- [ ] Extend the CI workflow (Phase 5) to run on `pull_request`: build → push image → wait for the corresponding ArgoCD PR Application to report `Synced` + `Healthy` (`argocd app wait <app-name> --timeout 120`) → run a smoke test (simple curl + assert on response) against the PR's unique hostname
- [ ] In GitHub repo settings, add a branch protection rule on `main`: "Require status checks to pass before merging," select this workflow
- [ ] Prove it works: intentionally break the app (e.g. wrong port in the Dockerfile), open a PR, confirm the check fails and the merge button is blocked. Fix it, confirm the check goes green and merging unblocks.

## Phase 8 — Full dry run (do this last, do it twice)
- [ ] From a clean feature branch: change the version string, open a PR, watch the ephemeral environment come up, watch the check pass, merge, watch `main`'s ArgoCD Application update via the GitOps loop
- [ ] Narrate out loud what's happening at each step, in your own words, as if explaining it in the interview — this is the actual goal of the exercise

---

## Notes for whoever is executing this (Claude Code)
- Karsten already has strong Kotlin/Spring Boot/Docker/CI-CD fundamentals — don't over-explain those parts, focus guidance on the Kubernetes/Helm/ArgoCD/GitOps-specific pieces.
- Stop and verify at the end of each phase before proceeding — don't let failures compound across phases.
- If something in a phase doesn't match reality (tool version differences, etc.), fix forward and note the deviation rather than skipping ahead.
