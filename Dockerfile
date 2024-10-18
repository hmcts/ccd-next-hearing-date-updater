ARG APP_INSIGHTS_AGENT_VERSION=3.5.4
FROM hmctspublic.azurecr.io/base/java:21-distroless

COPY build/libs/ccd-next-hearing-date-updater.jar /opt/app/

CMD [ "ccd-next-hearing-date-updater.jar" ]
