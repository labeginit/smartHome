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

import java.util.Map;

public class DBConnector {
    public static final MongoClient mongoClient = new MongoClient(new MongoClientURI(token()));
    public static final MongoDatabase database = mongoClient.getDatabase("SEGFour");
    public static final MongoCollection<Document> collection = database.getCollection("Devices");
    private final static String ID = "_id";

    static Document findDevice(String deviceName) {
        Bson filter = Filters.eq(ID, deviceName);
        FindIterable<Document> cursor = collection.find(filter);
        return cursor.first();
    }

    static void changeDeviceStatus(String deviceType, JsonObject jsonObject) {
        System.out.println(jsonObject);
        BasicDBObject query = new BasicDBObject();
        BasicDBObject newDocument = new BasicDBObject();
        query.put(ID, jsonObject.get(ID).toString().replace("\"", ""));

        if (deviceType.equals(DeviceType.LAMP.value) || deviceType.equals(DeviceType.THERMOMETER.value) || deviceType.equals(DeviceType.FAN.value) || deviceType.equals(DeviceType.CURTAIN.value) || deviceType.equals(DeviceType.ALARM.value) || deviceType.equals(DeviceType.HEATER.value)){
            newDocument.put("status", jsonObject.get("status").toString().replace("\"", ""));
        }

        BasicDBObject updateObject = new BasicDBObject();
        updateObject.put("$set", newDocument);
        collection.updateOne(query, updateObject);
    }

    static String token(){
        Map<String,String> env = System.getenv();
        return env.get("MONGO_TOKEN");
    }

    static void insertNewDoc(String _id, String deviceType, String status) {
        Document doc = new Document(ID, _id)
                .append("device", deviceType)
                .append("status", status);
        collection.insertOne(doc);
    }

    static void removeDoc(String _id) {
        collection.deleteOne(Filters.eq("_id", _id));
    }

}
