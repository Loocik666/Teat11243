package com.example.plugin;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class Lang {
   private static Map<String, String> currentLangMap = new HashMap();
   private static final String FALLBACK_LANG = "en_us";

   public static void init(String langCode) {
      if (!load(langCode)) {
         load(FALLBACK_LANG);
      }

   }

   private static boolean load(String code) {
      String resourcePath = "/lang/" + code.toLowerCase() + ".json";

      try {
         InputStream inputStream = Lang.class.getResourceAsStream(resourcePath);
         if (inputStream == null) {
            return false;
         }

         try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            Map<String, String> loaded = (Map)(new Gson()).fromJson(reader, (new TypeToken<Map<String, String>>() {
            }).getType());
            currentLangMap = loaded != null ? loaded : new HashMap();
            System.out.println("[Plugin] Language loaded: " + code);
            return true;
         }
      } catch (Exception var4) {
         System.err.println("[Plugin] Failed to load language: " + code);
         return false;
      }
   }

   public static String get(String key, Object... args) {
      String pattern = (String)currentLangMap.getOrDefault(key, key);
      return MessageFormat.format(pattern, args);
   }
}
