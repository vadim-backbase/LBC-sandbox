# Mock Financial Institution (Bank)

* Exposes endpoint for getting balances [GET /rest/balance/{arrangementId}]
* Currently the mock returns random booked and available balance for any arrangementId provided


To successfully build the project you need to copy one library file to your local 
maven repo. 

```
cp lib/openapi-bundle-1.4.7.zip ~/.m2/repository/com/backbase/products/openapi-bundle/1.4.7
```