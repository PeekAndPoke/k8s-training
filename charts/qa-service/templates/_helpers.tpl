{{/*
Common labels stamped onto every object. The app.kubernetes.io/* keys are the
Kubernetes-recommended standard labels — tools (kubectl, ArgoCD, dashboards) understand them.

Resource names are built inline as "{{ .Release.Name }}-<component>", e.g. release "qa"
=> qa-gateway, qa-backend, qa-redis. Prefixing with the release name is what makes per-PR
environments isolated in Phase 6: each release (each PR) gets uniquely-named Services, so a
gateway's BACKEND_URL always resolves to *its own* backend within the same namespace.
*/}}
{{- define "qa.labels" -}}
app.kubernetes.io/part-of: qa-service
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version }}
{{- end -}}
