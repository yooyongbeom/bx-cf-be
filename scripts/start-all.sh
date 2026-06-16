#!/bin/bash
BASE=~/apps/bx-cf-be
PROFILE=dev
JAVA_OPTS="-Dfile.encoding=UTF-8 -Duser.timezone=Asia/Seoul -Xms512m -Xmx1024m"

echo "Starting discovery-svc..."
nohup java $JAVA_OPTS \
  -Dspring.profiles.active=$PROFILE \
  -jar $BASE/discovery-svc/discovery-svc-0.0.1-SNAPSHOT.jar \
  > /dev/null 2>&1 &
echo $! > $BASE/discovery-svc/discovery-svc.pid
sleep 20

echo "Starting auth-svc..."
nohup java $JAVA_OPTS \
  -Dspring.profiles.active=$PROFILE \
  -jar $BASE/auth-svc/auth-svc-0.0.1-SNAPSHOT.jar \
  > /dev/null 2>&1 &
echo $! > $BASE/auth-svc/auth-svc.pid

echo "Starting product-svc..."
nohup java $JAVA_OPTS \
  -Dspring.profiles.active=$PROFILE \
  -jar $BASE/product-svc/product-svc-0.0.1-SNAPSHOT.jar \
  > /dev/null 2>&1 &
echo $! > $BASE/product-svc/product-svc.pid
sleep 20

echo "Starting api-gateway..."
nohup java $JAVA_OPTS \
  -Dspring.profiles.active=$PROFILE \
  -jar $BASE/api-gateway/api-gateway-0.0.1-SNAPSHOT.jar \
  > /dev/null 2>&1 &
echo $! > $BASE/api-gateway/api-gateway.pid

echo "All services started."
