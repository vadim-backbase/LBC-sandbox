# Prerequisites

> Prerequisites for installing Backbase 6 to the Pivotal Cloud Foundry.

## Pivotal Cloud Foundry

- Pivotal Cloud Foundry version **>= 2.1** with enabled [container to container networking](https://docs.pivotal.io/pivotalcf/2-4/concepts/understand-cf-networking.html).
  - Organization unit, and dedicated space for Backbase applications.
  - approximately 1Gb per application container

- PCF Services
	- [MySql service](https://docs.pivotal.io/p-mysql/2-1/index.html)

> Use `cf marketplace` to verify that required services are available.


## ActiveMq

Backbase 6 uses ActiveMQ messaging broker for asynchronous events, in order to work properly [ActiveMq _5.13.3_](http://activemq.apache.org/activemq-5133-release.html) is required.

> Network visibility between PCF instance and machine with ActiveMQ should be in the place.

## Oracle JDK 8

Backbase 6 services are Java based services and all of them are tested on Oracle JDK.
In PCF world, Java buildpack needs to be used and configured to point to location of the [Oracle JDK](https://github.com/cloudfoundry/java-buildpack/blob/master/docs/jre-oracle_jre.md).


For more details:

 - [Administering Container-to-Container Networking](https://docs.pivotal.io/pivotalcf/1-12/devguide/deploy-apps/cf-networking.html)
 - [Creating and Managing Users with the UAA CLI (UAAC)](https://docs.pivotal.io/pivotalcf/1-12/uaa/uaa-user-management.html).

# CX applications

Backbase 6 CX applications introduces customer experience to the Backbase 6 platform.

- Download all applications inside _target/dist_ folder

		 mvn package -Pdist

- Create PCF MySql service for each application

		cf create-service <service> <plan> <service name>
		
	Example:
	
		cf create-service p.mysql db-small portal-db -c '{"workload": "write-heavy"}'
		cf create-service p.mysql db-small targeting-db -c '{"workload": "write-heavy"}'
		cf create-service p.mysql db-small auditing-db -c '{"workload": "write-heavy"}'
		cf create-service p.mysql db-small provisioning-db -c '{"workload": "write-heavy"}'
		cf create-service p.mysql db-small contentservices-db -c '{"workload": "write-heavy"}'
		cf create-service p.mysql db-small renditionservice-db	-c '{"workload": "write-heavy"}'
		cf create-service p.mysql db-small mobile-db -c '{"workload": "write-heavy"}'


    > Note: _<service>_ and _<plan>_ depends on PCF configuration.


- Create tables in the databases

SQL scripts are available to download here:

https://community.backbase.com/documentation/cxs/latest/downloads

And to connect to the PCF database services we just created we can follow the official documentation:

https://docs.pivotal.io/p-mysql/2-4/use.html#dev-tools


- Deploy CX applications to the PCF
            
		    cf push -f manifest.yml

    > To push individual application: `cf push -f manifest.yml <application name>`
		

- Allow container to container network communication between Gateway application and other applications:

	   	cf add-network-policy <app from> --destination-app <app to>
		
    Example:

		cf add-network-policy gateway --destination-app portal
		cf add-network-policy gateway --destination-app targeting
		cf add-network-policy gateway --destination-app auditing
		cf add-network-policy gateway --destination-app provisioning
		cf add-network-policy gateway --destination-app contentservices
		cf add-network-policy gateway --destination-app renditionservice
		cf add-network-policy gateway --destination-app thumbnailservice
		cf add-network-policy gateway --destination-app versionmanagement-persistence-service
		cf add-network-policy gateway --destination-app push-integration-service

- Verify that deployed applications are healthy with `cf apps`, which will list all deployed applications and their statuses.

- Allow container to container network communication between Portal and Contentservices

        cf add-network-policy portal --destination-app contentservices

- Allow container to container network communication between Provisioning and Portal

        cf add-network-policy provisioning --destination-app portal
        cf add-network-policy provisioning --destination-app contentservices


- Allow container to container network communication from Portal to Provisioning and from Contentservices to Provisioning
   
        cf add-network-policy portal --destination-app provisioning
        cf add-network-policy contentservices --destination-app provisioning

- Allow container to container network communication from Thumbnail to Content and Rendition Services (just editorial)

        cf add-network-policy thumbnailservice --destination-app contentservices
        cf add-network-policy thumbnailservice --destination-app renditionservice

- Allow container to container network communication from Content to Rendition Services (just editorial)

        cf add-network-policy contentservices --destination-app renditionservice

- Allow container to container network communication from Targeting to Contentservices
   
        cf add-network-policy targeting --destination-app contentservices

