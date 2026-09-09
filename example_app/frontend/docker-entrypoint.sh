#!/bin/sh
set -eu

API_BASE_URL="${API_BASE_URL:-http://backend:8080/api/v1}"
AUTH_BASE_URL="${AUTH_BASE_URL:-http://auth:8081/api/v1/auth}"

# envsubst only substitutes exported environment variables.
export API_BASE_URL AUTH_BASE_URL

echo "Generating nginx config from template"
echo "- API_BASE_URL=${API_BASE_URL}"
echo "- AUTH_BASE_URL=${AUTH_BASE_URL}"

envsubst '${API_BASE_URL} ${AUTH_BASE_URL}' \
  < /etc/nginx/templates/nginx.conf.template \
  > /etc/nginx/conf.d/default.conf

exec nginx -g 'daemon off;'

