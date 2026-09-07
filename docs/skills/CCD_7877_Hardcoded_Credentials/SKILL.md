# CCD-7877 Hardcoded Credentials

## Objective

Ensure Next Hearing Date Updater credentials are supplied by runtime secret injection rather than committed defaults.

## Acceptance criteria

- IDAM client secret, system-user credentials, and S2S secret have no committed credential defaults.
- Existing environment variable names, Jenkins Azure Key Vault mappings, and Helm secret aliases remain unchanged.
- No live secret rotation is performed or implied.

## Findings and changes

- Removed the committed defaults from `src/main/resources/application.yaml` for `CCD_NEXT_HEARING_DATE_UPDATER_SERVICE_IDAM_CLIENT_SECRET`, `IDAM_NEXT_HEARING_DATE_SYSTEM_USER`, `IDAM_NEXT_HEARING_DATE_SYSTEM_PASSWORD`, and `IDAM_KEY_NEXT_HEARING_UPDATER`.
- The repository already maps these values through `Jenkinsfile_nightly` and the chart’s secret references.
- The tracked `.env` contains URLs and case-type configuration only; no credential value was identified in it.
- Preview PostgreSQL values now reference the chart’s existing `global.postgresql.auth` values rather than committed credentials.

## Local validation

Set the four variables above before starting the service or running tests that request IDAM/S2S tokens. For BEFTA functional tests, run `./bin/setup-local-befta-env.sh` with approved local values for those four variables plus `DEFINITION_IMPORTER_PASSWORD` and `CCD_BEFTA_MASTER_CASEWORKER_PWD`, then run the existing Gradle checks.

## Recommendations

Confirm the corresponding secret-store values are active in each deployment, then rotate any historical values and verify running workloads and rotation records.
