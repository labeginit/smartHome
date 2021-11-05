package com.example.serverdatabase.DeviceConnector;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

public class HttpHandler {
    //The baseAddress will be the device group address with their port number then
    private String baseAddress = "http://194.47.40.176:7070/";
    //private String baseAddress = "http://194.47.28.185:7070/";

    //private WebTarget webTarget; //Will use it then

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();


    public String changeLampStatus(String deviceId, String status, String deviceType) throws IOException, InterruptedException, JSONException {
        String message;
        JSONObject json = new JSONObject();
        json.put("deviceType", deviceType);
        json.put("_id", deviceId);
        json.put("on", status);
        message = json.toString();


        // add json header
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(message))
                .uri(URI.create(baseAddress + "changeDeviceStatus"))
                .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return "ok";
        } else {
            return "Error";
        }
    }

    public String changeFanStatus(String speed, boolean isWarm) throws IOException, InterruptedException, JSONException {
        String message;
        JSONObject json = new JSONObject();
        json.put("speed", speed);
        json.put("isWarm", isWarm);
        message = json.toString();


        // add json header
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(message))
                .uri(URI.create(baseAddress + "updateFan"))
                .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return "ok";
            //To do
        } else {
            return "Error";
            //To dop
        }
    }

}
