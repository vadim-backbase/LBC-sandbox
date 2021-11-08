# Platform applications

Backbase 6 Platform application are set of core applications needed for Backbase 6 platform to work properly.

- Assign the routes to the service set the values where the placeholder is located.
        
        ${x-registry-url}       => Registry route
        ${x-edge-url}           => Edge route
        ${activemq-broker-url}  => ActiveMQ URL

    > **NOTE 1**: Should exist the ActiveMQ message broker reachable by authentication service deployed on PCF
  
    > **NOTE 2**: Recommend the routes to have the following structure "${env}-${service}-${namespace}.${domain}"  

- Download all applications inside _target_ folder

		 mvn package -Pdist

- Deploy platform applications to the PCF

		cf push -f manifest.yml
		
	> To push individual application: `cf push -f manifest.yml <application name>`

- Allow container to container network communication between Edge application and other applications:

		cf add-network-policy <app from> --destination-app <app to>
		
	Example:

		cf add-network-policy edge --destination-app auth
		
		cf add-network-policy edge --destination-app token-converter
		
		
- Verify that deployed applications are healthy with `cf apps`, which will list all deployed applications and their statuses.
