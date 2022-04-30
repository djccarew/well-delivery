# Well Delivery DDMS Environment Variable Helper Script
This directory has a bash script that helps generate the environment variables needed to run and integration test well delivery DDMS. 

The script generates .yaml files to be used to (1) run the service locally (2) run integration tests against the service running locally (3) run integration tests against the service running remotely. Note that the usage of these files requires the [Envfile](https://plugins.jetbrains.com/plugin/7861-envfile) plugin for IntelliJ

## Prerequisites

- Access to .envrc file for the enviornment for which you are generating the .yamls and .envrc files. This file sets the following variables which are required to run the scripts: `UNIQUE`, `COMMON_VAULT`
- DNS host for the environment you are using
- Access to id and secret for no access tester info
- Azure CLI installed on your machine

## Usage

First, you must load in the environment's variables using your .envrc file.

```bash
# This logs your local Azure CLI in using the configured service principal.

az login --service-principal -u $ARM_CLIENT_ID -p $ARM_CLIENT_SECRET --tenant $ARM_TENANT_ID
```

**Set other required variables**
```bash
export DNS_HOST = ... # ex osdu-glab.msft-osdu-test.org
export NO_DATA_ACCESS_TESTER  = ... # value can be retrieved for most cases by using export NO_DATA_ACCESS_TESTER="$(az keyvault secret show --id https://$COMMON_VAULT.vault.azure.net/secrets/osdu-mvp-$UNIQUE-noaccess-clientid --query value -otsv)"
export NO_DATA_ACCESS_TESTER_SERVICEPRINCIPAL_SECRET = ... # value can be retrieved for most cases by using export NO_DATA_ACCESS_TESTER_SERVICEPRINCIPAL_SECRET="$(az keyvault secret show --id https://$COMMON_VAULT.vault.azure.net/secrets/osdu-mvp-$UNIQUE-noaccess-secret --query value -otsv)"
```

**Run the script**
```bash
# Example of running 
./well-delivery.sh
```