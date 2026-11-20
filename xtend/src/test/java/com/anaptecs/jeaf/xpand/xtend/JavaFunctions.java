package com.anaptecs.jeaf.xpand.xtend;

/**
 * Java implementations of Xtend functions (see Functions.ext).
 */
public class JavaFunctions {
  private final String prefix = "prefix-";

  public static String upper( String pValue ) {
    return pValue.toUpperCase();
  }

  // Non-static methods are called on a new instance that is created by Xtend.
  public String prefixed( String pValue ) {
    return prefix + pValue;
  }

  public static String fail( String pMessage ) {
    throw new IllegalStateException(pMessage);
  }
}
