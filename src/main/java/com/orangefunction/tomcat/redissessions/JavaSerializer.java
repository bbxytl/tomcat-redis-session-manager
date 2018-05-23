package com.orangefunction.tomcat.redissessions;

import org.apache.catalina.util.CustomObjectInputStream;

import javax.servlet.http.HttpSession;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import java.util.Enumeration;
import java.util.HashMap;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.catalina.Session;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

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
        // return "test88888";
        System.out.println("============ In ");
        String data = null;
        System.out.println("============ In 1");
        data = JSON.toJSONString(session);
        /* // data = Gson.class.newInstance().toJson(session);
        // data = gson.toJson(session);
        if (data == null) {
            data = "test333";
        }
        System.out.println("============ " + data);

        if (data == "null") {
            data = "test-null-00";
        } */
        System.out.println("============ out.data:  " + data);
        return data;
        /* DeserializedSessionContainer container = new DeserializedSessionContainer(session, metadata);
        String data = null;
        try {
              data = Gson.class.newInstance().toJson(container);
        } catch (InstantiationException | IllegalAccessException e) {
              e.printStackTrace();
        }
        return data; */
        /* byte[] serialized = null;

        try (
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(bos));
        ) {
          oos.writeObject(metadata);
          session.writeObjectData(oos);
          oos.flush();
          serialized = bos.toByteArray();
        }

        return serialized; */
    }

    @Override
    public void deserializeInto(String data, RedisSession session, SessionSerializationMetadata metadata) throws IOException, ClassNotFoundException {
        System.out.println("===*****========= " + data);
        session = gson.fromJson(data, RedisSession.class);
        /* try {
            session = Gson.class.newInstance().fromJson(data, RedisSession.class);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        } */
        /* DeserializedSessionContainer container = new DeserializedSessionContainer(session, metadata);
        try {
              container = Gson.class.newInstance().fromJson(data, DeserializedSessionContainer.class);
              session = container.session;
              metadata = container.metadata;
        } catch (InstantiationException | IllegalAccessException e) {
              e.printStackTrace();
        } */
        // session = gson.fromJson(data, RedisSession.class);
        /* try(
            BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(data));
            ObjectInputStream ois = new CustomObjectInputStream(bis, loader);
        ) {
          SessionSerializationMetadata serializedMetadata = (SessionSerializationMetadata)ois.readObject();
          metadata.copyFieldsFrom(serializedMetadata);
          session.readObjectData(ois);
        } */
        /* SessionSerializationMetadata metadatanew = JSON.parseObject(data, SessionSerializationMetadata.class);
        metadata.copyFieldsFrom(metadatanew);
        System.out.println(metadata.getSessionAttributesHash().toString()); */
        // session = (RedisSession)JSON.parseObject(data, Session.class);
        System.out.println("***************** " + session.getId() + session.getInfo());
    }
}
