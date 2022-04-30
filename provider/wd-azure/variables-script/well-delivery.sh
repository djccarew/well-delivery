#!/usr/bin/env bash
#
#  Purpose: Create the Developer Environment Variables.
#  Usage:
#    well-planning.sh

###############################
## ARGUMENT INPUT            ##
###############################
usage() { echo "Usage: DNS_HOST=<your_host> NO_DATA_ACCESS_TESTER=<no_data_access_tester_id> NO_DATA_ACCESS_TESTER_SERVICEPRINCIPAL_SECRET=<no_data_access_tester_secret> well_planning.sh " 1>&2; exit 1; }

SERVICE="well_delivery"


if [ -z $UNIQUE ]; then
  tput setaf 1; echo 'ERROR: UNIQUE not provided' ; tput sgr0
  usage;
fi

if [ -z $DNS_HOST ]; then
  tput setaf 1; echo 'ERROR: DNS_HOST not provided' ; tput sgr0
  usage;
fi

if [ -z $COMMON_VAULT ]; then
  tput setaf 1; echo 'ERROR: COMMON_VAULT not provided' ; tput sgr0
  usage;
fi

if [ -z $NO_DATA_ACCESS_TESTER ]; then
  tput setaf 1; echo 'ERROR: NO_DATA_ACCESS_TESTER not provided' ; tput sgr0
  usage;
fi

if [ -z $NO_DATA_ACCESS_TESTER_SERVICEPRINCIPAL_SECRET ]; then
  tput setaf 1; echo 'ERROR: NO_DATA_ACCESS_TESTER_SERVICEPRINCIPAL_SECRET not provided' ; tput sgr0
  usage;
fi

if [ ! -d environments/$UNIQUE ]; then mkdir -p environments/$UNIQUE; fi

# ------------------------------------------------------------------------------------------------------
# Generating required variables using Azure CLI
# ------------------------------------------------------------------------------------------------------

# ------------------------------------------------------------------------------------------------------
# KEY_VAULT
# ------------------------------------------------------------------------------------------------------

GROUP=$(az group list --query "[?contains(name, 'cr${UNIQUE}')].name" -otsv)
ENV_VAULT=$(az keyvault list --resource-group $GROUP --query [].name -otsv)

# ------------------------------------------------------------------------------------------------------
# Environment Settings
# ------------------------------------------------------------------------------------------------------
ENV_APP_ID="$(az keyvault secret show --id https://${ENV_VAULT}.vault.azure.net/secrets/aad-client-id --query value -otsv)"
ENV_PRINCIPAL_ID="$(az keyvault secret show --id https://${ENV_VAULT}.vault.azure.net/secrets/app-dev-sp-username --query value -otsv)"
ENV_PRINCIPAL_SECRET="$(az keyvault secret show --id https://${ENV_VAULT}.vault.azure.net/secrets/app-dev-sp-password --query value -otsv)"
TENANT_ID="$(az account show --query tenantId -otsv)"
KEYVAULT_URI="https://${ENV_VAULT}.vault.azure.net/"

# ------------------------------------------------------------------------------------------------------
# LocalHost Run Settings: Variables required to run the service locally
# ------------------------------------------------------------------------------------------------------

# Constants
azure_istioauth_enabled="true"
entitlements_service_api_key="OBSOLETE"

# Service endpoints
legal_service_endpoint="https://${DNS_HOST}/api/legal/v1"
entitlements_service_endpoint="https://${DNS_HOST}/api/entitlements/v2"
storage_service_endpoint="https://${DNS_HOST}/api/storage/v2"
schema_service_endpoint="https://${DNS_HOST}/api/schema-service/v1"
partition_service_endpoint="https://${DNS_HOST}/api/partition/v1"

# Environment Variables
aad_client_id="${ENV_APP_ID}"
AZURE_TENANT_ID="${TENANT_ID}"
AZURE_CLIENT_ID="${ENV_PRINCIPAL_ID}"
AZURE_CLIENT_SECRET="${ENV_PRINCIPAL_SECRET}"
KEYVAULT_URI="${KEYVAULT_URI}"

# ------------------------------------------------------------------------------------------------------
# Integration test Settings: Variables required to test the service
# ------------------------------------------------------------------------------------------------------

# Constants
TENANT_NAME="opendes"
DOMAIN="contoso.com"
PUBSUB_TOKEN="az"
DEPLOY_ENV="empty"

# Service Endpoints
STORAGE_URL="https://${DNS_HOST}/api/storage/v2/"
SCHEMA_URL="https://${DNS_HOST}/api/schema-service/v1/"
LEGAL_URL="https://${DNS_HOST}/api/legal/v1/"
ENTITY_URL_LOCAL="http://localhost:8080/api/well-delivery/"
ENTITY_URL_REMOTE="https://${DNS_HOST}/api/well-delivery/"

# Environment Variables
INTEGRATION_TESTER="${ENV_PRINCIPAL_ID}"
TESTER_SERVICEPRINCIPAL_SECRET="${ENV_PRINCIPAL_SECRET}"
AZURE_AD_TENANT_ID="${TENANT_ID}"
AZURE_AD_APP_RESOURCE_ID="${ENV_APP_ID}"
NO_DATA_ACCESS_TESTER="${NO_DATA_ACCESS_TESTER}"
NO_DATA_ACCESS_TESTER_SERVICEPRINCIPAL_SECRET="${NO_DATA_ACCESS_TESTER_SERVICEPRINCIPAL_SECRET}"

cat > environments/${UNIQUE}/${SERVICE}_local.yaml <<LOCALRUN
# Constants
azure_istioauth_enabled: "${azure_istioauth_enabled}"
entitlements_service_api_key: "${entitlements_service_api_key}"

# Service endpoints
legal_service_endpoint: "${legal_service_endpoint}"
entitlements_service_endpoint: "${entitlements_service_endpoint}"
storage_service_endpoint: "${storage_service_endpoint}"
schema_service_endpoint: "${schema_service_endpoint}"
partition_service_endpoint: "${partition_service_endpoint}"

# Environment Variables
aad_client_id: "${aad_client_id}"
AZURE_TENANT_ID: "${AZURE_TENANT_ID}"
AZURE_CLIENT_ID: "${AZURE_CLIENT_ID}"
AZURE_CLIENT_SECRET: "${AZURE_CLIENT_SECRET}"
KEYVAULT_URI: "${KEYVAULT_URI}"
LOCALRUN

cat > environments/${UNIQUE}/${SERVICE}_local_test.yaml <<LOCALTEST
# Constants
TENANT_NAME: "${TENANT_NAME}"
DOMAIN: "${DOMAIN}"
PUBSUB_TOKEN: "${PUBSUB_TOKEN}"
DEPLOY_ENV: "${DEPLOY_ENV}"

# Service Endpoints
STORAGE_URL: "${STORAGE_URL}"
SCHEMA_URL: "${SCHEMA_URL}"
LEGAL_URL: "${LEGAL_URL}"
ENTITY_URL: "${ENTITY_URL_LOCAL}"

# Environment Variables
INTEGRATION_TESTER: "${INTEGRATION_TESTER}"
TESTER_SERVICEPRINCIPAL_SECRET: "${TESTER_SERVICEPRINCIPAL_SECRET}"
AZURE_AD_TENANT_ID: "${AZURE_AD_TENANT_ID}"
AZURE_AD_APP_RESOURCE_ID: "${AZURE_AD_APP_RESOURCE_ID}"
NO_DATA_ACCESS_TESTER: "${NO_DATA_ACCESS_TESTER}"
NO_DATA_ACCESS_TESTER_SERVICEPRINCIPAL_SECRET: "${NO_DATA_ACCESS_TESTER_SERVICEPRINCIPAL_SECRET}"
LOCALTEST

cat > environments/${UNIQUE}/${SERVICE}_remote_test.yaml <<LOCALTEST
# Constants
TENANT_NAME: "${TENANT_NAME}"
DOMAIN: "${DOMAIN}"
PUBSUB_TOKEN: "${PUBSUB_TOKEN}"
DEPLOY_ENV: "${DEPLOY_ENV}"

# Service Endpoints
STORAGE_URL: "${STORAGE_URL}"
SCHEMA_URL: "${SCHEMA_URL}"
LEGAL_URL: "${LEGAL_URL}"
ENTITY_URL: "${ENTITY_URL_REMOTE}"

# Environment Variables
INTEGRATION_TESTER: "${INTEGRATION_TESTER}"
TESTER_SERVICEPRINCIPAL_SECRET: "${TESTER_SERVICEPRINCIPAL_SECRET}"
AZURE_AD_TENANT_ID: "${AZURE_AD_TENANT_ID}"
AZURE_AD_APP_RESOURCE_ID: "${AZURE_AD_APP_RESOURCE_ID}"
NO_DATA_ACCESS_TESTER: "${NO_DATA_ACCESS_TESTER}"
NO_DATA_ACCESS_TESTER_SERVICEPRINCIPAL_SECRET: "${NO_DATA_ACCESS_TESTER_SERVICEPRINCIPAL_SECRET}"
LOCALTEST