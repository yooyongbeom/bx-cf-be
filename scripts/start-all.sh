#!/bin/bash
BASE=~/apps/bx-cf-be
PROFILE=dev
JAVA_OPTS="-Dfile.encoding=UTF-8 -Duser.timezone=Asia/Seoul -Xms512m -Xmx1024m"

mkdir -p $BASE/logs

# GitHub Actions self-hosted runner(macOS)는 job 종료 시
# RUNNER_TRACKING_ID 환경변수가 일치하는 자식 프로세스를 모두 정리한다.
# env -u 로 이 변수를 제거해 runner 의 정리 대상에서 벗어나게 한다.
# (setsid 는 macOS 에 없으므로 사용하지 않는다)
# 초기 부팅 로그는 bootstrap 로그로 남겨 기동 실패 시 원인을 확인한다.
start_svc() {
  local name=$1
  local jar=$2
  env -u RUNNER_TRACKING_ID nohup java $JAVA_OPTS \
    -Dspring.profiles.active=$PROFILE \
    -jar $jar \
    < /dev/null > $BASE/logs/$name-bootstrap.log 2>&1 &
  echo $! > $BASE/$name/$name.pid
}

echo "Starting discovery-svc..."
start_svc discovery-svc $BASE/discovery-svc/discovery-svc-0.0.1-SNAPSHOT.jar
sleep 20

echo "Starting auth-svc..."
start_svc auth-svc $BASE/auth-svc/auth-svc-0.0.1-SNAPSHOT.jar

echo "Starting product-svc..."
start_svc product-svc $BASE/product-svc/product-svc-0.0.1-SNAPSHOT.jar

echo "Starting system-svc..."
start_svc system-svc $BASE/system-svc/system-svc-0.0.1-SNAPSHOT.jar

# MCI는 Gateway 뒤에서 호출되는 업무 서비스이므로 Gateway보다 먼저 Eureka에 등록한다.
echo "Starting mci-svc..."
start_svc mci-svc $BASE/mci-svc/mci-svc-0.0.1-SNAPSHOT.jar

echo "Starting integration-svc..."
start_svc integration-svc $BASE/integration-svc/integration-svc-0.0.1-SNAPSHOT.jar
sleep 20

echo "Starting api-gateway..."
start_svc api-gateway $BASE/api-gateway/api-gateway-0.0.1-SNAPSHOT.jar

echo "All services started."
