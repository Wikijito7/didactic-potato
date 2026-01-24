# didactic-potato

Didactic potato is an Android app used for consuming and communicating with esp32 boards. This project
is part of Wokis' sensors network. Currently, didactic-potato is the name of the prototype, but it
will be changed on the future.

## How does it work
This app works by requesting to the API and displaying the results. 

This app is composed mostly by 3 flows: home, public data and profile.

### Home
Home shows a summary of the last received data of your sensors and which sensors do you have associated
to your profile.

In this screen you can add new sensors, click on them to see their detail.

This home is a column which has mostly two different cards, one which shows the summary and the
other is a row where we see user's sensors.

### Public Data
Also known as Sensors tab, this tab will show public sensors' data. You'll be able to filter sensors,
add them to favourite and see the detail of each sensor.

When you enter on this screen, you'll see the last data of this sensor. Inside the detail, you'll be
able to see more data of this sensor.

### Profile
This tab will show user's information and associated sensors to this profile.

User will be able to modify their profile image, information and add or remove sensors.

Each sensor can be configured, changing their name, pooling rate, temperature unit and more.

### Other workflows

#### Sensor pairing workflow
This workflow will work by communicating via bluetooth with the ESP32. Firstly, we will enable bluetooth
if it is disabled, then search for ESP32s and show them to the user, in order to configure the correct one.

Once the sensor is clicked, we will ask the user for some information, like the name of the sensor,
temperature unit, pooling rate and other stuff related with the sensor. Finally, we will make an API
request to register the device and send the needed information to the sensor using bluetooth.

#### Sensor workflow
In this screen, entered by clicking on a sensor from Home or profile tab, we will be able to see
historic data of this sensor by a points chart, and also modify some data if the sensor is owned by
the user, such as the name of the sensor and the visibility of the data. If the sensor data is set as
public, it will be shown to all users that uses didactic-potato.

## What it uses

### UI

For UI we will use Compose. There will be only one activity, which will be the entrypoint of the
app and will contain the router for all composables.

Each composable screen will have an associated ViewModel, which will be used to obtain the required 
data in order to show the screen.

Each screen will have a State data class, which contains all the data for this composable. All models
of this state will be contained on the same file as the State data class.

```kotlin
data class MyScreenState(
    val data: List<MyCustomModel>,
    [...]
)

data class MyCustomModel(
    val name: String,
    val age: Int,
    val isPublic: Boolean,
    [...]
)
```

## DI
For dependency injection we will use Koin.

### Requests
For request, we will use ktor client with okhttp.

### Serialization
For serialization, we will use kotlinx-serialization

### Architecture
For architecture, we will use MVI, Model ViewModel Intent, and Clean Architecture.

An example of this architecture is the following:

API <-- DataSource <-- Repository <-- UseCase <-- ViewModel <-- UI

UI sees ViewModel, ViewModel sees and uses UseCase(s), UseCase sees and uses Repository(ies), which
uses DataSource(s) to obtain the required data, from API, local files or local database.

Models for each layer will be contained on their own layer.
- Domain models will be named *BO.kt
- Api models will be named *DTO.kt
- UI models will be named *VO.kt

Each layer will be mapped by using extension functions. This mappers will be contained on their own
layer:
- API <-> Domain mapper will be contained and used on API layer.
- Domain <-> UI mapper will be contained and used on Domain layer.

### Testing
We will use JUnit 4 with Mockk library for mocks. We will test all classes that aren't data classes
or UI classes. 

## API
Base URL will be sensors.wokis.es/api/.

This API of this project is https://github.com/Wikijito7/shiny-tribble.

Auth will be handled by the usage of a Bearer token. There's also the possibility to use 2fa 
authentication. If it is required by the request, we will show a dialog to the user in order to 
introduce the 2fa TOTP code.

### API routing

#### Auth

### Generic models
Acknowledge model can be returned on some responses.
```json
{
  "acknowledge": true
}
```

##### Not authenticated
- POST /login: logs in the user. It can use username or email to authenticate.
- POST /register: registers the user. It will be asked username, email and password.
- GET /verify/{token}: It's used to verify user's email. It may be skipped, but is recommended to verify user's email.
- POST /recover: Used to generate recover email to recover user's password.
- POST /recover-pass: It will be used to recover user's password.

##### Authenticated
- POST /verify: resends verification email.
- POST /change-pass: changes user's password. It need old password and new password.
- POST /logout: removes user token.
- DELETE /sessions: removes all user tokens.

##### Models
###### Register
```json
{
  "email": "",
  "username": "",
  "password": "",
  "lang": ""
}
```

###### Login
```json
{
  "username": "",
  "password": ""
}
```

##### Login/Register response
```json
{
  "authToken": ""
}
```

##### Change password
```json
{
    "oldPass": "",
    "recoverCode": "",
    "newPass": ""
}
```

##### TOTP Request
```json
{
  "authType": "",
  "timestamp": 0
}
```

##### Recover password
```json
{
  "email": ""
}
```

#### User

##### Not authenticated
- GET /user/{id}/avatar: get user's avatar, if it exists.

##### Authenticated
- GET /user: fetch current user's info. It identifies the user by using JWT token. It will only fetch its own info.
- PUT /user: updates current user. It identifies the user by using JWT token. It will only modify its own info.
- POST /user/image: uploads new image for the authenticated user. It will be saved on the folder configured on app.conf.
- DELETE /user/image: removes user's image.
- POST: /2fa: activates 2fa authentication on this account. It will be used to confirm actions. 2fa will be asked whenever the route has withAuthenticator(user) wrapper.
- DELETE /2fa: removes 2fa authentication on this account. It will ask 2fa code.

#### Models
##### User model
```json
{
  "id": "",
  "username": "",
  "email": "",
  "image": "",
  "lang": "",
  "createdOn": 0,
  "totpEnabled": true,
  "emailVerified": true
}
```

#### Sensors

##### Authenticated
- GET /sensor: returns all raw data of all sensors associated to the user.
- GET /sensor/last: returns last data of each sensor associated to the user.
- GET /sensor/historical/{time}/{interval}: returns all historical data on the given time period, grouped by the interval of each sensor associated to the user.
- GET /sensor/{id}: returns all raw data of the given sensor.
- PUT /sensor/{id}: updates given sensor.
- DELETE /sensor/{id}: removes given sensor and all related data of this sensor.
- DELETE /sensor/{id}/{timestamp}: removes given sensor data record.

#### Models
##### Sensors Model
```json
{
  "sensors": [
    {
      "name": "",
      "data": {
        "temp": 24,
        "hum": 69,
        "timestamp": 0,
        "error": "",
        "battery": {
          "isCharging": true,
          "percentage": 42
        }
      }
    }
  ]
}
```

##### Simple Sensors Model
```json
{
  "sensors": [
    {
      "name":"",
      "temp":17.5,
      "hum":81.0,
      "timestamp":1767189825244,
      "error":"",
      "battery": {
        "isCharging": true,
        "percentage": 42
      }
    },
    {
      "name":"",
      "temp":16.5,
      "hum":97.9,
      "timestamp":1769247731930,
      "error":null,
      "battery":null
    }
  ]
}
```

