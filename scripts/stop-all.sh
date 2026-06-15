#!/bin/bash
BASE=~/apps/bx-cf-be

SERVICES=("api-gateway" "product-svc" "auth-svc" "discovery-svc")

for svc in "${SERVICES[@]}"; do
    PID_FILE=$BASE/$svc/$svc.pid
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if kill -0 "$PID" 2>/dev/null; then
            echo "Stopping $svc (PID: $PID)..."
            kill "$PID"
        else
            echo "$svc is not running."
        fi
        rm -f "$PID_FILE"
    else
        echo "PID file not found for $svc, skipping."
    fi
done

echo "All services stopped."
