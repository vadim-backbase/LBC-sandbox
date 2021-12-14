# Setup
```
export BB_VERSION=2021.07
docker-compose -f <filename> up -d
docker-compose -f <filename> down
docker-compose -f <filename> restart
docker-compose -f <filename> ps
```

# Testing

* test active mq
http://localhost:8161/

* test registry
http://localhost:8761/

* test edge
http://localhost:8080/actuator/info (admin/admin)

* test auth via edge
http://localhost:8080/api/auth/login

* test identity
http://localhost:9090/auth/admin


* test mock
http://localhost:8186/client-api/v2/permissions/data-item?businessFunction=bf&privilege=pl&dataType=dt&dataItemId=di

# References
https://hub.docker.com/r/rmohr/activemq/
https://github.com/Backbase/docker-compose-examples
https://docs.docker.com/compose/compose-file/compose-file-v3/
https://docs.docker.com/engine/reference/commandline/build/