# Server & DB

## _Documentation_

[![N|Solid](https://sts.hkr.se/adfs/portal/logo/logo.sv.png?id=EEF44783CA63147AE553003A4940C9CC9EB367CC3B5D0CD3AF6D260338D971B5)](https://nodesource.com/products/nsolid)

Documentation of how to use the API.

- What data is needed on a POST/GET request
- How the JSON data shall be formatted

> This can be updated at any time. If any errors or questions appear then please contact the Server/DB team.

## Dependencies | Maven

Three dependencies will be utilized. GSON is used for parsing JSON. MongoDB is used for connecting to the database. And
Websocket is used for keeping track of clients connected and to broadcast messages.

| Library | Version | Link |
| ------ | ------ | ------ | 
| GSON | 2.8.5 | https://mvnrepository.com/artifact/com.google.code.gson/gson/2.8.5
| MongoDB | 3.12.5 | https://mvnrepository.com/artifact/org.mongodb/mongo-java-driver/3.12.5
| Spring-Websocket | 5.2.2 | https://mvnrepository.com/artifact/org.springframework/spring-websocket/5.2.2.RELEASE

## # Documentation for Units group

### _Usage_

**getDevices** - Is used for gathering all devices (NOT TV!) and their statuses, this is done via the **Websocket
Connection**.

#### Response

```json
[
  {
    "deviceID": "Kitchen Lamp",
    "on": false
  },
  {
    "deviceID": "Bathroom Lamp",
    "on": true
  },
  {
    "temperature": 19.1,
    "deviceID": "Livingroom Thermometer"
  },
  {
    "deviceID": "Livingroom Curtain",
    "open": false
  }
]
```

**changeDeviceStatus** - Is utilized for changing the status of a device.

### Example of request

```
changeDeviceStatus={'_id':'Livingroom Curtain', 'open':'false'}
```

**Note!** In order for the server to corretly parse the request, the command and "=" is important. This is because the
server splits the request into an array of two. The first index containing the **changeDeviceStatus** command. The
second index containg the payload **{'_id':'Livingroom Curtain', 'open':'false'}** in this case.

### Format for light

| _id | on |
| ------ | ------ |
| Kitchen Lamp | false/true

### Format for thermometer

| _id | temperature |
| ------ | ------ |
| Livingroom Thermometer | 0-100°C

### Format for curtains

| _id | open |
| ------ | ------ |
| Livingroom Curtain | false/true

### Example using Curl

```
curl -X POST http://localhost:8080/changeDeviceStatus -H "Content-Type: application/json" -d "{\"deviceName\":\"Kitchen Lamp\",\"status\":\"off\"}"
```

### Response

The response comes in two alternatives, either if the request was successful or not.

#### successful

```json
{
  "operation": "success"
}
```

#### failed

```json
{
  "reason": "Kitchen Lamp is already off",
  "operation": "failed"
}
```

### Broadcasting

Once a user uses the Endpoint **/changeDeviceStatus**, a message will be broadcasted to each client. And if the request
fails, then a broadcast message wont be sent out.

#### Curtain changed example

```json
{
  "device": "curtain",
  "operation": "success",
  "option": "true"
}
```

## # Documentation for FreeChoice group

### _Usage_

**getTVStatus** - Will present a list of Objects-properties of the TV via a **GET Request**.

```
http://localhost:8080/getTVStatus
``` 

#### Response

```json
[
  "Livingroom TV",
  true,
  1
]
```

**changeDeviceStatus** - Is utilized for changing the status of a device. A **POST Request** is requiered with specific
parameters.

### Format

| deviceName | <type> | status | on/off
| ------ | ------ | ------ | ------ |
| Livingroom TV | String | on | true

***
IMPORTANT: it does not matter what boolean value you are sending. It will invert the current state of the TV
***

Channel handling functionality comes soon...

### Example using Curl

```
curl -X POST http://localhost:8080/changeDeviceStatus -H "Content-Type: application/json" -d "{\"_id\":\"Livingroom TV\",\"on\":\"true\"}" -s
```

### Response

An error message will come back in case of wrong device ID or no connection to the database, otherwise it will always
change and return latest the status

#### successful

```json
{
  "operation": "success"
}
```

#### failed

```json
{
  "reason": "An error has occurred, please try again"
}
```

### Broadcasting

Once a user uses the Endpoint **/changeDeviceStatus**, a message will be broadcasted to each client.

#### TV changed state example

```json
{
  "device": "TV",
  "operation": "success",
  "option": "true"
}
```

## # Documentation for Device group

### _Usage_

**"confirmation"** - Is used for sending confirmations from devices to the server.

```json
"confirmation={'_id':'Outdoor lamp','device':'lamp','status':'true','result':'success'}"
```

**"temperature"** - Is used for sending temperature.

Device send message in this form

```json
"temperature={'_id':'LivingRoom Thermometer','device':'thermometer','status':'19'}"
```

**"changeDeviceStatus2Device"** - Is used to get request messages for changing devices status.

Once a user uses the Endpoint **/changeDeviceStatus**, a message will be broadcasts to device and the message will look
like this:

```json
"changeDeviceStatus2Device={'_id':'Outdoor lamp','device':'lamp','operation':'success',option:'true'}"
```

**getDevices** - Is used for gathering all devices and their statuses.

#### Response

```json
[
  {
    "_id": "Outdoor lamp",
    "status": false
  },
  {
    "_id": "Indoor lamp",
    "status": true
  },
  {
    "_id": "Livingroom Thermometer",
    "status": 19.1
  },
  {
    "_id": "Bedroom Fan",
    "status": false
  },
  {
    "_id": "Alarm",
    "status": "0"
  }
]
```

#### Data Format
Fan speed has 3 speeds (1-2-3)

Alarm has three statuses (0-off, 1-on, 2-door opened and signal starts)

Lamps (true , false)

Thermometer (double)