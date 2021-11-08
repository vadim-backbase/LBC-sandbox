# Local Development Setup for DBS

To run DBS you will need at minimum the following hardware


* Java JDK 8
* Intel Core i5 or higher
* 8 GB RAM (16 GB preferred)


## Setup Infrastructure Requirements

DBS requires MySQL and ActiveMQ to be running as services. For your convenience, the Backbase 6 Platform project also comes 
with a docker-compose that starts these services and binds them to localhost. 

Only Docker Compose version 3.2 and up are supported. More information regarding docker-compose versions can be found 
[here](https://docs.docker.com/compose/compose-file/compose-versioning/#compatibility-matrix)

### Docker Compose for MySQL and ActiveMQ

Inside `platform` run `docker-compose up -d` to start MySQL and Active MQ.

### Native MySQL

* Install MySQL 5.7.x or higher
* Configure MySQL according to settings stored in `platform/mysql/config/my.cnf`


### Native ActiveMQ

Install [Active MQ 5.14.x](http://activemq.apache.org/activemq-5145-release.html)

## Start a DBS Capability

Before starting any DBS Capabilities, ensure that Backbase 6 Platform Services, ActiveMQ and MySQL are all running. 

Backbase 6 Platform Services can be started by executing `mvn blade:run` inside the `platform` directory. 

NOTE: please be aware of dependencies between capabilities i.e. Payments needs Access Control and Product Summary configured in order to test it properly, please check:
https://my.backbase.com/docs/product-documentation/documentation//DBS/latest/dbs_reference.html


To create the needed databases and tables execute:

```bash
cd capability-folder-name
mvn clean install -Pclean-database
```

To run the services:

```bash
mvn blade:run
```
For example to run contact manager

```bash
cd contact-manager
mvn clean install -Pclean-database
mvn blade:run
```
