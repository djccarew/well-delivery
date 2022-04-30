# Well-delivery-gcp


### Database structure
```
CREATE TABLE IF NOT EXISTS public.jdbc_entity
(
    id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    data jsonb,
    relationships jsonb,
	deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT json_entity_pkey PRIMARY KEY (id)
)
TABLESPACE pg_default;
ALTER TABLE public.jdbc_entity
    OWNER to postgres;
```

### Requirements

In order to run this service from a local machine to Cloud Run, you need the following:

- [Maven 3.6.0+](https://maven.apache.org/download.cgi)
- [AdoptOpenJDK8](https://adoptopenjdk.net/)
- [Google Cloud SDK](https://cloud.google.com/sdk/)
- [Docker](https://docs.docker.com/engine/install/)

### General Tips

**Environment Variable Management**
The following tools make environment variable configuration simpler
 - [direnv](https://direnv.net/) - for a shell/terminal environment
 - [EnvFile](https://plugins.jetbrains.com/plugin/7861-envfile) - for [Intellij IDEA](https://www.jetbrains.com/idea/)

**Lombok**
This project uses [Lombok](https://projectlombok.org/) for code generation. You may need to configure your IDE to take advantage of this tool.
 - [Intellij configuration](https://projectlombok.org/setup/intellij)
 - [VSCode configuration](https://projectlombok.org/setup/vscode)
 
 ### Environment Variables
 
 | name | value | description | sensitive? | source |
 | ---  | ---   | ---         | ---        | ---    |
 | `WELL_DELIVERY_DB_URL` | ex `jdbc:postgresql://localhost:5432/postgres` | The JDBC-valid connection string for database | yes | https://console.cloud.google.com/ |
 | `WELL_DELIVERY_DB_USERNAME` | ex `postgres` | The username of database user | yes | - | 
 | `WELL_DELIVERY_DB_PASSWORD` | ex `********` | The password of database user | yes | - |
 | `LEGAL_SERVICE_ENDPOINT` | ex `http://legal/api/legal/v1` | Legal service endpoint | no | - |
 | `ENTITLEMENTS_SERVICE_ENDPOINT` | ex `http://entitlements/api/entitlements/v2` | Entitlements service endpoint | no | - |
 | `STORAGE_SERVICE_ENDPOINT` | ex `http://storage/api/storage/v2` | Storage service endpoint | no | - |
 | `SCHEMA_SERVICE_ENDPOINT` | ex `http://schema/api/schema-service/v1` | Schema service endpoint | no | - |
 | `PARTITION_SERVICE_ENDPOINT` | ex `http://partition/api/partition/v1/` | Partition service endpoint | no | - |
 | `APP_ENTITY_STORAGE` | ex `true` or `false` | Flag that enables records writing to Storage service if set to `true` | no | - |
 | `server_port` | ex `8080` | Port of the server | no | -- |
 | `GOOGLE_APPLICATION_CREDENTIALS` | `********` | Need this only if running locally, this service acc must have token sign access | yes | -- |
 
 ### Build and run the application
 
 After configuring your environment as specified above, you can follow these steps to build and run the application. These steps should be invoked from the *repository root.*
 
 ```bash
 # build + test + install core service code
 $ ./mvnw clean install
 
 # run service
 #
 # Note: this assumes that the environment variables for running the service as outlined
 #       above are already exported in your environment.
 $ java -jar $(find provider/wd-gcp/target/ -name '*-spring-boot.jar')
 
 # Alternately you can run using the Maven Task
 $ ./mvnw spring-boot:run -pl provider/wd-gcp
 ```
 
 
 ### Test the application
 
 
 ### Integration Tests
 
 In order to run integration tests, you need to have the following environment variables defined:
 
 | Name | Value | Description | Sensitive? | Source |
 | ---  | ---   | ---         | ---        | ---    |
 | `SCHEMA_URL` | ex `http://schema/api/schema-service/v1/` | Schema service endpoint | no | -- |
 | `STORAGE_URL` | ex `http://storage/api/storage/v2/` | Storage service endpoint | no | -- |
 | `ENTITY_URL` | ex `http://well-delivery/api/well-delivery/` | Well-delivery service endpoint | no | -- |
 | `LEGAL_URL` | ex `http://legal/api/legal/v1/` | Legal service endpoint | no | -- |
 | `DOMAIN` | ex `osdu-gcp.go3-nrg.projects.epam.com` | Must match the value of `service_domain_name` above | no | -- |
 | `TENANT_NAME` | ex `opendes` | OSDU tenant used for testing | no | -- |
 | `INTEGRATION_TESTER` | `********` | Base64 encoded service account key. Note: This user must have entitlements already configured | yes | -- |
 | `INTEGRATION_TEST_AUDIENCE` | `********` | Client Id for `$INTEGRATION_TESTER` | yes | -- |
 | `DEPLOY_ENV` | `empty` | Required but not used, should be set up with string "empty"| no | - |
 
  **Entitlements configuration for integration accounts**
  
  | INTEGRATION_TESTER | 
  | ---  | 
  | users<br/>service.entitlements.user<br/>service.storage.creator<br/>service.storage.viewer<br/>data.test1<br/>data.integration.test<br/>service.schema-service.editors | 
  
  Execute following command to build code and run all the integration tests:
  
  ```bash
  # Note: this assumes that the environment variables for integration tests as outlined
  #       above are already exported in your environment.
  # build + install integration test core
  $ (cd testing/wd-gcp/ && mvn clean install)
  ```
  ```bash
  # build + run GCP integration tests.
  $ (cd testing/wd-gcp/ && mvn clean test)
  ```
 
 ## Deployment
 Storage Service is compatible with App Engine Flexible Environment and Cloud Run.
 
 * To deploy into Cloud run, please, use this documentation:
 https://cloud.google.com/run/docs/quickstarts/build-and-deploy
 
 * To deploy into App Engine, please, use this documentation:
 https://cloud.google.com/appengine/docs/flexible/java/quickstart
 
 ## License
 Copyright © Google LLC
 
 Copyright © EPAM Systems
  
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
  
 [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)
  
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.