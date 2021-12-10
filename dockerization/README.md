# Setup
```
export BB_VERSION=2021.07
docker-compose up -d
docker-compose down
docker-compose restart
docker-compose ps
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

# References
https://hub.docker.com/r/rmohr/activemq/
https://github.com/Backbase/docker-compose-examples


