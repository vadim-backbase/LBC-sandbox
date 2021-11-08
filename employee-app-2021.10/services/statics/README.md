# Statics project

The statics project hold all the references to Backbase Widget Collections and your own project files. 

The `bb-maven-plugin` is configured to connect to your local development installation.


The bb-maven-plugin can be used for the following:

* Build collections using the binary npm package @bb-cli/bb

* Provision a Collection

* Packaging to Zip of Zips, External Packages and Sources

* Import & Export of Experiences


More information can be found on our [Community](https://community.backbase.com/documentation/cxs/latest/deploy_using_bb_maven)

## Basic Usage

To provision all your widget collection and custom projects listed in the statics `pom.xml`

### CX 6 without Content Approvals
```bash
mvn package bb:provision
mvn bb:import-experience
```
Importing page (link) artifacts. Pages need a destination which is the portal to import into. Before running this command, make sure the portal is already provisioned.
```bash
mvn bb:import-packages
```
### CX 6 with Content Approvals (compatible with Gateway 1.11+)
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

### CXP 5.x.x

#### Login into cxp-manager with admin/admin:
Example login URL:
```
http://localhost:7777/portalserver/login/login.jsp
```

#### Import backbase portal object:
Example import URL:
```
http://localhost:7777/portalserver/import
```
```Check all and click IMPORT button.```

Change host and port according to your need.

Then run following command:

```bash
mvn package bb:import 
```

```bash
mvn bb:import-experiences
```

## Set target experience services

You can configure remote profiles by adding / overwriting the host configuration properties in the statics `pom.xml` file:

```xml
    ...
    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>com.backbase.maven.plugins</groupId>
                    <artifactId>bb-maven-plugin</artifactId>
                    <version>1.0.47</version>
                    <extensions>true</extensions>
                    <configuration>
                        <version>${portal.version}</version>
                        <host>${portal.host}</host>
                        <port>${portal.port}</port>
                        <username>${portal.username}</username>
                        <password>${portal.password}</password>
                        <!-- You can upgrade bb-cli version independently -->
                        <bbCliVersion>2.7.15</bbCliVersion>
                        <!-- Or choose to use your globally available bb-cli -->
                        <useGlobalBBCli>false</useGlobalBBCli>
                    </configuration>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>

    <profiles>
        <profile>
            <id>local</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <portal.version>6</portal.version>
                <portal.host>localhost</portal.host>
                <portal.port>8080</portal.port>
                <portal.username>admin</portal.username>
                <portal.password>admin</portal.password>
            </properties>
        </profile>

        <profile>
            <id>remote</id>
            <properties>
                <portal.host>remote</portal.host>
                <portal.port>8080</portal.port>
            </properties>
        </profile>
    </profiles>
    ...
```

You can also overwrite these using the command line parameters

```bash
mvn bb:provision -Dportal.host=<portalHost> -Dportal.port=80
```

## Widget Collection 3 Support
See more on https://community.backbase.com/documentation/Retail-Banking/latest/setup_widget_development_environment
