#!/bin/bash
set -e

echo "[INFO] Removing previous 'oracle' container if it exists..."
docker rm -f oracle 2>/dev/null || true

echo "[INFO] Starting Oracle container..."
docker-compose -f docker-compose.oracle.yml up -d

echo "[INFO] Waiting for Oracle to initialize (checking logs)..."
max_wait=120
elapsed=0
until docker logs oracle 2>&1 | grep -q "DATABASE IS READY TO USE"; do
  sleep 3
  elapsed=$((elapsed+3))
  if [ $elapsed -ge $max_wait ]; then
    echo "[ERROR] Timeout: Oracle did not start within ${max_wait}s"
    docker-compose -f docker-compose.oracle.yml down -v
    exit 1
  fi
done
echo "[OK] Oracle container is ready."

echo "[INFO] Checking if user 'WEATHER_EVALUATOR' exists..."
docker exec oracle bash -c 'sqlplus -s system/oracle@XEPDB1 <<EOF
SET HEADING OFF;
SET FEEDBACK OFF;
SELECT username FROM all_users WHERE username = '\''WEATHER_EVALUATOR'\'';
EXIT;
EOF' | grep -q "WEATHER_EVALUATOR" || {
  echo '[ERROR] User WEATHER_EVALUATOR not found in ALL_USERS.'
  docker-compose -f docker-compose.oracle.yml down -v
  exit 2
}
echo "[OK] User exists."

echo "[INFO] Verifying user has CREATE SESSION privilege..."
docker exec oracle bash -c 'sqlplus -s system/oracle@XEPDB1 <<EOF
SET HEADING OFF;
SET FEEDBACK OFF;
SELECT privilege FROM dba_sys_privs WHERE grantee = '\''WEATHER_EVALUATOR'\'' AND privilege = '\''CREATE SESSION'\'';
EXIT;
EOF' | grep -q "CREATE SESSION" || {
  echo '[ERROR] User does not have CREATE SESSION privilege.'
  docker-compose -f docker-compose.oracle.yml down -v
  exit 3
}
echo "[OK] User has CREATE SESSION."

echo "[INFO] Verifying user has RESOURCE role..."
docker exec oracle bash -c 'sqlplus -s system/oracle@XEPDB1 <<EOF
SET HEADING OFF;
SET FEEDBACK OFF;
SELECT granted_role FROM dba_role_privs WHERE grantee = '\''WEATHER_EVALUATOR'\'' AND granted_role = '\''RESOURCE'\'';
EXIT;
EOF' | grep -q "RESOURCE" || {
  echo '[ERROR] User does not have RESOURCE role.'
  docker-compose -f docker-compose.oracle.yml down -v
  exit 4
}
echo "[OK] User has RESOURCE."

echo "[INFO] Verifying user has UNLIMITED TABLESPACE..."
docker exec oracle bash -c 'sqlplus -s system/oracle@XEPDB1 <<EOF
SET HEADING OFF;
SET FEEDBACK OFF;
SELECT privilege FROM dba_sys_privs WHERE grantee = '\''WEATHER_EVALUATOR'\'' AND privilege = '\''UNLIMITED TABLESPACE'\'';
EXIT;
EOF' | grep -q "UNLIMITED TABLESPACE" || {
  echo '[ERROR] User does not have UNLIMITED TABLESPACE privilege.'
  docker-compose -f docker-compose.oracle.yml down -v
  exit 5
}
echo "[OK] User has UNLIMITED TABLESPACE."

echo "[INFO] Testing login as 'weather_evaluator'..."
docker exec oracle bash -c 'sqlplus -s weather_evaluator/weather_evaluator@XEPDB1 <<EOF
SELECT 1 FROM dual;
EXIT;
EOF' | grep -q "1" || {
  echo '[ERROR] Failed to log in as weather_evaluator.'
  docker-compose -f docker-compose.oracle.yml down -v
  exit 6
}
echo "[OK] Login as weather_evaluator successful."

echo "[INFO] Cleaning up..."
docker-compose -f docker-compose.oracle.yml down -v

echo "[INFO] All checks passed successfully!"
exit 0
