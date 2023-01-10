# ccd-next-hearing-date-updater

[![Build Status](https://travis-ci.org/hmcts/ccd-next-hearing-date-updater.svg?branch=master)](https://travis-ci.org/hmcts/ccd-next-hearing-date-updater)
[![Template CI / build Status](https://github.com/hmcts/ccd-next-hearing-date-updater/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/hmcts/ccd-next-hearing-date-updater/actions/workflows/ci.yml?query=branch:master)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=uk.gov.hmcts.reform%3Accd-next-hearing-date-updater&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=uk.gov.hmcts.reform%3Accd-next-hearing-date-updater)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=uk.gov.hmcts.reform%3Accd-next-hearing-date-updater&metric=coverage)](https://sonarcloud.io/summary/overall?id=uk.gov.hmcts.reform%3Accd-next-hearing-date-updater)

## Purpose

This microservice implements `The Next Hearing Date` feature outlined in [this wiki](https://tools.hmcts.net/confluence/display/RCCD/Maintain+Next+Hearing+Date+Scope+of+Work).

## Getting Started

### Prerequisites
- [JDK 17](https://openjdk.org/projects/jdk/17/)
- [Docker](https://www.docker.com)

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains `./gradlew` wrapper script, so
there's no need to install gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

### Running the application

The *Next Hearing Date Updater* can be run in one of two ways:

1. *via a CSV file*: against a file containing a list of Case References to process, or
2. *via Elasticsearch*: using a list of Case Types to search for cases that have an out-of-date *Next Hearing Date*
   value and then to process the results from these searches.

#### Running in local terminal

Example 1: Running against a local file (`test.csv`) containing a list of Case References to process:

```bash
./gradlew bootRun --args="--FILE_LOCATION=./test.csv"
```

Example 2: Running against a local file (`test.csv`) and overriding the default maximum CSV file size with the value
`3`, i.e. to test with a value less than the default 10,000:

```bash
./gradlew bootRun --args="--FILE_LOCATION=./test.csv --MAX_CSV_RECORDS=3"
```

Example 3: Run using a search query against case types `FT_NextHearingDate` & `FT_NextHearingDate_Clear`:

```bash
./gradlew bootRun --args="--CASE_TYPES=FT_NextHearingDate,FT_NextHearingDate_Clear"
```

Example 4: Run using a search query against case type `FT_NextHearingDate` and enabling Elasticsearch pagination after
3 records, i.e. to test with a value less than the default 100:

```bash
./gradlew bootRun --args="--CASE_TYPES=FT_NextHearingDate --ES_QUERY_SIZE=3"
```

> :information_source: Note: For information on the test case types available for manual testing see
> [Test Case Types](#test-case-types).

#### Running in local docker

When running in local docker the environment variables passed to the container will include those overridden by the
[.env](.env) file; as these values will need to be different to the standard ones defined in
[CCD-Docker](https://github.com/hmcts/ccd-docker).

> :information_source: Note: Although there are two different ways the *Next Hearing Date Updater* can be run (see
> [Running the application](#running-the-application)) when it is inside a container it is really only the 'via
> Elasticsearch' process that is available.
>
> The default configuration inside the [.env](.env) file will run an elasticsearch query against the
> [Test Case Types](#test-case-types): `FT_NextHearingDate` & `FT_NextHearingDate_Clear`.  If you wish to use a
> different set of case types: then override the following environment variable prior to executing the docker command
> or script:
>
>  ```bash
> export $CASE_TYPES="FT_NextHearingDate,FT_NHD_Bad_StartEvent"
> ```

##### Using docker-compose

Create the image of the application by executing the following command:

```bash
  ./gradlew assemble
```

Create docker image:

```bash
  docker-compose build
```

Run the distribution (created in `build/install/ccd-next-hearing-date-updater` directory) by executing the following
command:

```bash
  docker-compose up
```

This will start the *Next Hearing Date Updater* container.

##### Alternative script to run application through docker

To skip all the setting up and building, just execute the following command:

```bash
./bin/run-in-docker.sh
```

For more information:

```bash
./bin/run-in-docker.sh -h
```

##### Cleaning up docker images

Script includes bare minimum environment variables necessary to start the instance. Whenever any variable is changed or
any other script regarding docker image/container build, the suggested way to ensure all is cleaned up properly is by
this command:

```bash
docker-compose rm
```

It clears stopped containers correctly. Might consider removing clutter of images too, especially the ones fiddled with:

```bash
docker images

docker image rm <image-id>
```

There is no need to remove postgres and java or similar core images.


### Environment variables

To see all environment variables and how they map to functionality see [application.yaml](./src/main/resources/application.yaml).

#### Process flow environment variables

The following environment variables control the process:

| Name          | Description                                                                                                         | Example <span style="font-weight:normal">(for local testing)</span> |
|---------------|---------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------|
| CASE_TYPES    | (optional) CSV list of case type IDs to use when searching for cases with an out-of-date *Next Hearing Date* value. | `FT_NextHearingDate,FT_NextHearingDate_Clear`                       |
| FILE_LOCATION | (optional) path to file that contains list of Case References to process.                                           | `./test.csv`                                                        |

> :information_source: Note: The *Next Hearing Date Updater* is expected to be run using only one of the above
> configurations, i.e. either against a list of Case References to process or a list of Case Types to search against
> and then to process the results from these searches.

#### Key vault environment variables

The following environment variables would normally be loaded from the key vaults.  Therefor, if running against an
external environment then these would need to be set to match the corresponding environment.

| Name                                                     | Description                                                                  |
|----------------------------------------------------------|------------------------------------------------------------------------------|
| CCD_NEXT_HEARING_DATE_UPDATER_SERVICE_IDAM_CLIENT_SECRET | IDAM Client secret used when authenticating service account.                 |
| IDAM_KEY_NEXT_HEARING_UPDATER                            | S2S secret used to authenticate via S2S API service (service-auth-provider). |
| IDAM_NEXT_HEARING_DATE_SYSTEM_USER                       | Username for the *Next Hearing Date Updater*'s service account               |
| IDAM_NEXT_HEARING_DATE_SYSTEM_PASSWORD                   | Password for the *Next Hearing Date Updater*'s service account               |

> :information_source: Note: To see the correct key vault mappings for these variables see the
> [default chart values file](./charts/ccd-next-hearing-date-updater/values.yaml).

#### Downstream API environment variables

The following environment variables control the downstream API calls:

| Name                        | Description                                                  | Example <span style="font-weight:normal">(when: [running in local terminal](#running-in-local-terminal) or [running in local docker](#running-in-local-docker))</span> |
|-----------------------------|--------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| IDAM_API_URL                | Base URL for IdAM's User API service (idam-app).             | `http://localhost:5000` or `http://host.docker.internal:5000`                                                                                                          |
| IDAM_S2S_URL                | Base URL for IdAM's S2S API service (service-auth-provider). | `http://localhost:4502` or `http://host.docker.internal:4502`                                                                                                          |
| CCD_DATA_STORE_API_BASE_URL | Base URL for CCD Data-Store APIs.                            | `http://localhost:4452` or `http://host.docker.internal:4452`                                                                                                          |

> :information_source: Note: When running against a local CCD environment; provided the above environment variables are
> `unset`, they should default to the correct working value regardless of whether the *Next Hearing Date Updater* is
> [running in local terminal](#running-in-local-terminal) or [running in local docker](#running-in-local-docker).
> However, if running against an external environment then these would need to be set to match the corresponding
> environment, see the [default chart values file](./charts/ccd-next-hearing-date-updater/values.yaml) for guidance on
> URL formats.

#### Additional environment variables useful for testing

The following additional environment variables can be overridden to force a particular manual test scenario:

| Name            | Description                                                                                                                                                                      | Default |
|-----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------:|
| MAX_CSV_RECORDS | Maximum number of CSV entries to process. The process will generate an error and exit if the specified CSV file's contents (see `FILE_LOCATION`) exceeds this number of records. | `10000` |
| ES_QUERY_SIZE   | Elasticsearch query size, before pagination is used.                                                                                                                             |   `100` |


## Developing

### Test Case Types

The following CCD test case types have been added to the `BEFTA_MASTER` jurisdiction configuration in the
[CCD Test Definitions](https://github.com/hmcts/ccd-test-definitions):

| Name                     | Description                                                                                                                    |
|--------------------------|--------------------------------------------------------------------------------------------------------------------------------|
| FT_NextHearingDate       | :white_check_mark: Success test: the `UpdateNextHearingInfo` event will successfully update the Hearing Data to a future date. |
| FT_NextHearingDate_Clear | :white_check_mark: Success test: the `UpdateNextHearingInfo` event will successfully clear the Hearing Data.                   |
| FT_NHD_Bad_DatePast      | :x: Negative test: the `UpdateNextHearingInfo` event will attempt to set a bad hearing date value, i.e. in the Past.           |
| FT_NHD_Bad_DateNull      | :x: Negative test: the `UpdateNextHearingInfo` event will attempt to clear just the Hearing Date value which is not permitted. |
| FT_NHD_Bad_IDNull        | :x: Negative test: the `UpdateNextHearingInfo` event will attempt to clear just the Hearing ID value which is not permitted.   |
| FT_NHD_Bad_StartEvent    | :x: Negative test: the `UpdateNextHearingInfo` event will generate a Start Event failure.                                      |
| FT_NHD_Bad_SubmitEvent   | :x: Negative test: the `UpdateNextHearingInfo` event will generate a Submit Event failure.                                     |

The above test definitions will be imported automatically when running [functional tests](#functional-tests) or
starting a local CCD environment using the provided [bootWithCcd](#bootwithccd).  However, they can also be imported
into a local CCD environment using the following command:

```bash
./gradlew localDataSetup
```

### Unit tests

To run all unit tests execute the following command:

```bash
./gradlew test
```

### Integration tests

To run all integration tests execute the following command:

```bash
./gradlew integration
```

### Functional tests

To run all functional tests execute the following command:

```bash
./gradlew functional
```

> :information_source: Note: The functional tests must be run against a CCD environment, see
> [Configuring a local CCD environment](#configuring-a-local-ccd-environment).

> :information_source: Note: The functional Gradle task will import the environment variables from the
> [.env.befta.local.env](./.env.befta.local.env) file prior to execution.  This will set all the variables needed to
> execute the [BEFTA Framework](https://github.com/hmcts/befta-fw) based tests.

### Configuring a local CCD environment

#### bootWithCcd

The easiest way to run a local CCD environment for the *Next Hearing Date Updater* to use is with the `bootWithCCD`
Gradle task by executing the following command:

```bash
./gradlew bootWithCCD
```

This will start the CCD Environment and its dependent services.

In order to test if the *CCD Data-Store* is up, you can call its health endpoint:

```bash
  curl http://localhost:4451/health | jq
```

You should get a response similar to this:

```json
{
  "status": "UP",
  "components": {
    "caseDocumentManagement": {
      "status": "UP"
    },
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 250685575168,
        "free": 35057991680,
        "threshold": 10485760,
        "exists": true
      }
    },
    "hazelcast": {
      "status": "UP",
      "details": {
        "name": "hazelcast-instance-ccd",
        "uuid": "9b800043-cf88-4ab9-b00c-3cebdff60919"
      }
    },
    "ping": {
      "status": "UP"
    },
    "refreshScope": {
      "status": "UP"
    },
    "serviceAuth": {
      "status": "UP"
    }
  },
  "groups": [
    "readiness"
  ]
}
```

> :information_source: Note: Should the docker containers fail to start, it is likely that the `bootWithCCD` plugin is
> not authorized to pull the container images from Azure.
>
> Log in, using the commands below:
>
> ```bash
>  az acr login --name hmctspublic --subscription DCD-CNP-DEV
>  az acr login --name hmctspublic --subscription DCD-CFT-Sandbox
> ```
>
> … or if [CCD-Docker](https://github.com/hmcts/ccd-docker) repository is already cloned locally, run the login command:
>
> ```bash
> ./ccd login
> ```

Once the `bootWithCcd` environment is running, test cases can be created manually using http://localhost:3000/ and the
[master caseworker](./src/cftlib/java/uk/gov/hmcts/reform/next/hearing/date/updater/CftLibConfig.java) account.  For
information on the test case types available see [Test Case Types](#test-case-types).

To stop the CCD elements of the `bootWithCcd` environment cleanly type `stop` at the active console for the process and
then press enter. However, this will leave the additional `cftlib` containers running. To stop them, use the following
command:

```bash
docker ps -q --filter name=cftlib-* | xargs docker stop
```

#### CCD-Docker

> :warning: Warning: If switching from an existing [bootWithCcd](#bootwithccd) instance: it is important that the
> following actions are completed:
>
> 1. Stop and / or delete the `cftlib` containers generated by [bootWithCcd](#bootwithccd); to avoid port number
     conflicts.
> 2. Delete the [.env.test.stub.service.env](./.env.test.stub.service.env) file if present; as the test-stubs URL
     override is no longer required.
> 3. (if testing manually) After starting the local [CCD-Docker](https://github.com/hmcts/ccd-docker)
     instance, re-import the [Test Case Types](#test-case-types) definitions; as the test-stubs/callback URLs used by
     the case events will have changed.

An alternative to using [bootWithCcd](#bootwithccd) is to use a local [CCD-Docker](https://github.com/hmcts/ccd-docker)
instance.   [CCD-Docker](https://github.com/hmcts/ccd-docker) has been updated to include the correct CCD configuration
to support the *Next Hearing Date Updater*.  However, the [functional tests](#functional-tests) require Elasticsearch,
which is not enable by the default [CCD-Docker](https://github.com/hmcts/ccd-docker) setup, therefore it should be
enabled along with logstash with this command:

```bash
./ccd enable elasticsearch logstash
```

The next step is to get both `ccd-definition-store-api` and `ccd-data-store-api` to use Elasticsearch and this is done
by exporting the following environment variables:
```bash
export ES_ENABLED_DOCKER=true
export ELASTIC_SEARCH_ENABLED=$ES_ENABLED_DOCKER
```

Indices of the relevant case types are expected to be present in the Elasticsearch instance for the tests to work.
These are generated each time a case type definition is published.  So this will happen automatically when running the
[functional tests](#functional-tests). However, if running manual tests against a fresh
[CCD-Docker](https://github.com/hmcts/ccd-docker) environment then run the `localDataSetup` command shown in
[Test Case Types](#test-case-types).

If the frontend is enabled in this CCD Docker environment (`./ccd enable xui-frontend frontend`), test cases can be
created manually using http://localhost:3455/ and the
[master caseworker](https://github.com/hmcts/ccd-docker/blob/master/bin/users.json) account.  For information on the
test case types available see [Test Case Types](#test-case-types).

### Code quality checks

This repository uses [checkstyle](http://checkstyle.sourceforge.net/) and [PMD](https://pmd.github.io/).

To run all checks execute the following command:

```bash
./gradlew clean checkstyleMain checkstyleTest checkstyleIntegrationTest pmdMain pmdTest pmdIntegrationTest
```

To run all checks alongside the unit tests execute the following command:

```bash
./gradlew check
```

or to run all checks, all tests and generate a code coverage report execute the following command: *although functional
tests do not form part of code coverage.*

```bash
./gradlew check functional jacocoTestReport
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
