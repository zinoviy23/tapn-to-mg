package com.github.zinoviy23.tapaal.extenstions;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public final class ExtensionPoint<T> {
  private final Class<T> interfaceType;
  private final List<Class<?>> extensionClasses = new ArrayList<>();

  private final Map<Class<?>, Object> extensionCache = new HashMap<>();

  public ExtensionPoint(Class<T> interfaceType) {
    this.interfaceType = interfaceType;
  }

  public Class<T> getInterfaceType() {
    return interfaceType;
  }

  public synchronized List<T> getExtensions() {
    return extensionClasses.stream()
        .map(ext -> {
          @SuppressWarnings("unchecked")
          T instance = (T) extensionCache.computeIfAbsent(ext, ExtensionPoint::instantiate);
          return instance;
        })
        .collect(Collectors.toList());
  }

  public synchronized void addExtension(Class<?> extension) {
    if (!interfaceType.isAssignableFrom(extension)) {
      throw new ExtensionException("Adding extension of wrong type. Expected " + interfaceType.getName());
    }

    extensionClasses.add(extension);
  }

  private static Object instantiate(Class<?> clazz) {
    try {
      Constructor<?> constructor = clazz.getConstructor();
      return constructor.newInstance();
    } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
      throw new ExtensionException("Extension must have default constructor", e);
    }
  }
}
