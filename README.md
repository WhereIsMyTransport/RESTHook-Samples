# **WhereIsMyTransport Live Services - CaptainHook**

# Overview 

## Introduction 
CaptainHook is a service which allows you to subscribe to events using a 
simple REST API. When an event occurs, it is sent to event subscribers
as a JSON formatted `POST` request.   

## Usage

### Authentication

All our APIs use Open Id Connect. To access any WhereIsMyTransport service, you are required to obtain an access token. 
Whilst our services are in private preview, the instructions detailing how to obtain an access token should
have been enumerated in a separate email. From this point onwards, it is assumed that your access token 
is contained within the variable `$token`

__Note__: You can only delete a webhook with the same client credentials used to create it

### Creating a Webhook

[//]: # (This is) 

Our web hook implementation follows on many of the ideas contained within http://resthooks.org/docs/

REST Hooks are best described as a collection of patterns to create webhooks in a structured and automatable manner.


To create a hook, the receiver/client makes a `POST` request to one of the CaptainHook's endpoints (e.g. `/api/v1/subscriptions/alerts/agencie/{agencyId}`)

All `POST` requests to CaptainHook must include the entry `Authorization: 
Bearer {$token}` in the header. 

Assuming that  `$endpointUrl` is a valid absolute Url and `$description` is a string describing the webhook 
(or for channels, friendly name), the body should (for the most part) be:

```javascript
{
    "description": $description,
    "callbackUrl": $callbackUrl        
}
```

When CaptainHook API receives such a request, it sends a `POST` request to the `$callbackUrl` with the header `X-Hook-Secret`.
The unique value of `X-Hook-Secret` is important for security purposes and should be 
persisted by the receiver. Upon receipt of this initial message, the receiver should  
return a `200 OK` response which includes `X-Hook-Secret` header in order to active 
the webhook. If this initialisation request fails, CaptainHook will return  a 
`400 BAD_REQUEST` response to the client which attempted to create the webhook.
Else on success, CaptainHook will return `201 CREATED`, along with the webhook path.
This path is required to delete the hook.

You can remove a webhook by sending a `DELETE` request to the 
href returned in the initial `201 CREATED` message.

You can update a webhook by sending the original request, just using the `PUT` method.
Endpoints are reentrant as automation of hook creation is highly encouraged.
     
### Transport security

The value of `X-Hook-Secret` is the key used to generate the 
[HMAC](https://en.wikipedia.org/wiki/Hash-based_message_authentication_code). 
`X-Hook-Secret` is encoded as a base64 string.
 
For each call to `$callbackUrl`, The HMAC is included in the request
under the header key `X-Hook-Signature` as another base64 string, 
and is calculated using `HMAC-SHA-256` as detailed in 
[RFC4868](https://tools.ietf.org/html/rfc4868).

The use of HMACs, provided that the initial secret exchange is SSL encrypted,
prevents a number of classes of attacks, but is reliant on clients correctly
calculating the HMAC and comparing it to the `X-Hook-Signature`

You can find HMAC generation samples for various [languages here.](http://www.jokecamp.com/blog/examples-of-creating-base64-hashes-using-hmac-sha256-in-different-languages/)

### Dealing with connection failures

For each event, Captain Hook tries five times to `POST` the event to the callback URL 
using exponential backoff. Retries are performed on 400 and 500 http 
response codes with the exception of return code 410 (Gone).

If these five attempts fail, Captain Hook decrements the liveness counter for that 
callback url/event tuple. All callbacks/event tuple pairs
start off with 25 retry sets available to them. If the liveness counter reaches zero,
the callback/event tuple is deactived. If any retry set succeeds, 
the counter is reset to 25. This policy is to prevent abuse of the API and to ensure 
efficient bandwidth usage. 

For that reason, receivers should be liberal about resending subscription requests, 
especially upon startup or after transient connection failures. But as was previously
stated, all subscription endpoints are reentrant, which means if you send the same
request to the same endpoint, multiple times, only one subscription will be created.

# API

## Demo

### *Description:*

This event type was created to allow developers to test the webhook api before they 
receive real events. A demo event is sent approximately every 30 seconds.

### *Sample Body:* 

```json
{
   "channels":[
      "Facebook",
      "AfriGISTest",
      "Twitter",
      "ArbitraryChannelOfYourChoice"
   ],
   "message":"This is a demo message. This particular message has a random Guid of d4502bce-376e-451b-8bf1-13965c9c4753, which is a very fine Guid indeed.",
   "activePeriod":{
      "startTime":"2016-07-01T09:04:15Z",
      "endTime":"2016-07-03T09:04:15Z"
   },
   "alertId":"87d195d5-63f2-4ab6-860e-1a458fbf10d7",
   "username":"demo@example.com",
   "cause":{
      "value":"MAINTENANCE",
      "description":"Maintenance has caused our service to be degraded"
   },
   "effect":{
      "value":"NOSERVICE",
      "description":"The service has in effect being cancelled"
   },
   "delayDuration":0,
   "description":"A brief string describing the message template. Largely for internal purposes, however may be useful for other applications.",
   "informedEntities":[
      {
         "agencyId":"edObkk6o-0WN3tNZBLqKPg",
         "stopId":"ABC"
      }
   ],
   "messagePriority":"1",
   "publishedDate":"2016-07-01T09:04:15Z"
}
```

### *Endpoints:*

| Endpoint     |Verb | Description |
|--------------|------------ |------------| 
| `/api/v1/subscriptions/demos/` |`POST or POST` | Creates a webhook which receives demo events.                 |
| `/api/v1/subscriptions/demos?callbackUrl={{urlEncode($callbackUrl)}}` |`DELETE`  | Deletes the demo webhook      

## Messenger

### *Description:*

WhereIsMyTransport Messenger gives transit agencies the ability to easily create structured announcements
about their transit network. But if no one is listening, was the announcement even made? 

You can subscribe to alerts on three levels of detail: all, for each agency or if you 
have the appropriate credentials, you can create a channel which will appear on the Messenger
interface and you will be only sent a message whenever the agency selects the channel name.  

### *Sample Body:*
```json
{
   "channels":[
      "Facebook",
      "AfriGISTest",
      "Twitter",
      "ArbitraryChannelOfYourChoice"
   ],
   "message":"This is a demo message. This particular message has a random Guid of d4502bce-376e-451b-8bf1-13965c9c4753, which is a very fine Guid indeed.",
   "activePeriod":{
      "startTime":"2016-07-01T09:04:15Z",
      "endTime":"2016-07-03T09:04:15Z"
   },
   "alertId":"87d195d5-63f2-4ab6-860e-1a458fbf10d7",
   "username":"demo@example.com",
   "cause":{
      "value":"MAINTENANCE",
      "description":"Maintenance has caused our service to be degraded"
   },
   "effect":{
      "value":"NOSERVICE",
      "description":"The service has in effect being cancelled"
   },
   "delayDuration":0,
   "description":"A brief string describing the message template. Largely for internal purposes, however may be useful for other applications.",
   "informedEntities":[
      {
         "agencyId":"edObkk6o-0WN3tNZBLqKPg",
         "stopId":"ABC"
      }
   ],
   "messagePriority":"1",
   "publishedDate":"2016-07-01T09:04:15Z"
}
```

### *Endpoints:*

| Endpoint     | Verb        | Description |
|--------------|------------ |------------ |
| `/api/v1/subscriptions/alerts/` | `POST` | Creates a webhook which receives announcements from *any* transit authority.              |
| `/api/v1/subscriptions/alerts?callbackUrl={{urlEncode($callbackUrl)}}` | `DELETE` | Deletes the root alert webhook with `$callbackUrl`. |
| `/api/v1/subscriptions/alerts/agencies/{$agencyId}` |`POST` | Creates a webhook which receives announcements whenever `$agencyId` posts an announcement. |
| `/api/v1/subscriptions/alerts/agencies/{$agencyId}?callbackUrl={{urlEncode($callbackUrl)}}` |`DELETE` | Deletes the webhook to `$callbackUrl` for the given `$agencyId` |
| `/api/v1/subscriptions/alerts/authorities/{$authorityId}/channels/{$channelName}` |`POST` | Creates a webhook which receives announcements whenever `$authority` posts an announcement to `$channelName`. This also creates the channel if it doesn't already exist. Note that a channel can only have a single webhook, and if the channel already exists, this webhook is replaced. Only clients who have `$authorityId's` credentials can create a webhook of this type. |
| `/api/v1/subscriptions/alerts/authorities/{$authorityId}/channels/{$channelName}` |`DELETE` | Deletes the channel. |


------------

### *CaptainHook Url*:  https://webhooks.whereismytransport.com/
### *Samples*:  http://opensource.whereismytransport.com/
