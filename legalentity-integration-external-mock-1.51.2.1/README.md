# Legal Entity Integration External Mock
Service mocking an outbound integration to legal entity. 

Build and start mock locally as spring process 
```
export SIG_SECRET_KEY=JWTSecretKeyDontUseInProduction!
mvn clean spring-boot:run
```

Build mock image locally so it could be started by docker-compose
```
mvn clean package jib:dockerBuild
```

