# kubectl / helm / k3d — Command Cheat-Sheet

Practical commands for **this** project. Copy-paste ready.

```
Cluster:   qa-practice          Context:   k3d-qa-practice
Namespace: qa                   Release:   qa
Public:    http://qa.127.0.0.1.sslip.io/hello
```

> Tip: set your default namespace once and you can drop `-n qa` everywhere:
> `kubectl config set-context --current --namespace=qa`

---

## Look at what's running (namespace `qa`)

```bash
kubectl get pods -n qa                 # the overview: READY / STATUS / RESTARTS
kubectl get pods -n qa -w              # -w = watch, auto-refreshes live (Ctrl-C to stop)
kubectl get pods -n qa -o wide         # + node + pod IP
kubectl get all -n qa                  # pods + services + deployments + replicasets
kubectl get svc -n qa                  # services (TYPE: ClusterIP = internal, etc.)
kubectl get ingress -n qa              # public hostnames (the HOSTS column)
kubectl get endpoints -n qa            # which pod IPs sit behind each Service
kubectl get deploy -n qa               # deployments + how many replicas are ready
kubectl get secret -n qa               # secrets (values are base64, not shown)
kubectl get events -n qa --sort-by=.lastTimestamp   # recent cluster events
```

## Inspect / debug ONE thing

```bash
kubectl describe pod <pod> -n qa       # ⭐ Events at the bottom = why it's unhealthy
kubectl logs <pod> -n qa               # the app's stdout (Spring stack traces)
kubectl logs <pod> -n qa -f            # -f = follow (stream new lines)
kubectl logs <pod> -n qa --previous    # logs from the PREVIOUS (crashed) container
kubectl logs deploy/qa-backend -n qa   # logs via a deployment (picks one of its pods)
kubectl exec -it <pod> -n qa -- sh     # shell inside a pod
```

### Status decoder
| READY / STATUS | Meaning | First look |
|---|---|---|
| `1/1 Running` | healthy | 🎉 |
| `0/1 Running` | up but readiness probe failing | `describe` + `logs` |
| `Pending` | can't be scheduled | `describe` → Events |
| `ContainerCreating` | starting (normal, briefly) | wait, then `describe` if stuck |
| `CrashLoopBackOff` | crashing on boot | `logs --previous` |
| `ImagePullBackOff` | can't fetch image | `describe` → Events |

## Reach a service

```bash
# Public (gateway, via Ingress):
curl -s http://qa.127.0.0.1.sslip.io/hello | jq .

# Internal service → tunnel it to localhost:
kubectl port-forward -n qa svc/qa-backend 8081:8080     # then: curl localhost:8081/hello

# One-off curl from INSIDE the cluster (throwaway pod):
kubectl run tmp --rm -it --image=curlimages/curl -n qa -- \
  curl -s http://qa-backend:8080/hello
```

## Helm (manage the release)

```bash
helm list -n qa                        # installed releases
helm status qa -n qa                   # release status + NOTES
helm get values qa -n qa               # values that were used
helm get manifest qa -n qa             # the exact YAML applied to the cluster
helm template qa ./charts/qa-service   # render locally, print, DON'T touch cluster
helm lint ./charts/qa-service          # sanity-check the chart

helm upgrade qa ./charts/qa-service -n qa --set backend.replicas=3   # change one thing
helm history qa -n qa                  # revision history
helm rollback qa 1 -n qa               # roll back to revision 1
helm uninstall qa -n qa                # remove the whole release
```

## Rollout / scale (usually done via Helm/Git, but handy)

```bash
kubectl rollout status  deploy/qa-backend -n qa    # watch a rollout finish
kubectl rollout restart deploy/qa-backend -n qa    # restart pods (re-pull, re-read config)
kubectl rollout undo    deploy/qa-backend -n qa    # roll back last rollout
kubectl scale deploy/qa-backend -n qa --replicas=3 # ⚠️ direct edit = drift from Git
```

## k3d (the cluster itself)

```bash
k3d cluster list
k3d cluster stop  qa-practice          # pause (frees CPU/RAM, keeps state)
k3d cluster start qa-practice          # resume
k3d image import <image> -c qa-practice   # load a local Docker image into the cluster
```

## k9s (terminal UI)

```bash
k9s -n qa        # launch scoped to the qa namespace
# inside k9s:  :pod ⏎  :svc ⏎  :ing ⏎   switch views
#              l = logs   d = describe   s = shell   Ctrl-D = delete   :q = quit
```

## Context & namespace

```bash
kubectl config get-contexts            # list contexts (NAMESPACE col = current ns)
kubectl config current-context
kubectl config set-context --current --namespace=qa   # set default ns → drop -n later
kubectl get namespaces
```

## Teardown (full clean-up — nothing left on the host)

```bash
helm uninstall qa -n qa                # remove the app
kubectl delete namespace qa            # remove the namespace
k3d cluster delete qa-practice         # remove the whole cluster (all its containers)
docker image prune -a                  # reclaim pulled images (optional)
```
