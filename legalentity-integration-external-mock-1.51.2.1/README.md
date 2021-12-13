# Legal Entity Integration External Mock
Service mocking an outbound integration to legal entity. 

export SIG_SECRET_KEY=JWTSecretKeyDontUseInProduction!
mvn clean spring-boot:run

mvn clean package
docker build -t legalentity-integration-external-mock:local .

