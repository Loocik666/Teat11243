package com.example.plugin;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class Lang {
   private static Map<String, String> currentLangMap = new HashMap();
   private static final String FALLBACK_LANG = "en_us";

   public static void init(String langCode) {
      if (!load(langCode)) {
         load("en_us");
      }

   }

   private static boolean load(String code) {
      try {
         InputStreamReader reader = new InputStreamReader(Lang.class.getResourceAsStream("/lang/" + code.toLowerCase() + ".json"));

         boolean var2;
         try {
            currentLangMap = (Map)(new Gson()).fromJson(reader, (new TypeToken<Map<String, String>>() {
            }).getType());
            System.out.println("[Plugin] Langue chargée : " + code);
            var2 = true;
         } catch (Throwable var5) {
            try {
               reader.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }

            throw var5;
         }

         reader.close();
         return var2;
      } catch (Exception var6) {
         System.err.println("[Plugin] Impossible de charger la langue : " + code);
         return false;
      }
   }

   public static String get(String key, Object... args) {
      String pattern = (String)currentLangMap.getOrDefault(key, key);
      return MessageFormat.format(pattern, args);
   }
}
