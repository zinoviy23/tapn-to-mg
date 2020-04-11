package com.github.zinoviy23.tapaal.extenstions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ExtensionManager {
  private static ExtensionManager instance;

  private final Map<String, ExtensionPoint<?>> extensionPoints = new HashMap<>();

  private ExtensionManager() {
  }

  public static ExtensionManager getInstance() {
    if (instance == null) {
      synchronized (ExtensionManager.class) {
        if (instance == null) {
          instance = new ExtensionManager();
        }
      }
    }
    return instance;
  }

  public synchronized <T> void registerExtensionPoint(String name, Class<T> interfaceType) {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(interfaceType, "interfaceType");

    if (extensionPoints.containsKey(name)) {
      throw new ExtensionException("Already contains extensionPoint " + name);
    }
    extensionPoints.put(name, new ExtensionPoint<>(interfaceType));
  }

  public synchronized void registerExtension(String name, Class<?> extensionImpl) {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(extensionImpl, "extension");
    if (!extensionPoints.containsKey(name)) {
      throw new ExtensionException("There isn't any extension point " + name);
    }

    extensionPoints.get(name).addExtension(extensionImpl);
  }

  public synchronized <T> ExtensionPoint<T> getExtensionPoint(String name) {
    //noinspection unchecked
    return (ExtensionPoint<T>) extensionPoints.get(name);
  }
}
