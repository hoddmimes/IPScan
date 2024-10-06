import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.util.HashMap;

import  java.util.List;
import java.util.Map;


public class Caches {
    private static String CACHE_FILE = "ipscan.json";
    HashMap<String, String> mManufactors;
    HashMap<String, String> mMacAddresses;
    HashMap<String, String> mIpNames;
    HashMap<String, String> mIpCustomNames;

    Caches() {
        mMacAddresses = new HashMap<>();
        mIpNames = new HashMap<>();
        mManufactors = new HashMap<>();
        mIpCustomNames = new HashMap<>();
        loadCaches();
    }

    private void loadCaches() {
        try {
            File tCacheFile = new File( CACHE_FILE );
            if ((!tCacheFile.exists()) || (!tCacheFile.canRead())) {
                System.out.println("can not read cache file \"" + CACHE_FILE + "\"");
                return;
            }
            JsonObject jCache = JsonParser.parseReader(new FileReader(CACHE_FILE)).getAsJsonObject();
            // Load known  MAC manufactors
            JsonArray jManufactors = jCache.get("manufactors").getAsJsonArray();
            for (int i = 0; i < jManufactors.size(); i++) {
                JsonObject jManufactor = jManufactors.get(i).getAsJsonObject();
                mManufactors.put(jManufactor.get("mac").getAsString(), jManufactor.get("manufactor").getAsString());
            }

            // Load known  MAC addresses
            JsonArray jMacAddresses = jCache.get("mac_addresses").getAsJsonArray();
            for (int i = 0; i < jMacAddresses.size(); i++) {
                JsonObject jMac = jMacAddresses.get(i).getAsJsonObject();
                mMacAddresses.put(jMac.get("ip").getAsString(), jMac.get("mac").getAsString());
            }

            // Load known IP names
            JsonArray jIpName = jCache.get("ip_names").getAsJsonArray();
            for (int i = 0; i < jIpName.size(); i++) {
                JsonObject jName = jIpName.get(i).getAsJsonObject();
                mIpNames.put( jName.get("mac").getAsString(), jName.get("name").getAsString());
            }

            // Load known custom IP names
            JsonArray jIpCustomName = jCache.get("ip_custom_names").getAsJsonArray();
            for (int i = 0; i < jIpCustomName.size(); i++) {
                JsonObject jName = jIpCustomName.get(i).getAsJsonObject();
                mIpCustomNames.put( jName.get("ip").getAsString(), jName.get("name").getAsString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    void saveCaches() {
        JsonObject jRoot = new JsonObject();
        // Save MAC Manufactors
        JsonArray jManfactorArray = new JsonArray();
        for (Map.Entry<String,String> e : mManufactors.entrySet()) {
            JsonObject jManufactor = new JsonObject();
            jManufactor.addProperty("mac", e.getKey());
            jManufactor.addProperty("manufactor", e.getValue());
            jManfactorArray.add(jManufactor);
        }
        jRoot.add("manufactors", jManfactorArray);
        // Save MAC  Addresses
        JsonArray jMacArray = new JsonArray();
        for (Map.Entry<String,String> e : mMacAddresses.entrySet()) {
            JsonObject jMac = new JsonObject();
            jMac.addProperty("ip", e.getKey());
            jMac.addProperty("mac", e.getValue());
            jMacArray.add(jMac);
        }
        jRoot.add("mac_addresses", jMacArray);

        // Save IP Names
        JsonArray jNameArray = new JsonArray();
        for (Map.Entry<String,String> e : mIpNames.entrySet()) {
            JsonObject jName = new JsonObject();
            jName.addProperty("mac", e.getKey());
            jName.addProperty("name", e.getValue());
            jNameArray.add(jName);
        }
        jRoot.add("ip_names", jNameArray);

        // Save IP Custom Names
        JsonArray jCustomNameArray = new JsonArray();
        for (Map.Entry<String,String> e : mIpCustomNames.entrySet()) {
            JsonObject jName = new JsonObject();
            jName.addProperty("ip", e.getKey());
            jName.addProperty("name", e.getValue());
            jCustomNameArray.add(jName);
        }
        jRoot.add("ip_custom_names", jCustomNameArray);

        try {
            Writer writer = new FileWriter(CACHE_FILE);
            Gson gson = new GsonBuilder().create();
            gson.toJson(jRoot, writer);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void reset( boolean pResetCustomNames ) {
        mMacAddresses.clear();
        mManufactors.clear();
        mIpNames.clear();
        if (pResetCustomNames) {
            mIpCustomNames.clear();
        }
        saveCaches();
    }



    static class IpName {
        String  mName;
        boolean mCustomName;

        IpName( String pName, boolean pCustomName) {
            mName = pName;
            mCustomName = pCustomName;
        }
    }



    }








