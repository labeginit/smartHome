package com.example.serverdatabase;

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

    public static final String dbURI = "SECRET"; // Contact "Hugo Sigurdson" to get the URI
    public static final MongoClient mongoClient = new MongoClient(new MongoClientURI(dbURI));
    public static final MongoDatabase database = mongoClient.getDatabase("SEGFour");
    public static final MongoCollection<Document> collection = database.getCollection("Devices");

    static Document findDevice(String deviceName) {
        Bson filter = Filters.eq("name", deviceName);
        FindIterable<Document> cursor = collection.find(filter);
        return cursor.first();
    }

    static void changeDeviceStatus(String deviceName, String status) {
        BasicDBObject query = new BasicDBObject();
        query.put("name", deviceName);
        BasicDBObject newDocument = new BasicDBObject();
        newDocument.put("status", status);
        BasicDBObject updateObject = new BasicDBObject();
        updateObject.put("$set", newDocument);
        collection.updateOne(query, updateObject);
    }

}
