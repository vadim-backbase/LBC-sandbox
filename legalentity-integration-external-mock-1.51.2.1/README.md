# Legal Entity Integration External Mock
Service mocking an outbound integration to legal entity. 

Build and start steps with Maven:
```
export SIG_SECRET_KEY=JWTSecretKeyDontUseInProduction!
mvn clean spring-boot:run
```

Build steps only:
```
mvn clean package
docker build -t legalentity-integration-external-mock:local .
```
