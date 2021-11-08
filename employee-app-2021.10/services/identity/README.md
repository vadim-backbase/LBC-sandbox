# Backbase Identity local setup

## Running Backbase Identity

Currently Backbase Identity is only provided as a docker solution, this project will create an image locally which can be started using the provided docker-compose.yml
### Prerequesites

Backbase Identity requires a MySQL database, the platform project provides this via their provided docker-compose.yml file.

### Installation

Navigate to the identity folder and then execute the following steps in order:

* To create the required databases or delete any existing data in the ```backbase_identity```, ```fido```, ```device_management``` and ```confirmation``` databases.  (Note this step is only required if you do not have the schema and user already):
```bash
mvn clean install -Pclean-database
```

* To start Backbase Identity:
```bash
docker-compose up -d
```

* To run the Identity integration service, token converter, Fido service & Device Management service:

```bash
mvn blade:run
```


### Usage

For convenience this solution comes  with a Backbase realm containing 3 users, each with a preconfigured cx role related to their name: 

| username  | role  |
|---        |---|
|admin      |ROLE_group_admin(ADMIN)|
|manager    |ROLE_group_Manager(MANAGER)|
|user       |ROLE_group_user(USER)|

There are also two custom clients existing in the backbase realm:

```bb-tooling-client``` is intended for use with backbase tooling that requires authentication with identity. \
```bb-web-client``` is intended for cxp manager authentication.  You can configure additional experiences to authenticate against this realm by following the instructions [here](https://community.backbase.com/documentation/identity/latest/create_experiences_for_identity#create_new_experiences).  Note the cx and statics pom configuration is already applied in this project. \
```mobile-client``` is intended for use with MFA utilising Fido.

When the docker image has finished starting, you will be able to access the [admin console](http://localhost:8180/auth).


#### Native MySQL

* Install MySQL 5.7.x or higher
* Configure MySQL according to settings stored in `platform/mysql/config/my.cnf`
