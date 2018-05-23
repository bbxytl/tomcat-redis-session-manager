package com.orangefunction.tomcat.redissessions;

import java.io.IOException;
import com.google.gson.Gson;

public interface Serializer {
  void setClassLoader(ClassLoader loader);
  void setGsonLoader(Gson loader);

  byte[] attributesHashFrom(RedisSession session) throws IOException;
  String serializeFrom(RedisSession session, SessionSerializationMetadata metadata) throws IOException;
  void deserializeInto(String data, RedisSession session, SessionSerializationMetadata metadata) throws IOException, ClassNotFoundException;
}
