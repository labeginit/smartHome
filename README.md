
666 ööopöokpklpömlök# Server & DB
## _Documentation_

[![N|Solid](https://sts.hkr.se/adfs/portal/logo/logo.sv.png?id=EEF44783CA63147AE553003A4940C9CC9EB367CC3B5D0CD3AF6D260338D971B5)](https://nodesource.com/products/nsolid)

Documentation of how to use the API. 

- What data is needed on a POST/GET request
- How the JSON data shall be formatted

> This can be updated at any time. If any errors or questions appear then please contact the Server/DB team.

## Dependencies | Maven

Three dependencies will be utilized. GSON is used for parsing JSON. MongoDB is used for connecting to the database. 
And Websocket is used for keeping track of clients connected and to broadcast messages. 

| Library | Version | Link |
| ------ | ------ | ------ | 
| GSON | 2.8.5 | https://mvnrepository.com/artifact/com.google.code.gson/gson/2.8.5
| MongoDB | 3.12.5 | https://mvnrepository.com/artifact/org.mongodb/mongo-java-driver/3.12.5
| Spring-Websocket | 5.2.2 | https://mvnrepository.com/artifact/org.springframework/spring-websocket/5.2.2.RELEASE

## # Documentation for Units group

### _Usage_

**getAllDeviceStatuses** - Is used for gathering all devices and their statuses, this is done via a **GET Request**.
```
http://localhost:8080/getAllDeviceStatuses
``` 
#### Response
```json
[
    {"deviceID":"6165e106a44071e42cf2dc77",
    "status":"on",
    "name":"Kitchen Lamp"
    },
        {"deviceID":"6165e127a44071e42cf2dc78",
        "status":"on",
        "name":"Garage Lamp"
        }
]
```

**changeDeviceStatus** - Is utilized for changing the status of a device. A **POST Request** is requiered with specific parameters.

### Format
| deviceName | <type> | status | on/off
| ------ | ------ | ------ | ------ |
| Kitchen Lamp | String | status | on

This will later be changed for instructing temperature changes, schedules for lamp etc.
### Example using Curl
```
curl -X POST http://localhost:8080/changeDeviceStatus -H "Content-Type: application/json" -d "{\"deviceName\":\"Kitchen Lamp\",\"status\":\"off\"}"
```
### Response
The response comes in two alternatives, either if the request was successful or not. 

#### successful
```json
{"operation":"success"}
```

#### failed
```json
{"reason":"Kitchen Lamp is already off","operation":"failed"}
```
### Broadcasting
Once a user uses the Endpoint **/changeDeviceStatus**, a message will be broadcasted to each client. 
#### Curtain changed example
```json
{"device":"curtain", "operation":"success", "option":"true"}
```
