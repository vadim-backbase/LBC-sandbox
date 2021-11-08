# DBS applications

DBS applications introduces Backbase Digital Banking Services (DBS), a set of custom banking capabilities that you use
to enrich and expand your existing system

- Download all applications inside _${capability}/target_ folder

		 mvn package


- Create PCF MySql service for each application that requires it

		cf create-service <service> <plan> <service name>
		
	Example:
	
		cf create-service p-mysql 512mb contacts-db
		cf create-service p-mysql 512mb productsummary-db
		cf create-service p-mysql 512mb payments-db
		cf create-service p-mysql 512mb audit-db
		cf create-service p-mysql 512mb transactions-db
		cf create-service p-mysql 512mb messagecenter-db
		cf create-service p-mysql 512mb notifications-db
		cf create-service p-mysql 512mb entitlements-db
		cf create-service p-mysql 512mb actions-db
		cf create-service p-mysql 512mb limits-db

    > Note: _<service>_ and _<plan>_ depends on PCF configuration.


- Create tables in the databases

How to download SQL scripts via maven is explained here:

https://community.backbase.com/documentation/DBS/latest/deploy_tomcat_mysql

And to connect to the PCF database services we just created we can follow the official documentation:

https://docs.pivotal.io/p-mysql/2-4/use.html#dev-tools


- Deploy DBS applications to the PCF

		                   
	- Push the applications and start them
		                                     
		    cf push -f contact-manager/contacts-manifest.yml
		    cf push -f audit/audit-manifest.yml
            ...

       > To push individual application: `cf push -f manifest.yml <application name>`
		

- Allow container to container network communication between Gateway application and other applications (presentation services only):

	   	cf add-network-policy <app from> --destination-app <app to>
		
    Example:

		cf add-network-policy gateway --destination-app accessgroup-presentation-service
		cf add-network-policy gateway --destination-app account-presentation-service
		cf add-network-policy gateway --destination-app actionrecipes-presentation-service
		cf add-network-policy gateway --destination-app audit-presentation-service
		cf add-network-policy gateway --destination-app contact-presentation-service
		cf add-network-policy gateway --destination-app legalentity-presentation-service
		cf add-network-policy gateway --destination-app limits--presentation-service
		cf add-network-policy gateway --destination-app messages-presentation-service
		cf add-network-policy gateway  --destination-app payment-order-presentation-service
		cf add-network-policy gateway  --destination-app product-summary-presentation-service
		cf add-network-policy gateway  --destination-app transaction-presentation-service
		cf add-network-policy gateway  --destination-app user--presentation-service

- Verify that deployed applications are healthy with `cf apps`, which will list all deployed applications and their statuses.
