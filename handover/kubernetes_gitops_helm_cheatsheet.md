# Kubernetes / GitOps / Helm — Crash Course

## Core objects
- **Cluster**: control plane (API server, scheduler, controller-manager, etcd) + worker nodes.
- **Pod**: smallest deployable unit; wraps one or more containers sharing network/storage.
- **Deployment**: declares desired state (image, replica count, resource limits) for a set of pods; manages rolling updates via ReplicaSets.
- **Service**: stable virtual IP/DNS name that load-balances traffic across matching pods (ClusterIP, NodePort, LoadBalancer).
- **Ingress**: HTTP(S) routing in front of Services (host/path routing, TLS). Needs an Ingress Controller running in the cluster (nginx, Traefik).
- **ConfigMap / Secret**: inject config/env vars/secrets into pods without baking them into the image.
- **Namespace**: logical partition of a cluster (e.g. per env or per team).

## How Kubernetes decides what runs
- Everything is **declarative**: you describe desired state in YAML.
- The control plane runs a continuous reconciliation loop: actual state → desired state.
- Change the image tag in a Deployment → K8s does a **rolling update**: spins up new pods, waits for the **readiness probe** to pass, then terminates old ones (rate controlled by `maxSurge`/`maxUnavailable`).
- **Liveness probe**: restarts an unhealthy container. **Readiness probe**: gates whether a pod receives traffic.

## Helm
- Package manager for Kubernetes — think "npm for K8s manifests," or since you know Terraform: a **Terraform module, but for K8s YAML**.
- A "chart" = templated manifests + a `values.yaml` for parameters.
- `helm upgrade --set image.tag=1.2.3` renders the templates with your values and applies them.
- Typical real-world setup: one chart per service, separate values files per environment (`values-dev.yaml`, `values-prod.yaml`).

## GitOps
- The git repo is the **single source of truth** for what should be running (manifests or Helm values) — same philosophy as your Terraform-drift-prevention approach, just applied one layer up, to app deployments instead of infra.
- A controller inside the cluster — **ArgoCD** or **Flux** are the two big ones — continuously watches that repo.
- When it sees a diff (e.g. CI committed a new image tag), it applies the change automatically. Nobody runs `kubectl apply` by hand.
- Rollback = `git revert`. Audit trail = `git log`. Manual cluster changes get flagged/reverted (drift detection, built in).

## The day-to-day flow, end to end
1. Dev merges a PR to main.
2. CI builds a Docker image, tags it (commit SHA or semver), pushes to a registry (ECR/GHCR/Docker Hub).
3. CI commits the new image tag into a separate "deploy" repo or a `values.yaml`.
4. ArgoCD/Flux detects the commit, diffs it against the live cluster, applies it (runs `helm upgrade` under the hood if Helm-based).
5. Kubernetes performs the rolling update described above.
6. Something breaks → revert the commit in the deploy repo → controller rolls the cluster back automatically.

## Where your existing experience already maps
- **Terraform + drift-prevention discipline** → identical mental model to GitOps, just for infra instead of app deploys.
- **ECS Fargate / ALB / RDS work** → container orchestration + load balancing + rolling deploys, conceptually the same as Deployments + Services.
- **CI/CD pipeline experience** → steps 1–2 above are platform-agnostic; you've done this part regardless of target.

## Local dev clusters vs. a real bare-metal QA server
- **minikube / kind**: single-node K8s cluster running in a VM or container on your own machine. Learning/dev only — no HA, not something a team's shared QA environment would run on.
- **k3s**: lightweight, certified Kubernetes distribution (single binary, small footprint), purpose-built for single-node/bare-metal/edge use — the realistic real-world fit for a setup like a single Hetzner box.
- **kubeadm**: the "full" official way to bootstrap a real multi-node cluster on bare metal or VMs, if you ever needed more than one box.
- Rule of thumb: minikube/kind to learn the mechanics locally; k3s (or kubeadm for multi-node) is what an actual bare-metal QA server would run.

## Testing a feature branch before it hits main (ArgoCD)
Standard, well-solved problem — two approaches:
1. **Separate `Application` per branch**: an ArgoCD `Application` resource points to a repo *and* a `targetRevision` — this can be a branch, tag, or commit SHA, not just `main`. A feature branch gets its own `Application`, deployed to its own namespace, fully isolated from the one tracking `main`.
2. **`ApplicationSet` with a PR generator**: watches the git provider (GitHub/GitLab) for open pull requests and automatically spins up a temporary `Application` + isolated environment per PR. Torn down automatically when the PR merges or closes. This is the purpose-built solution for "test in isolation before touching anything stable."

Either way: the feature branch gets its own throwaway environment; `main`/QA stays clean until the PR is actually merged.
