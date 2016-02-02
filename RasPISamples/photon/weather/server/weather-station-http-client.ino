// This #include statement was automatically added by the Particle IDE.
#include "HttpClient/HttpClient.h"

/**
* This one pings the weather station app (running on the Raspberry PI)
* to get the json object that will be used to feed the IoT server on
* a regular base, for "real-time" data.
*/
unsigned int nextTime = 0;    // Next time to contact the server
HttpClient http;

// Headers currently need to be set at init, useful for API keys etc.
http_header_t headers[] = {
    //  { "Content-Type", "application/json" },
    //  { "Accept" , "application/json" },
    { "Accept" , "*/*"},
    { NULL, NULL } // NOTE: Always terminate headers will NULL
};

http_request_t request;
http_response_t response;

String httpPayload;
String status;
String urlToPing;

unsigned int SECOND = 1000;

void setup() {
//  Serial.begin(9600);
    Particle.variable("weatherHttpPayload", httpPayload);
    Particle.variable("weatherHttpStatus", status);
    Particle.variable("weatherHttpURL", urlToPing);
}

void loop() {
    if (nextTime > millis()) {
        return;
    }

//  Serial.println();
//  Serial.println("Application>\tStart of Loop.");
    status = "Start of the loop";
    // Request path and body can be set at runtime or at setup.
    request.hostname = "192.168.1.166";
    request.port     = 9876;
    request.path     = "/getJsonData"; // In json format

    urlToPing = "http://" + request.hostname + ":" + request.port + request.path;

    // The library also supports sending a body with your request:
    // request.body = "{\"key\":\"value\"}"; // For a POST

    // Get request
    http.get(request, response, headers);
//  Serial.print("Application>\tResponse status: ");
    status = response.status;
//  Serial.println(response.status);

//  Serial.print("Application>\tHTTP Response Body: ");
    httpPayload = response.body;
//  Serial.println(response.body);

    Spark.publish("weatherdata", httpPayload);

    nextTime = millis() + (10 * SECOND);
}
