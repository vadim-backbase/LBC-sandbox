# Development Authentication Service
The Development Authentication service provides authentication and authorization for downstream services **for development purposes**.

**Artifact**

	GroupId: com.backbase.infra
	ArtifactId: authentication-dev
	
## Service Configuration
	
For a full list of 1000 users provided by the service and their respective roles please have a look at the config/users.json file.
Here is small excerpt of the first 10.
                                                                                              
| Username     | Password     | Roles           | Group   |
|--------------|--------------|-----------------|---------|
| user         | user         | USER            | USER    |
| admin        | admin        | ADMIN, ACTUATOR | ADMIN   |
| manager      | manager      | USER            | MANAGER |
| rmeeron0     | rmeeron0     | USER            | USER    |
| scrampsey1   | scrampsey1   | USER, MANAGER   | MANAGER |
| jvischi2     | jvischi2     | USER            | USER    |
| xspurdle3    | xspurdle3    | ADMIN, ACTUATOR | ADMIN   |
| lredwood4    | lredwood4    | USER            | USER    |
| nleadston5   | nleadston5   | ADMIN, ACTUATOR | ADMIN   |
| tbrantl6     | tbrantl6     | USER, MANAGER   | MANAGER |

### Custom Test Users
**Classpath:**
```
authentication:
  users:
    resource: classpath:backbase_users.json
```
**File:**
```
authentication:
  users:
    resource: file:/path/to/your/file.json
```
**Url:**
```
authentication:
  users:
    # Load users from Mockaroo https://mockaroo.com/270be6b0
    resource: https://api.mockaroo.com/api/270be6b0?count=1000&key=${yourAPIKey}
```