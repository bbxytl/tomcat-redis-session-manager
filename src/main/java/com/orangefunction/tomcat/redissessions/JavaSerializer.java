package com.orangefunction.tomcat.redissessions;

import java.util.Enumeration;
import java.util.HashMap;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.gson.Gson;

public class JavaSerializer implements Serializer {
    private ClassLoader loader;
    private Gson gson;

    private final Log log = LogFactory.getLog(JavaSerializer.class);

    @Override
    public void setClassLoader(ClassLoader loader) {
        this.loader = loader;
    }

    @Override
    public void setGsonLoader(Gson loader) {
        this.gson = loader;
    }

    public byte[] attributesHashFrom(RedisSession session) throws IOException {
        HashMap<String, Object> attributes = new HashMap<String, Object>();
        for (Enumeration<String> enumerator = session.getAttributeNames(); enumerator.hasMoreElements();) {
            String key = enumerator.nextElement();
            attributes.put(key, session.getAttribute(key));
        }

        byte[] serialized = null;

        try (
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(bos));
            ) {
            oos.writeUnshared(attributes);
            oos.flush();
            serialized = bos.toByteArray();
        }

        MessageDigest digester = null;
        try {
            digester = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            log.error("Unable to get MessageDigest instance for MD5");
        }
        return digester.digest(serialized);
    }

    @Override
    public String serializeFrom(RedisSession session, SessionSerializationMetadata metadata) throws IOException {
        String data = null;
        JsonSession jsession = session.writeJsonSession();
        data = gson.toJson(jsession);
        // System.out.println("===*****serializeFrom: " + data);
        return data;
    }

    @Override
    public void deserializeInto(String data, RedisSession session, SessionSerializationMetadata metadata) throws IOException, ClassNotFoundException {
        // System.out.println("===*****deserializeInto: " + data);
        JsonSession jsession = gson.fromJson(data, JsonSession.class);
        session.readJsonSession(jsession);
    }
}


class JsonSession {
    public Long creationTime;
    public Long lastAccessedTime;
    public Integer maxInactiveInterval;
    public Boolean isNew;
    public Boolean isValid;
    public Long thisAccessedTime;
    public String sessionId;
    public Long thisCreationTime;
    public ConcurrentMap<String, Object> attributesMap;

    public JsonSession() {
        attributesMap = new ConcurrentHashMap<String, Object>();
    }

    public JsonSession(Long creationTime, Long lastAccessedTime, Integer maxInactiveInterval, Boolean isNew, Boolean isValid, Long thisAccessedTime, String sessionId, Long thisCreationTime) {
        this.creationTime = creationTime;
        this.lastAccessedTime = lastAccessedTime;
        this.maxInactiveInterval = maxInactiveInterval;
        this.isNew = isNew;
        this.isValid = isValid;
        this.thisAccessedTime = thisAccessedTime;
        this.sessionId = sessionId;
        this.thisCreationTime = thisCreationTime;
        this.attributesMap = new ConcurrentHashMap<String, Object>();
    }

}
