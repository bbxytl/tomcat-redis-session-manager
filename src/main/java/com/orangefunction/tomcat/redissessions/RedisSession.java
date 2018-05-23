package com.orangefunction.tomcat.redissessions;

import java.security.Principal;
import org.apache.catalina.Manager;
import org.apache.catalina.session.StandardSession;
import java.util.HashMap;
import java.io.IOException;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.util.Enumeration;

public class RedisSession extends StandardSession {

    private final Log log = LogFactory.getLog(RedisSession.class);

    protected static Boolean manualDirtyTrackingSupportEnabled = false;

    public static void setManualDirtyTrackingSupportEnabled(Boolean enabled) {
        manualDirtyTrackingSupportEnabled = enabled;
    }

    protected static String manualDirtyTrackingAttributeKey = "__changed__";

    public static void setManualDirtyTrackingAttributeKey(String key) {
        manualDirtyTrackingAttributeKey = key;
    }


    protected HashMap<String, Object> changedAttributes;
    protected Boolean dirty;


    public RedisSession(Manager manager) {
        super(manager);
        resetDirtyTracking();
    }

    public Boolean isDirty() {
        return dirty || !changedAttributes.isEmpty();
    }

    public HashMap<String, Object> getChangedAttributes() {
        return changedAttributes;
    }

    public void resetDirtyTracking() {
        changedAttributes = new HashMap<>();
        dirty = false;
    }

    @Override
    public void setAttribute(String key, Object value) {
        if (manualDirtyTrackingSupportEnabled && manualDirtyTrackingAttributeKey.equals(key)) {
            dirty = true;
            return;
        }

        Object oldValue = getAttribute(key);
        super.setAttribute(key, value);

        if ( (value != null || oldValue != null)
                && ( value == null && oldValue != null
                     || oldValue == null && value != null
                     || !value.getClass().isInstance(oldValue)
                     || !value.equals(oldValue) ) ) {
            if (this.manager instanceof RedisSessionManager
                    && ((RedisSessionManager)this.manager).getSaveOnChange()) {
                try {
                    ((RedisSessionManager)this.manager).save(this, true);
                } catch (IOException ex) {
                    log.error("Error saving session on setAttribute (triggered by saveOnChange=true): " + ex.getMessage());
                }
            } else {
                changedAttributes.put(key, value);
            }
        }
    }

    @Override
    public void removeAttribute(String name) {
        super.removeAttribute(name);
        if (this.manager instanceof RedisSessionManager
                && ((RedisSessionManager)this.manager).getSaveOnChange()) {
            try {
                ((RedisSessionManager)this.manager).save(this, true);
            } catch (IOException ex) {
                log.error("Error saving session on setAttribute (triggered by saveOnChange=true): " + ex.getMessage());
            }
        } else {
            dirty = true;
        }
    }

    @Override
    public void setId(String id) {
        // Specifically do not call super(): it's implementation does unexpected things
        // like calling manager.remove(session.id) and manager.add(session).

        this.id = id;
    }

    @Override
    public void setPrincipal(Principal principal) {
        dirty = true;
        super.setPrincipal(principal);
    }

    @Override
    public void writeObjectData(java.io.ObjectOutputStream out) throws IOException {
        super.writeObjectData(out);
        out.writeLong(this.getCreationTime());

        if (manager.getContainer().getLogger().isDebugEnabled()) {
            manager.getContainer().getLogger().debug("writeObject() storing session " + id);
        }

    }

    @Override
    public void readObjectData(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        super.readObjectData(in);
        this.setCreationTime(in.readLong());
    }

    public void readJsonSession(JsonSession jsession) {
        creationTime = jsession.creationTime;
        lastAccessedTime = jsession.lastAccessedTime;
        maxInactiveInterval = jsession.maxInactiveInterval;
        isNew = jsession.isNew;
        isValid = jsession.isValid;
        thisAccessedTime = jsession.thisAccessedTime;
        id = jsession.sessionId;
        this.setCreationTime(jsession.thisCreationTime);

        Gson gson2 = new GsonBuilder().enableComplexMapKeySerialization().create();
        Type type = new TypeToken<ConcurrentHashMap<String, Object>>() {}.getType();
        this.attributes = gson2.fromJson(jsession.attributes, type);

    }

    public JsonSession writeJsonSession() {
        JsonSession jsonSession = new JsonSession();
        // Write the scalar instance variables (except Manager)
        jsonSession.creationTime = Long.valueOf(creationTime);
        jsonSession.lastAccessedTime =  Long.valueOf(lastAccessedTime);
        jsonSession.maxInactiveInterval = Integer.valueOf(maxInactiveInterval);
        jsonSession.isNew =  Boolean.valueOf(isNew);
        jsonSession.isValid = Boolean.valueOf(isValid);
        jsonSession.thisAccessedTime = Long.valueOf(thisAccessedTime);
        jsonSession.sessionId = id;
        jsonSession.thisCreationTime = this.getCreationTime();

        System.out.println("+++++++++++++++++++ jsonSession.: ");
        // Gson gson2 = new GsonBuilder().enableComplexMapKeySerialization().create();
        // jsonSession.attributes = gson2.toJson(this.attributes);
        HashMap<String, List<String>> employees = new HashMap<>();
        employees.put("A", Arrays.asList("Andreas", "Arnold", "Aden"));
        employees.put("C", Arrays.asList("Christian", "Carter"));
        employees.put("M", Arrays.asList("Marcus", "Mary"));
        System.out.println(new Gson().toJson(employees));

        ConcurrentMap<String, Object> myattributes = new ConcurrentHashMap<String, Object>();
        this.putValue("test", "test");
        System.out.println("+++++++++++++" + this.getAttribute("test"));
        Enumeration<String> keys = this.getAttributeNames();
        while(keys.hasMoreElements()) {
            String name = keys.nextElement();
            System.out.println("=== " + name);
            Object value = this.getAttribute(name);
            myattributes.put(name, value);
        }
        myattributes.put("hashMap", employees);
        // for (String s : this.getAttributeNames() ){
            // System.out.println("=== " + s);
        // }

        /* for (String s : attributes.keySet() ){
            System.out.println("=== " + s);
        }
        System.out.println("+++++++++++++" + this.attributes.get("test"));
 */
        jsonSession.attributesMap = myattributes;
        jsonSession.attributes = new Gson().toJson(myattributes, ConcurrentMap.class);

        System.out.println("+++++++++++++++++++ jsonSession.attributes: " + jsonSession.attributes);
        return jsonSession;
    }
}
