# Kibana Monitoring Setup

This guide explains how to view backend logs in Kibana using the local
Elasticsearch + Logstash + Kibana stack.

## Prerequisites

- Docker running
- The app running with Logstash enabled (local/dev/prod Spring profiles)
- Logstash pipeline at `observability/logstash/pipeline/logstash.conf`

## Start the stack

From repo root:
```
docker compose --profile local up -d elasticsearch logstash kibana
```

## Send logs to Logstash

If the app runs on the host:
```
LOGSTASH_HOST=localhost
LOGSTASH_PORT=5000
```

If the app runs in a container on the same compose network:
```
LOGSTASH_HOST=logstash
LOGSTASH_PORT=5000
```

The Spring Boot app already uses Logstash encoder when the profile is
`local`, `dev`, or `prod`.

## Configure Kibana

1) Open Kibana: `http://localhost:5601`
2) Go to "Stack Management" -> "Data Views"
3) Create a data view:
   - Name: `standard`
   - Index pattern: `standard-*`
   - Time field: `@timestamp`
4) Open "Discover" and select the `standard` data view

You should now see log documents. The Logstash pipeline writes to indexes like
`standard-YYYY.MM.dd`.

## Optional checks

- Verify Logstash can reach Elasticsearch:
  - `docker logs <logstash-container>`
- Verify logs are written:
  - `GET /actuator/health` or any API call should emit new log entries
