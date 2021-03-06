package com.github.zinoviy23.tapaal.extended;

import com.github.zinoviy23.tapaal.extenstions.ExtensionManager;
import net.tapaal.TAPAAL;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public class ExtendedTapaal {
  private static final Pattern extensionPattern = Pattern.compile("(.+)\\.\\d+");

  public static void main(String[] args) {
    initializeExtensionPoints();
    initializeExtensions();
    TAPAAL.main(args);
  }

  private static void initializeExtensionPoints() {
    try (InputStream config = TAPAAL.class.getResourceAsStream("/resources/Plugins/tapaal.ext.properties")) {
      Properties properties = new Properties();
      properties.load(config);
      addExtensionPoints(properties);
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  private static void addExtensionPoints(Properties properties) throws ClassNotFoundException {
    ExtensionManager instance = ExtensionManager.getInstance();
    for (Map.Entry<Object, Object> extensionPoint : properties.entrySet()) {
      Class<?> extensionInterface = Class.forName((String) extensionPoint.getValue());
      instance.registerExtensionPoint((String) extensionPoint.getKey(), extensionInterface);
    }
  }

  private static void initializeExtensions() {
    try (InputStream config = TAPAAL.class.getResourceAsStream("/extensions.properties")) {
      Properties properties = new Properties();
      properties.load(config);
      addExtensions(properties);
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  private static void addExtensions(Properties properties) throws ClassNotFoundException {
    ExtensionManager instance = ExtensionManager.getInstance();
    for (Map.Entry<Object, Object> extension : properties.entrySet()) {
      Class<?> extensionInterface = Class.forName((String) extension.getValue());
      String key;
      var matcher = extensionPattern.matcher((String) extension.getKey());
      if (matcher.matches()) {
        key = matcher.group(1);
      } else {
        key = (String) extension.getKey();
      }
      instance.registerExtension(key, extensionInterface);
    }
  }
}
