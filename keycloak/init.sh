#!/bin/sh

REALM_LIST="rciam"
SUPER_ADMIN_ROLE="${API_AUTH_ENTITLEMENTS_SUPER_ADMIN_ROLE:-super_admin}"
PARENT_GROUP="${API_AUTH_ENTITLEMENTS_PARENT_GROUP:-parent_group}"

set -eu

MAX_ATTEMPTS=60
SLEEP_SECONDS=3

echo "Waiting for Keycloak admin token endpoint to be ready..."

until curl -s -o /dev/null -w "%{http_code}" http://keycloak:8080/ | grep -qE "200|404|301|302"; do
    echo "Waiting for Keycloak..."
    sleep 5
done

echo "Keycloak is up (responding HTTP)"

attempt=1
ACCTOK=""

while [ "$attempt" -le "$MAX_ATTEMPTS" ]; do
    TOKEN_RESPONSE=$(curl -s -X POST "http://keycloak:8080/realms/master/protocol/openid-connect/token" \
            -H "Content-Type: application/x-www-form-urlencoded" \
            -d "username=tempadmin" \
            -d "password=tempadmin" \
            -d "grant_type=password" \
            -d "client_id=admin-cli")

        if [ $? -ne 0 ]; then
            echo "curl request failed, attempt ${attempt}/${MAX_ATTEMPTS}, retrying in ${SLEEP_SECONDS}s..."
            attempt=$((attempt + 1))
            sleep "$SLEEP_SECONDS"
            continue
        fi

        ACCTOK=$(echo "$TOKEN_RESPONSE" | sed 's/.*"access_token":"\([^"]*\)".*/\1/')

        if [ -n "$ACCTOK" ] && [ "$ACCTOK" != "$TOKEN_RESPONSE" ]; then
            echo "Keycloak is ready, got admin token (attempt ${attempt}/${MAX_ATTEMPTS})."
            break
        fi

        echo "Not ready yet, attempt ${attempt}/${MAX_ATTEMPTS}, retrying in ${SLEEP_SECONDS}s..."
        echo "DEBUG: response was: ${TOKEN_RESPONSE}"
        ACCTOK=""
        attempt=$((attempt + 1))
        sleep "$SLEEP_SECONDS"
done

if [ -z "$ACCTOK" ]; then
    echo "ERROR: Keycloak admin token could not be obtained after ${MAX_ATTEMPTS} attempts." >&2
    exit 1
fi

echo "Token obtained successfully."

echo "Configuring Member User Attribute..."
for REALM_NAME in $REALM_LIST; do
  echo ""
  echo "[${REALM_NAME}] Configuring..."

  # --- can select different namespaces ---
  case "$REALM_NAME" in
    rciam)
      URN_NAMESPACE="urn:rciam.example.org"
      ;;
    ishare)
      URN_NAMESPACE="urn:ishare.test.example.org"
      ;;
    *)
      # Default fallback if realm is not listed above
      URN_NAMESPACE="urn:default.example.org"
      echo "Warning: No specific namespace found for '$REALM_NAME'. Using default."
      ;;
  esac

  echo "Using namespace: $URN_NAMESPACE"

  curl -s -X POST "http://keycloak:8080/realms/${REALM_NAME}/agm/admin/member-user-attribute/configuration" \
    -H "Authorization: Bearer $ACCTOK" \
    -H "Content-Type: application/json" \
    -H "Accept: application/json" \
    -d "{\"userAttribute\": \"localEntitlements\", \"urnNamespace\": \"$URN_NAMESPACE\", \"signatureMessage\": \"RCIAM Support team\"}"

  if [ $? -eq 0 ]; then
    echo "✓ [${REALM_NAME}] Success"
  else
    echo "✗ [${REALM_NAME}] Failed"
  fi

sleep 1

echo "Getting Group Management Admin Token..."
TOKEN_AGM_RESPONSE=$(curl -s -X POST "http://keycloak:8080/realms/rciam/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_secret=nlKena9eYtF7XMeP8kZD0YU9pStxPggN" \
  -d "client_id=status.page.manage.groups" \
  -d "grant_type=client_credentials")

if [ $? -ne 0 ]; then
  echo "ERROR: Failed to get group management token"
  exit 1
fi

# Extract token
ACCTOK=$(echo "$TOKEN_AGM_RESPONSE" | sed 's/.*"access_token":"\([^"]*\)".*/\1/')

if [ -z "$ACCTOK" ]; then
  echo "ERROR: Could not extract access token"
  exit 1
fi

echo "Group management token obtained successfully."

response=$(curl -s -w "\n%{http_code}" -X POST \
  "http://keycloak:8080/realms/${REALM_NAME}/agm/account/group-admin/group" \
  -H "Authorization: Bearer $ACCTOK" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d "{\"name\": \"$PARENT_GROUP\"}")

status=$(echo "$response" | tail -n1)

if [ "$status" -lt 200 ] || [ "$status" -ge 300 ]; then
  echo "❌ Request failed with HTTP $status"
  exit 1
fi

echo "✅ Success: '${PARENT_GROUP}' group created successfully"

sleep 1

response=$(curl -s -w "\n%{http_code}" -X GET \
  "http://keycloak:8080/realms/${REALM_NAME}/agm/account/group-admin/groups" \
  -H "Authorization: Bearer $ACCTOK")

body=$(echo "$response" | sed '$d')
status=$(echo "$response" | tail -n1)

if [ "$status" -lt 200 ] || [ "$status" -ge 300 ]; then
  echo "❌ HTTP error: $status"
  exit 1
fi

ID=$(echo "$body" | awk -F'"' '
  {
    for (i=1; i<=NF; i++) {
      if ($i == "id") {
        print $(i+2)
        exit
      }
    }
  }
')

response=$(curl -s -w "\n%{http_code}" -X POST \
  "http://keycloak:8080/realms/${REALM_NAME}/agm/account/group-admin/group/${ID}/roles?name=${SUPER_ADMIN_ROLE}" \
  -H "Authorization: Bearer $ACCTOK" \
  -H "Accept: application/json")

status=$(echo "$response" | tail -n1)

if [ "$status" -lt 200 ] || [ "$status" -ge 300 ]; then
  echo "❌ Failed to add role '${SUPER_ADMIN_ROLE}' to '${PARENT_GROUP}' group. HTTP $status"
  exit 1
fi

echo "✅ Success: role '${SUPER_ADMIN_ROLE}' added to '${PARENT_GROUP}' group"

response=$(curl -s -w "\n%{http_code}" -X POST \
  "http://keycloak:8080/realms/${REALM_NAME}/agm/account/group-admin/group/${ID}/children" \
  -H "Authorization: Bearer $ACCTOK" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d "{\"name\": \"members\"}")

status=$(echo "$response" | tail -n1)

if [ "$status" -lt 200 ] || [ "$status" -ge 300 ]; then
  echo "❌ Request failed with HTTP $status"
  exit 1
fi

echo "✅ Success: members group created successfully"


response=$(curl -s -w "\n%{http_code}" -X POST \
  "http://keycloak:8080/realms/${REALM_NAME}/agm/account/group-admin/group/${ID}/admin?username=service-account-status.page.manage.groups" \
  -H "Authorization: Bearer $ACCTOK" \
  -H "Accept: application/json")

status=$(echo "$response" | tail -n1)

if [ "$status" -lt 200 ] || [ "$status" -ge 300 ]; then
  echo "❌ Failed to make service account group admin. HTTP $status"
  echo "$response"
  exit 1
fi

echo "✅ Success: service-account-status.page.manage.groups added as group admin for '${PARENT_GROUP}'"


response=$(curl -s -w "\n%{http_code}" -X POST \
  "http://keycloak:8080/realms/${REALM_NAME}/agm/account/group-admin/group/${ID}/members" \
  -H "Authorization: Bearer $ACCTOK" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d "{
    \"user\": {
      \"username\": \"admin\"
    },
    \"groupRoles\": [
      \"$SUPER_ADMIN_ROLE\"
    ]
  }")

status=$(echo "$response" | tail -n1)

if [ "$status" -lt 200 ] || [ "$status" -ge 300 ]; then
  echo "❌ Request failed with HTTP $status"
  exit 1
fi

echo "✅ Success: admin added to '${PARENT_GROUP}' group successfully"


done
echo ""
echo "All steps completed successfully."
touch /tmp/entitlements-done
sleep 5