# Legal Entity Integration External Mock
Service mocking an outbound integration to legal entity. 

## Build and start mock locally as spring process 
```
export SIG_SECRET_KEY=JWTSecretKeyDontUseInProduction!
mvn clean spring-boot:run
```

## Build mock image locally 
```
mvn package -Pdocker-image,local-client
```

## Build mock image and push to AWS ECR

* Create ecr repo (Do it only once to create repo if it does not exits). Pay attention to the naming convention.
```
aws ecr create-repository --repository-name lbc/legalentity-integration-external-mock --profile lbc 
```
* Build image and push to ECR repo
```
mvn package -Pdocker-image -Djib.to.auth.username=AWS -Djib.to.auth.password=`aws ecr get-login-password --region ca-central-1 --profile lbc`
```
