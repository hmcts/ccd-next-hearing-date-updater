# ccd-next-hearing-date-updater

[![Build Status](https://travis-ci.org/hmcts/ccd-next-hearing-date-updater.svg?branch=master)](https://travis-ci.org/hmcts/ccd-next-hearing-date-updater)

## Purpose

This micro-service implements `The Next Hearing Date` feature outlined in [this wiki](https://tools.hmcts.net/confluence/display/RCCD/Maintain+Next+Hearing+Date+Scope+of+Work)

## Building and deploying the application

### Prerequisites
- [JDK 17](https://openjdk.org/projects/jdk/17/)

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

### Running the application

Create the image of the application by executing the following command:

```bash
  ./gradlew assemble
```

Create docker image:

```bash
  docker-compose build
```

Run the distribution (created in `build/install/ccd-next-hearing-date-updater` directory)
by executing the following command:

```bash
  docker-compose up
```

This will start the API container

### Alternative script to run application

To skip all the setting up and building, just execute the following command:

```bash
./bin/run-in-docker.sh
```

For more information:

```bash
./bin/run-in-docker.sh -h
```

Script includes bare minimum environment variables necessary to start api instance. Whenever any variable is changed or any other script regarding docker image/container build, the suggested way to ensure all is cleaned up properly is by this command:

```bash
docker-compose rm
```

It clears stopped containers correctly. Might consider removing clutter of images too, especially the ones fiddled with:

```bash
docker images

docker image rm <image-id>
```

There is no need to remove postgres and java or similar core images.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

