# The Lingo — Kubernetes / GitOps / Cloud-Native Vocabulary

A living glossary of the **idioms, jargon, and shorthand** people actually say in this world.
The goal isn't just definitions (the cheatsheet has those) — it's the *flavor of speech* that
signals "I've done this for real" in an interview.

> **How to use it:** Don't force these in. Fluency reads as using the right term *naturally* at
> the right moment ("we treat pods as cattle, so...") — not reciting a thesaurus. Drop terms where
> they actually clarify your point.

---

## If you remember only ten

These carry the most weight — they show you grasp the *mental model*, not just commands:

1. **Cattle, not pets** — servers/pods are disposable and replaceable, not lovingly maintained.
2. **Declarative, not imperative** — you describe the desired *end state*, not the steps.
3. **Reconciliation loop** — a controller continuously drives actual state → desired state.
4. **Desired state vs. actual state** — and **drift** is the gap between them.
5. **Self-healing** — the system restores desired state on its own (restarts, reschedules).
6. **Single source of truth** — Git holds what *should* be running (the heart of GitOps).
7. **Pull vs. push deployment** — an in-cluster operator pulls from Git (GitOps) vs. CI pushing to the cluster.
8. **Blast radius** — how much breaks when one thing fails; you design to shrink it.
9. **Immutable infrastructure** — never patch a running box; replace it with a new image.
10. **Idempotent** — applying the same thing twice changes nothing the second time.

---

## Pronunciation (instant credibility — or instant tell)

| Written | Say it | Notes |
|---|---|---|
| **k8s** | "kates" or "k-eights" | The `8` = eight letters between k and s. |
| **kubectl** | "kube control" | Most common. "kube-cuddle" / "kube-c-t-l" are jokey/tolerated. Never spell it "koob-ectal". |
| **kube** | "koob" | As in koob-let, koob-proxy. |
| **YAML** | "yammel" | Rhymes with "camel", not "Y-A-M-L". |
| **etcd** | "et-see-dee" | The cluster's key-value store (from `/etc` + `d` for distributed). |
| **CNCF** | "C-N-C-F" | Cloud Native Computing Foundation — the body that stewards k8s, ArgoCD, Helm, etc. |
| **Helm / chart / release** | as written | See Helm section. |
| **PromQL / Prometheus** | "prom-Q-L" / "pro-mee-thee-us" | The metrics query language / system. |

---

## Culture idioms (the colorful ones)

| Term | Meaning |
|---|---|
| **Cattle, not pets** | *Pets*: servers you name, hand-tune, and nurse back to health when sick. *Cattle*: numbered, identical, and when one gets sick you shoot it and replace it. Modern infra treats pods/nodes/instances as cattle. (Coined by Bill Baker, popularized by Randy Bias.) |
| **Snowflake server** | A machine configured by hand over time, unique and unreproducible — the thing immutable infra kills. "A special snowflake." |
| **Works on my machine** | The classic excuse that containers exist to eliminate. |
| **Throw it over the wall** | The old dev→ops handoff antipattern DevOps was invented to fix. |
| **Turtles all the way down** | Containers in pods on nodes in VMs in a hypervisor... abstraction stacked on abstraction. |
| **Yak shaving** | The endless chain of unrelated prerequisite tasks you must finish before doing the actual task. |
| **Bikeshedding** | Spending disproportionate time arguing trivial details (naming, colors) while ignoring the hard stuff. |
| **Ship it** | Ready to deploy. Often ironic. |
| **Dogfooding** | Running your own product internally to feel its pain. |
| **The happy path** | The normal, everything-works execution flow (vs. the error/edge cases). |
| **Greenfield vs. brownfield** | Building fresh from scratch vs. working within an existing system's constraints. |

---

## Core mental model

| Term | Meaning |
|---|---|
| **Declarative** | You describe the *desired end state* (YAML); the system figures out how to get there. Opposite of **imperative** (you script the steps). |
| **Reconciliation loop / control loop** | A controller endlessly compares desired vs. actual state and takes action to converge them. The beating heart of both Kubernetes *and* GitOps. |
| **Desired state / actual state** | What you declared should run vs. what's really running right now. |
| **Drift** | Actual state diverging from desired state (someone `kubectl edit`ed something by hand). GitOps *detects and corrects* drift. |
| **Converge / eventual consistency** | The cluster steadily moves toward desired state; it may not be instant, but it gets there. |
| **Self-healing** | The system restores desired state automatically — restarts crashed pods, reschedules pods off a dead node. |
| **Idempotent** | Applying the same operation repeatedly yields the same result. Why you can re-`apply` a manifest safely. |
| **Immutable infrastructure** | You never modify a running server; you replace it with a freshly-built image. Ties directly to *cattle*. |
| **Control plane vs. data plane** | *Control plane* = the brains (decides/schedules — API server, scheduler, etcd). *Data plane* = the muscle (actually moves traffic / runs workloads). |

---

## Pods, nodes & workload types

| Term | Meaning |
|---|---|
| **Ephemeral** | Short-lived and disposable. Pods are ephemeral — they die and are replaced with new IPs. |
| **Sidecar** | A helper container running *alongside* your app in the same pod (log shipper, proxy). A service mesh injects a proxy sidecar into every pod. |
| **Init container** | Runs to completion *before* the app containers start (DB migrations, waiting on a dependency). |
| **DaemonSet** | Runs exactly *one pod per node* (log collectors, node metrics, CNI agents). |
| **StatefulSet** | For stateful apps needing stable identity + storage (databases). Ordered, named pods (`db-0`, `db-1`). |
| **Deployment / ReplicaSet** | Deployment manages ReplicaSets manages Pods. You edit the Deployment; it handles the rest. |
| **Job / CronJob** | Run-to-completion task / scheduled task. (The `helm-install-traefik` "Completed" pods you saw were Jobs.) |
| **Replica** | One running copy of a pod. "Scale to 3 replicas." |
| **Manifest** | A YAML file describing a resource. "Apply the manifest." |
| **Workload** | Umbrella term for anything running your code (Deployments, StatefulSets, DaemonSets, Jobs). |

---

## Pod/container failure states (interviewers *love* these)

| Status | What it means |
|---|---|
| **CrashLoopBackOff** | The container keeps crashing right after start, so the kubelet restarts it with an ever-increasing back-off delay. Usually a bad config, missing dependency, or the app exiting immediately. **The #1 status you'll be asked to debug.** |
| **OOMKilled** | The kernel killed the container for exceeding its memory *limit* (exit code 137). Fix: raise the limit or fix the leak. |
| **ImagePullBackOff / ErrImagePull** | Can't pull the image — wrong tag, private registry with no credentials, or registry unreachable. |
| **Pending** | The pod can't be scheduled onto any node — no node has enough CPU/RAM, or constraints (taints/affinity) can't be satisfied. |
| **Evicted** | The kubelet kicked the pod off a node under resource pressure (disk/memory). |
| **Terminating (stuck)** | Pod won't finish shutting down — often a finalizer or a process ignoring SIGTERM. |
| **Readiness vs. liveness** | *Readiness* gates whether a pod gets traffic; *liveness* decides whether to restart it. A pod can be Running but Not Ready. |

---

## Node operations

| Term | Meaning |
|---|---|
| **Cordon** | Mark a node unschedulable — no new pods land, existing ones stay (`kubectl cordon`). |
| **Drain** | Evict all pods off a node (for maintenance/upgrade) — cordon + relocate (`kubectl drain`). |
| **Taints and tolerations** | A *taint* on a node repels pods unless the pod has a matching *toleration*. Node-side "keep out unless you have a pass." |
| **Affinity / anti-affinity** | Rules attracting or repelling pods relative to nodes or other pods ("spread replicas across nodes"). |
| **Noisy neighbor** | One workload hogging shared CPU/IO/network and degrading others on the same node. |
| **Bin packing** | The scheduler efficiently packing pods onto nodes to use capacity well. |

---

## Networking

| Term | Meaning |
|---|---|
| **North-south traffic** | Traffic in/out of the cluster (client ↔ cluster, via Ingress/LoadBalancer). |
| **East-west traffic** | Traffic *between* services inside the cluster (pod ↔ pod). Service meshes mostly govern this. |
| **Ingress / egress** | Traffic coming *in* / going *out*. |
| **ClusterIP / NodePort / LoadBalancer** | Service types: internal-only virtual IP / a port on every node / an external load balancer. |
| **Service mesh** | A layer (Istio, Linkerd) that manages east-west traffic — mTLS, retries, routing — via sidecar proxies. |
| **kube-proxy** | The component wiring Service virtual IPs to real pod IPs on each node. |
| **CNI** | Container **Network** Interface — the pluggable component that gives each pod its IP and wires the flat pod-to-pod network (Flannel = k3s default; Calico, Cilium). Also enforces NetworkPolicy, if the plugin supports it. |
| **CRI / CSI** (CNI's siblings) | The other swappable interfaces K8s delegates to. **CRI** = Container *Runtime* Interface (containerd, CRI-O — *how containers run*). **CSI** = Container *Storage* Interface (*how PersistentVolumes are provisioned*). Same idea every time: K8s defines the contract, a plugin fulfills it. |
| **Headless service** | A Service with no ClusterIP — returns pod IPs directly (used by StatefulSets). |

---

## Scaling

| Term | Meaning |
|---|---|
| **Scale out / in (horizontal)** | Add/remove *replicas or nodes*. "Throw more pods at it." |
| **Scale up / down (vertical)** | Make the *box bigger/smaller* (more CPU/RAM per pod). |
| **HPA / VPA** | Horizontal Pod Autoscaler (more replicas on load) / Vertical Pod Autoscaler (right-sizes requests). |
| **Cluster autoscaler** | Adds/removes *nodes* when pods can't be scheduled / nodes sit idle. |
| **Requests vs. limits** | *Request* = guaranteed reserved amount (used for scheduling). *Limit* = hard ceiling (breach memory → OOMKilled). |
| **Overcommit** | Scheduling more requests than physical capacity, betting not everyone peaks at once. |

---

## Deployment strategies

| Term | Meaning |
|---|---|
| **Rolling update** | Gradually replace old pods with new ones, a few at a time (Kubernetes default). Controlled by `maxSurge`/`maxUnavailable`. |
| **Blue/green** | Stand up a full second environment (green), flip *all* traffic at once, keep blue as instant rollback. |
| **Canary** | Release to a *small % of traffic* first, watch metrics, then ramp up (or roll back). |
| **Bake time** | How long you let a canary/new version "soak" under real traffic before promoting it. |
| **Feature flag** | Toggle functionality at runtime without redeploying — decouples *deploy* from *release*. |
| **Shift left** | Move testing/security *earlier* in the pipeline (catch issues before prod). |
| **Progressive delivery** | Umbrella term for canary/blue-green/feature-flag gradual rollouts (Argo Rollouts, Flagger). |

---

## Environments & preview-env cost

| Term | Meaning |
|---|---|
| **Ephemeral / preview environment** | A throwaway environment spun up per branch/PR to test a change in isolation, torn down on merge/close. High fidelity, but cost scales with *open* PRs — so they're kept small. |
| **Shared vs. isolated dependencies** | The key cost lever: give each PR its own *stateless app* but point it at a *shared* database (a schema / logical DB per PR) instead of a whole DB per PR. |
| **Scale to zero** | Idle workloads dropped to 0 replicas and woken on first request (KEDA/Knative) — keeps unused preview envs nearly free. |
| **ResourceQuota / LimitRange** | Namespace-level caps: a *ResourceQuota* bounds total CPU/mem a namespace (PR env) may request; a *LimitRange* sets per-pod defaults/ceilings. |
| **TTL / auto-teardown** | Preview envs auto-deleted on PR close or after idle timeout, so the running count = *open* PRs, not all-time. |

---

## GitOps

| Term | Meaning |
|---|---|
| **Single source of truth** | Git holds the desired state of the whole system; nothing is "true" until it's committed. |
| **Pull vs. push deployment** | *Push*: CI runs `kubectl/helm` against the cluster (credentials leave CI). *Pull*: an in-cluster operator (ArgoCD/Flux) pulls from Git and applies — more secure, cluster reaches out, no external creds. GitOps = pull. |
| **GitOps operator / controller** | The in-cluster agent watching Git and reconciling (ArgoCD, Flux). |
| **Drift detection** | Operator notices the live cluster no longer matches Git and flags it **OutOfSync**. |
| **Self-heal** (ArgoCD) | Auto-revert any manual cluster change back to what Git says. |
| **Prune** | Delete cluster resources that were removed from Git (so Git deletions actually take effect). |
| **App-of-apps** | An ArgoCD Application whose job is to deploy *other* Applications — the bootstrap pattern. |
| **ApplicationSet** | Templating layer that generates many Applications from a generator (e.g. one per PR, per cluster). |
| **Sync wave** | Ordering hint so ArgoCD applies resources in phases (CRDs before the things that use them). |
| **Rollback = git revert** | You don't hotfix the cluster; you revert the commit and let the operator converge. |

**ArgoCD status vocabulary you'll say out loud:**
- **Synced / OutOfSync** — does the live cluster match Git?
- **Healthy / Progressing / Degraded / Missing** — are the resources actually working?
- **Auto-sync**, **self-heal**, **prune** — the three toggles that make it fully automatic.

---

## Helm

| Term | Meaning |
|---|---|
| **Chart** | A package of templated Kubernetes manifests + a `values.yaml`. "npm package for K8s." |
| **Release** | *One installed instance* of a chart in a cluster. Install the same chart twice = two releases. |
| **Values** | The parameters that fill in the templates (`values.yaml`, `--set image.tag=...`). |
| **Templating / rendering** | Turning the chart + values into final YAML (`helm template` to preview). |
| **Umbrella / parent chart** | A chart that bundles other charts as dependencies (deploy a whole stack at once). |
| **Chart repo** | A hosted index of charts you can `helm install` from. |
| **Rollback** | `helm rollback` returns a release to a previous revision. |
| **Hooks** | Chart-defined actions at lifecycle points (pre-install job, post-upgrade test). |

---

## Reliability / SRE

| Term | Meaning |
|---|---|
| **SLA / SLO / SLI** | *SLA*: the promise to customers (contractual). *SLO*: your internal target (e.g. 99.9%). *SLI*: the actual measured number. |
| **Error budget** | The allowed unreliability implied by an SLO (99.9% → 0.1% "budget"). Spend it on shipping fast; run out → freeze and stabilize. |
| **Toil** | Manual, repetitive, automatable ops work with no lasting value. The thing SREs relentlessly automate away. |
| **Golden signals** | Latency, Traffic, Errors, Saturation (Google SRE). Variants: **RED** (Rate, Errors, Duration), **USE** (Utilization, Saturation, Errors). |
| **Three pillars of observability** | Logs, metrics, traces. |
| **MTTR / MTBF** | Mean Time To Recovery / Between Failures. Lowering MTTR usually beats chasing zero failures. |
| **Blast radius** | Scope of impact when something fails; you architect to shrink it (namespaces, cells, quotas). |
| **Runbook** | A step-by-step "when X breaks, do Y" doc for on-call. |
| **Blameless postmortem** | Incident write-up focused on *system* fixes, not who to blame. |
| **Day 1 vs. Day 2 operations** | Day 1 = build/deploy. Day 2 = everything after: upgrades, scaling, backups, patching, monitoring. |
| **On-call / pager / incident** | Rotation carrying the pager; a page; a declared operational event. |

---

## Failure-mode idioms

| Term | Meaning |
|---|---|
| **Thundering herd** | Many clients wake/retry simultaneously and overwhelm a resource. |
| **Cascading failure** | One failure triggers the next in a chain until everything's down. |
| **Retry storm** | Aggressive automatic retries amplifying an outage instead of easing it. |
| **Circuit breaker** | Stop calling a failing dependency for a while so it can recover (Hystrix pattern). |
| **Backpressure** | Signaling upstream to *slow down* when you're overwhelmed, instead of falling over. |
| **Graceful degradation** | Serve reduced functionality under failure instead of a full outage. |
| **Fail fast** | Surface errors immediately rather than limping along in a bad state. |
| **Split brain** | A cluster partitions and both halves think they're in charge — data corruption risk. |
| **Quorum** | The majority needed to make decisions in a consensus system (etcd/Raft). Lose quorum → cluster goes read-only. |

---

## Org / platform culture

| Term | Meaning |
|---|---|
| **DevOps** | Culture + tooling collapsing the dev/ops wall — you automate the path from commit to prod. |
| **Platform engineering** | Building an internal platform so product devs self-serve infra instead of filing tickets. |
| **Internal Developer Platform (IDP)** | The self-service product a platform team builds (not to be confused with identity "IdP"). |
| **Paved road / golden path** | The supported, opinionated, easy way to do something. Step off it and you're on your own. |
| **You build it, you run it** | Dev teams own their services in production, pager included (Werner Vogels / Amazon). |
| **Cloud native** | Apps designed for elastic, containerized, orchestrated environments — the CNCF ecosystem. |
| **12-factor app** | A methodology for cloud apps (config in env vars, stateless processes, disposability...). |
| **Infrastructure as Code (IaC)** | Infra defined in version-controlled files (Terraform, Pulumi), not clicked in a console. |
| **Tech debt** | Shortcuts taken now that cost more to live with later. |

---

*Add to this as you hit new terms — that's the point. When an interviewer uses a word you had to*
*look up, drop it in here with a one-line gloss in your own words.*
