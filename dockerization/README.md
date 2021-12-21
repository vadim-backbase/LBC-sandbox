# Setup
```
export BB_VERSION=2021.07
aws ecr get-login-password --region ca-central-1 --profile lbc | docker login --username AWS --password-stdin 197569887140.dkr.ecr.ca-central-1.amazonaws.com

docker-compose -f <filename> up -d
docker-compose -f <filename> down
docker-compose -f <filename> restart
docker-compose -f <filename> ps
```

# Database creation

export CD_DB_HOST=localhost
export CD_DB_PORT=3306
export CD_DB_USE_SSL=false
export CD_DB_USER_NAME=root
export CD_DB_PASSWORD=root
export CD_DB_PASSWORD=root

# Testing

* test active mq
http://localhost:8161/

* test registry
http://localhost:8761/

* test edge
http://localhost:8080/actuator/info (admin/admin)

* test bb-auth-dev
http://localhost:8080/api/auth/login

* test identity
http://localhost:9090/auth/admin

* test account mock
http://localhost:8187/actuator/health

* test legal-entity mock
http://localhost:8186/actuator/health

# References
https://hub.docker.com/r/rmohr/activemq/
https://github.com/Backbase/docker-compose-examples
https://docs.docker.com/compose/compose-file/compose-file-v3/
https://docs.docker.com/engine/reference/commandline/build/
https://community.backbase.com/documentation/ServiceSDK/latest/generate_jib_images
https://repo.backbase.com/ui/native/backbase-docker-releases/


