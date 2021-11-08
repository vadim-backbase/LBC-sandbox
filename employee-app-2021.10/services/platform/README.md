# Backbase 6 Platform

The Backbase Platform services are required to run DBS and CX on your local machine and is composed of the following services:

- Eureka Service Registry
- Edge Service Router
- Authentication Service
- BB Authentication Token Converter Service


## Docker Compose for MySQL, ActiveMQ and Reverse Proxy (NGINX)

Run `docker-compose up -d` to start MySQL, Active MQ and NGINX.


## Running Backbase 6 Platform Services

```bash
mvn blade:run
```

## Running Edge Service

You may deploy the Edge Service by executing the following:
```bash
mvn -pl edge package -Prun-edge
```

Alternatively, depending on the operating system, you may run either
```bash
bash run_edge.sh
```
or
```bash
run_edge.cmd
```


### Native MySQL

* Install MySQL 5.7.x or higher
* Configure MySQL according to settings stored in `mysql/config/my.cnf`


### Native ActiveMQ

Install [Active MQ 5.14.x](http://activemq.apache.org/activemq-5145-release.html)