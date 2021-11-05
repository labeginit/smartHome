package com.example.serverdatabase;

import com.google.gson.JsonObject;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

public class DBConnector {

    public static final String dbURI = ""; // Contact "Hugo Sigurdson" to get the URI
    public static final MongoClient mongoClient = new MongoClient(new MongoClientURI(dbURI));
    public static final MongoDatabase database = mongoClient.getDatabase("SEGFour");
    public static final MongoCollection<Document> collection = database.getCollection("Devices");

    static Document findDevice(String deviceName) {
        Bson filter = Filters.eq("_id", deviceName);
        FindIterable<Document> cursor = collection.find(filter);
        return cursor.first();
    }

    static void changeDeviceStatus(String deviceType, JsonObject jsonObject) {
        System.out.println(jsonObject);
        BasicDBObject query = new BasicDBObject();
        BasicDBObject newDocument = new BasicDBObject();
        query.put("_id", jsonObject.get("_id").toString().replace("\"", ""));

        if (deviceType.equals("lamp"))
            newDocument.put("on", jsonObject.get("on").toString().replace("\"", ""));
        if (deviceType.equals("thermometer"))
            newDocument.put("temp", jsonObject.get("temp").toString().replace("\"", ""));
        if (deviceType.equals("curtain"))
            newDocument.put("open", jsonObject.get("open").toString().replace("\"", ""));
        if (deviceType.equals("fan"))
            newDocument.put("speed", jsonObject.get("speed").toString().replace("\"", ""));
        if (deviceType.equals("TV")) {
            if (jsonObject.has("on")) {
                newDocument.put("on", jsonObject.get("on").toString().replace("\"", ""));
            } else if (jsonObject.has("channel")){
                newDocument.put("channel", jsonObject.get("channel").toString().replace("\"", ""));
            }
        }
        BasicDBObject updateObject = new BasicDBObject();
        updateObject.put("$set", newDocument);
        collection.updateOne(query, updateObject);
    }

}
