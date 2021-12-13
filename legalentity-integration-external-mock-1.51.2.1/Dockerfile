# Build from backbase/base-tomcat
FROM base-tomcat:latest

# Copy war file to /tmp/
ADD target/legalentity-integration-external-mock-*.war /tmp/

# Create directory to extract war file inside webapps
RUN mkdir $CATALINA_HOME/webapps/ROOT && \
    unzip -q /tmp/legalentity-integration-external-mock-*.war -d $CATALINA_HOME/webapps/ROOT/ && \
    rm -rfv /tmp/*.war
