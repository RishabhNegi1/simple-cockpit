\# Simple Cockpit API Contract



\## Endpoints



POST /api/v1/dashboard/sync

GET /health



\## Request



{

&#x20; "clientTimestamp": "2026-07-30T17:30:00Z",

&#x20; "action": "NONE"

}



Allowed actions:



\- NONE

\- TOGGLE\_PLAYBACK



\## Response



{

&#x20; "serverTimestamp": "2026-07-30T17:30:00Z",

&#x20; "speedKmh": 82.0,

&#x20; "batteryPercent": 74,

&#x20; "outsideTemperatureC": 21.5,

&#x20; "drivingStatus": "DRIVING",

&#x20; "media": {

&#x20;   "isPlaying": true,

&#x20;   "trackName": "Night Drive",

&#x20;   "progressPercent": 46

&#x20; },

&#x20; "navigation": {

&#x20;   "destination": "Central Station",

&#x20;   "remainingMinutes": 18,

&#x20;   "distanceKm": 12.4

&#x20; }

}



\## Constraints



\- action: NONE or TOGGLE\_PLAYBACK

\- speedKmh: 0 to 250

\- batteryPercent: 0 to 100

\- outsideTemperatureC: -20 to +50

\- drivingStatus: PARKED, DRIVING, or CHARGING

\- progressPercent: 0 to 100

\- timestamps: ISO-8601

