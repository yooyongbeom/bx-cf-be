#!/bin/bash
BASE=~/apps/bx-cf-be
PROFILE=dev
JAVA_OPTS="-Dfile.encoding=UTF-8 -Duser.timezone=Asia/Seoul -Xms512m -Xmx1024m"

# GitHub Actions self-hosted runner는 job 종료 시 자식 프로세스를 모두 정리한다.
# - RUNNER_TRACKING_ID 환경변수를 제거하고(env -u)
# - setsid 로 runner 와 분리된 새 세션을 만들어
# 서비스가 job 종료 후에도 살아있게 한다.
start_svc() {
  local name=$1
  local jar=$2
  env -u RUNNER_TRACKING_ID setsid bash -c "
    echo \$\$ > $BASE/$name/$name.pid
    exec java $JAVA_OPTS -Dspring.profiles.active=$PROFILE -jar $jar
  " < /dev/null > /dev/null 2>&1 &
}

echo "Starting discovery-svc..."
start_svc discovery-svc $BASE/discovery-svc/discovery-svc-0.0.1-SNAPSHOT.jar
sleep 20

echo "Starting auth-svc..."
start_svc auth-svc $BASE/auth-svc/auth-svc-0.0.1-SNAPSHOT.jar

echo "Starting product-svc..."
start_svc product-svc $BASE/product-svc/product-svc-0.0.1-SNAPSHOT.jar
sleep 20

echo "Starting api-gateway..."
start_svc api-gateway $BASE/api-gateway/api-gateway-0.0.1-SNAPSHOT.jar

echo "All services started."
