# Mock Financial Institution (Bank)

* Exposes endpoint for getting balances [GET /rest/balance/{arrangementId}]
* Currently the mock returns random booked and available balance for any arrangementId provided


## To successfully build the project you need to copy one library file to your local 
maven repo. 

```
cp lib/openapi-bundle-1.4.7.zip ~/.m2/repository/com/backbase/products/openapi-bundle/1.4.7
```

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
aws ecr create-repository --repository-name lbc/account-mock --profile lbc 
```
* Build image and push to ECR repo
```
mvn package -Pdocker-image -Djib.to.auth.username=AWS -Djib.to.auth.password=`aws ecr get-login-password --region ca-central-1 --profile lbc`
```
