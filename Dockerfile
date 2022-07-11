ARG APP_INSIGHTS_AGENT_VERSION=3.2.10
FROM hmctspublic.azurecr.io/base/java:17-distroless

COPY build/libs/ccd-next-hearing-date-updater.jar /opt/app/

CMD [ "ccd-next-hearing-date-updater.jar" ]
