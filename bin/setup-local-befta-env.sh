#!/usr/bin/env bash

set -euo pipefail

script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
repo_dir=$(cd "$script_dir/.." && pwd)
env_file="$repo_dir/.env.befta.local.env"
template_file="$repo_dir/.env.befta.local.env.example"

if [[ -e "$env_file" ]]; then
  echo "Refusing to overwrite existing $env_file" >&2
  exit 1
fi

definition_importer_password=${DEFINITION_IMPORTER_PASSWORD:?Set DEFINITION_IMPORTER_PASSWORD}
master_caseworker_password=${CCD_BEFTA_MASTER_CASEWORKER_PWD:?Set CCD_BEFTA_MASTER_CASEWORKER_PWD}
service_idam_client_secret=${CCD_NEXT_HEARING_DATE_UPDATER_SERVICE_IDAM_CLIENT_SECRET:?Set CCD_NEXT_HEARING_DATE_UPDATER_SERVICE_IDAM_CLIENT_SECRET}
system_user_username=${IDAM_NEXT_HEARING_DATE_SYSTEM_USER:?Set IDAM_NEXT_HEARING_DATE_SYSTEM_USER}
system_user_password=${IDAM_NEXT_HEARING_DATE_SYSTEM_PASSWORD:?Set IDAM_NEXT_HEARING_DATE_SYSTEM_PASSWORD}
s2s_secret=${IDAM_KEY_NEXT_HEARING_UPDATER:?Set IDAM_KEY_NEXT_HEARING_UPDATER}
befta_s2s_client_secret=${BEFTA_S2S_CLIENT_SECRET:?Set BEFTA_S2S_CLIENT_SECRET}
xui_s2s_client_secret=${BEFTA_S2S_CLIENT_SECRET_OF_XUI_WEBAPP:?Set BEFTA_S2S_CLIENT_SECRET_OF_XUI_WEBAPP}
xui_oauth2_client_secret=${BEFTA_OAUTH2_CLIENT_SECRET_OF_XUIWEBAPP:?Set BEFTA_OAUTH2_CLIENT_SECRET_OF_XUIWEBAPP}
ccd_gateway_s2s_key=${CCD_API_GATEWAY_S2S_KEY:?Set CCD_API_GATEWAY_S2S_KEY}
ccd_gateway_oauth2_client_secret=${CCD_API_GATEWAY_OAUTH2_CLIENT_SECRET:?Set CCD_API_GATEWAY_OAUTH2_CLIENT_SECRET}

template=$(<"$template_file")
template=${template//replace-with-definition-importer-password/$definition_importer_password}
template=${template//replace-with-master-caseworker-password/$master_caseworker_password}
template=${template//replace-with-service-idam-client-secret/$service_idam_client_secret}
template=${template//replace-with-system-user-username/$system_user_username}
template=${template//replace-with-system-user-password/$system_user_password}
template=${template//replace-with-s2s-secret/$s2s_secret}
template=${template//replace-with-befta-s2s-client-secret/$befta_s2s_client_secret}
template=${template//replace-with-xui-s2s-client-secret/$xui_s2s_client_secret}
template=${template//replace-with-xui-oauth2-client-secret/$xui_oauth2_client_secret}
template=${template//replace-with-ccd-api-gateway-s2s-key/$ccd_gateway_s2s_key}
template=${template//replace-with-ccd-api-gateway-oauth2-client-secret/$ccd_gateway_oauth2_client_secret}

if [[ "$template" == *replace-with-* ]]; then
  echo "Template contains an unreplaced credential placeholder" >&2
  exit 1
fi

umask 077
printf '%s\n' "$template" > "$env_file"
echo "Created $env_file with supplied local BEFTA values. Do not commit it."
