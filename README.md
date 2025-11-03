# Smartsense Dummy Clearing House - Backend Application

This repository contains a small Spring Boot-based dummy implementation of a clearing house used in CX data-space. It implements two lightweight HTTP endpoints that simulate portal callbacks and self-description registration for external systems.

---

**Key points**
- Minimal Spring Boot application (Java 17).
- Provides two endpoints used by external portals to post validation and compliance events.
- Uses a RestTemplate to call external portal endpoints and a Keycloak token endpoint to obtain a client_credentials access token for outbound requests.
- Includes a Dockerfile and a Helm chart (charts/clearing-house) for containerized deployment.

---

**What this project is NOT**
- This is a dummy/stub service intended for integration/testing. It does not store data in a database.

---

**External services this application interacts with**
- **Keycloak** (or any OAuth2 token endpoint) to obtain client credentials access token.
  - Configured via: **app.portal.client.tokenUrl** (env: **AUTH_SERVER_TOKEN_URL**)
- **Portal registration endpoints** (to post self-description responses):
  - **app.portal.reg.selfDescriptionUrl** (env: **PORTAL_REGI_SD_URL**)
  - **app.portal.connector.selfDescriptionUrl** (env: **PORTAL_CONNECTOR_SD_URL**)

---

**Configuration / Environment variables**

| Environment Variable | Application Property | Description | Default |
|---|---|---|---|
| SERVER_PORT | server.port | Spring Boot server port | 8082 (from src/main/resources/application.yaml) |
| PORTAL_REGI_SD_URL | app.portal.reg.selfDescriptionUrl | URL to POST self-description responses for registration events | https://localhost:8089/api/administration/registration/clearinghouse/selfDescription |
| PORTAL_CONNECTOR_SD_URL | app.portal.connector.selfDescriptionUrl | URL to POST self-description responses for connector/service-offering events | https://localhost:8089/api/administration/Connectors/clearinghouse/selfDescription |
| KEYCLOAK_CLIENT_ID | app.portal.client.id | OAuth2 client id used to obtain access token | sa-cl2-01 |
| KEYCLOAK_CLIENT_SECRET | app.portal.client.secret | OAuth2 client secret used to obtain access token | (no default) |
| AUTH_SERVER_TOKEN_URL | app.portal.client.tokenUrl | Token endpoint to obtain client_credentials token | https://localhost:8000/auth/realms/CX-Central/protocol/openid-connect/token |
| RETRY_COUNT | app.retryCount | Number of retries when outbound POSTs return errors | 5 |

---

## API Endpoints

### 1) **POST /api/v1/validation**
**Purpose:** Simulate a validation callback. The controller accepts a JSON payload describing the participant and a callback URL. After a short delay the service posts a confirmation message to the provided callbackUrl using a Bearer token.

**Incoming request**
- Method: POST
- Path: /api/v1/validation
- Headers: Content-Type: application/json
- Body (JSON):

```json
{
  "callbackUrl": "https://portal.example.com/api/validate/callback",
  "participantDetails": {
    "bpn": "BPN123456"
  }
}
```

**Outbound callback (POST to callbackUrl)**
- Method: POST
- Headers: Content-Type: application/json, Authorization: Bearer <access_token>
- Body (JSON):

```json
{
  "bpn": "BPN123456",
  "status": "CONFIRM",
  "message": "SUccess"
}
```

**Response from this endpoint**
- Status: 200 OK (empty body)
- Note: the controller triggers the callback asynchronously; the immediate HTTP response is an acknowledgement only.

---

### 2) **POST /api/v1/compliance?externalId=<id>**
**Purpose:** Simulate a portal compliance/self-description callback. Accepts an externalId query parameter and a JSON payload describing the credentialSubject. The service posts a self-description confirmation to either the connector self-description endpoint or the registration self-description endpoint depending on the credentialSubject.type.

**Incoming request**
- Method: POST
- Path: /api/v1/compliance?externalId=<id>
- Headers: Content-Type: application/json
- Body (JSON):

```json
{
  "credentialSubject": {
    "type": "ServiceOffering"
  },
  "other": "..."
}
```

**Outbound callback (POST to portal registration/connector endpoint)**
- Chosen URL:
  - If credentialSubject.type == "ServiceOffering" -> **app.portal.connector.selfDescriptionUrl**
  - Otherwise -> **app.portal.reg.selfDescriptionUrl**
- Method: POST
- Headers: Content-Type: application/json, Authorization: Bearer <access_token>
- Body (JSON):

```json
{
  "externalId": "<externalId>",
  "statusMessage": "DUMMT APPROVAL",
  "description": "{ \"test\": true }",
  "status": "Confirm"
}
```

**Response from this endpoint**
- Status: 200 OK (empty body)
- Note: the controller triggers the outbound self-description callback asynchronously; the immediate HTTP response is an acknowledgement only.

---


## Building and running

**Prerequisites**
- Java 17 (JDK 17)
- Gradle (wrapper included)
- Docker and Helm if deploying containerized

**Run locally from IDE**
- Import the project as a Gradle project in your IDE (IntelliJ, Eclipse).
- Run the main class **com.smartsense.dummy.ch.ClearingHouseApplication** or run `./gradlew bootRun`.
- The application reads defaults from `src/main/resources/application.yaml`. Override values with environment variables as needed.

**Build with Gradle**
- Build jar (uses included Gradle wrapper):

```
./gradlew clean build
```

- After a successful build the runnable jar is in `build/libs/`.

**Run with Docker**
- Build image (from project root):

```
docker build -t smartsense-ch:latest .
```

- Run container (example overriding server port):

```
docker run --rm -p 8082:8082 \
  -e KEYCLOAK_CLIENT_ID=sa-cl2-01 \
  -e KEYCLOAK_CLIENT_SECRET=secret \
  -e AUTH_SERVER_TOKEN_URL=https://keycloak.example.com/auth/realms/.../protocol/openid-connect/token \
  -e PORTAL_REGI_SD_URL=https://portal.example.com/api/administration/registration/clearinghouse/selfDescription \
  -e PORTAL_CONNECTOR_SD_URL=https://portal.example.com/api/administration/Connectors/clearinghouse/selfDescription \
  smartsense-ch:latest
```

- Note: the Dockerfile exposes port 8080, but the application default port is 8082. Use SERVER_PORT or `-e SERVER_PORT` to align container port and application port.

**Deploy with Helm (supplied chart)**
- A Helm chart is available under `charts/clearing-house`.
- Customize values in `charts/clearing-house/values-custom.yaml` or pass values via `--set` or `-f`.

```
helm install smartsense-ch charts/clearing-house -n my-namespace --create-namespace -f charts/clearing-house/values-custom.yaml
```

---

**Notes and known quirks**
- This is a dummy/stub service used to simulate callbacks and token flows during integration testing. It intentionally performs no persistent storage.
- The application sleeps (Thread.sleep) in controllers to simulate processing time before starting async callbacks.
