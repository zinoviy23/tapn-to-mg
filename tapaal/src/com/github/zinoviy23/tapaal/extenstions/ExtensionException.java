package com.github.zinoviy23.tapaal.extenstions;

public class ExtensionException extends RuntimeException {
  public ExtensionException(String message) {
    super(message);
  }

  public ExtensionException(String message, Throwable cause) {
    super(message, cause);
  }
}
