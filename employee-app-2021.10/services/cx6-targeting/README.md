# Local Development Setup CX6

To run CX6 you will need at minimum the following software and hardware requirements:

* Java JDK 8
* Maven 3.5
* Intel Core i5 or higher
* 16 GB RAM (32 GB preferred)
* Access to repo.backbase.com
* Maven Settings File Setup with Backbase Repositories

## Setup Infrastructure Requirements

CX6 requires MySQL and ActiveMQ to be running as services. For your convenience, the platform project also comes 
with a docker-compose that starts these services and binds them to localhost. 

Note: Running Content Approvals (Space Controller) is necessary to configure a Reverse Proxy in order to have spaces draft and final.

Only Docker Compose version 3.2 and up are supported. More information regarding docker-compose versions can be found 
[here](https://docs.docker.com/compose/compose-file/compose-versioning/#compatibility-matrix)

This project template is tested with:

* [Docker for Mac](https://www.docker.com/docker-mac)
* [Docker for Windows](https://www.docker.com/docker-windows)

If your environment does not support Docker for Mac/Windows, then you must use the native MySQL and ActiveMQ instructions.

In case of exporting an experience with a significant amount of shared content, you may need to increase the assigned memory for the Content Service instance.

### Docker Option:

## Docker Compose for MySQL, ActiveMQ and Reverse Proxy (NGINX)
Open a new terminal, go to your project folder, navigate to the `platform` folder, where the docker-compose.yml file is
located and run `docker-compose up -d` to start MySQL, Active MQ and NGINX. Schemas are automatically created the first time
it starts.
Don't close this terminal.

### Native Option:

#### Native MySQL

* Install MySQL 5.7.x or higher
* Configure MySQL according to settings stored in `mysql/config/my.cnf`
* Execute script on: `mysql/scripts/drop_create_cx_schemas.sql`

#### Native ActiveMQ

Install [Active MQ 5.14.x](http://activemq.apache.org/activemq-5145-release.html)

## Start Blade

Make sure that you have access to repo.backbase.com


## Create databases

To create the needed databases and tables, go to `cx6-targeting/` folder and execute:

```bash
mvn clean install -Pclean-database
```

## Start Platform Services

Refer to [platform documentation](../platform/README.md) for starting up Platform Services.

When all platform services are started, you can start the Customer Experience Services. 

## Start Customer Experience Services 

Open a new terminal, go to your project folder, navigate to the cx6 folder and run:

```bash
mvn blade:run
```

Wait until all services in Infra, Portal and Editorial are started on http://localhost:8080 and continue to the next step:
Once Portal and Editorial Services are started continue to the next step:

NOTE: Mobile services are not stared by default as they are marked as optional.

## Provision Statics without Content Approvals

Inside the statics object navigate to `statics`

```bash
mvn bb:provision
```

This will provision all widget collections referenced Maven pom files.


## Import Experience Packages

To import all experience packages configured in the pom files execute in `statics`

```bash
mvn bb:import-experiences
```

To import an experience one by one, navigate to the subdirectory and run `mvn bb:import-experiences` to only import experience packaged configured in the 'bb-maven-plugin'

### Import Experience Manager

Navigate to `statics/collection-experience-manager` and execute:

```bash
mvn bb:import-experiences
```

## Import Packages

Importing page (link) artifacts.
Pages need a destination which is the portal to import into. Before running this command, make sure the portal is already provisioned.

Navigate to `statics/` and execute:

```bash
mvn bb:import-packages
```

### Provision Statics with Content Approvals (compatible with Gateway 1.11+)
In order to set up `Content Approvals`, you need to provision packages and experience on draft by running following commands:

```bash
mvn bb:provision -Pspace-draft
mvn bb:import-experience -Pspace-draft
```

Also, you need provision packages on final environment by running following command:

```bash
mvn bb:provision -Pspace-final
```
In case you need to import some experiences (except Experience Manager) on final, execute the following command:

```bash
mvn bb:import-experience -Pspace-final
```

#### Accessing Experience Manager and Experiences
Using Content Approvals (Space Controller), the Experience Manager must be accessible through Reverse Proxy:

Using Edge 2 (IPS 1.12+)
```
http://localhost:8085/cxp-manager
```
Using Gateway (IPS 1.11)
```
http://localhost:8085/gateway/cxp-manager
```

Access the Experience on final, a different port (see Reverse Proxy configuration):

Using Edge 2 (IPS 1.12+)
```
http://localhost:9085/your-experience-name
```
Using Gateway (IPS 1.11)
```
http://localhost:9085/gateway/your-experience-name
```

## Demo Data

The built in LDAP Server is initiated using an LDIF file. This file is located in:

`platform/config/backbase/authentication-ldap/users.ldif`

This file contains all users and groups available for you to test your local development environment with

- Admin Users
-- admin / admin
-- admin2 / admin
-- new-admin / admin

To enable mock data on the demo portals append `?enable-mocks` to the demo pages. 

For instance: `http://localhost:8080/gateway/retail-banking-demo/index?enable-mocks`



## Reset Database
If you like to reset the database when using docker-compose, you need to shutdown MySQL and ActiveMQ and remove the volume. 

Do do this make sure blade is not running and execute in CX 6

```bash
docker-compose down -v
```

The `-v` option removes the attached MySQL storage volume allowing you to restart



    






