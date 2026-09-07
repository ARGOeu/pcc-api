FROM eclipse-temurin:21-jdk
ARG KC_VERSION=26.5.5-1.1
ARG AGM_URL=https://github.com/rciam/keycloak-group-management/releases/download/2.0.13/keycloak-group-management.jar
ARG THEME_URL=https://github.com/rciam/keycloak-theme-vanilla/releases/download/6.1.2/keycloak-theme-vanilla.jar

WORKDIR /opt
RUN curl -L -o keycloak.tar.gz \
    https://github.com/eosc-kc/keycloak/releases/download/26.5.5-1.1/keycloak.tar.gz
RUN mkdir -p /opt/keycloak; \
    tar -xzvf keycloak.tar.gz -C /opt/keycloak --strip-components=1
RUN curl -L -o /opt/keycloak/providers/keycloak-group-management.jar $AGM_URL
RUN curl -L -o /opt/keycloak/providers/keycloak-theme-vanilla.jar $THEME_URL

RUN /opt/keycloak/bin/kc.sh build

WORKDIR /opt/keycloak
ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]