package com.anaptecs.jeaf.xpand.stdlib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.emf.mwe.core.WorkflowContextDefaultImpl;
import org.eclipse.emf.mwe.core.issues.Issues;
import org.eclipse.emf.mwe.core.issues.IssuesImpl;
import org.eclipse.emf.mwe.core.monitor.NullProgressMonitor;
import org.eclipse.xtend.XtendFacade;
import org.eclipse.xtend.expression.ExecutionContextImpl;
import org.eclipse.xtend.util.stdlib.PropertiesReader;
import org.junit.jupiter.api.Test;

public class StdlibTest {
  private static final String STDLIB = "org::eclipse::xtend::util::stdlib::";

  private static XtendFacade createFacade( String... pExtensions ) {
    String[] lExtensions = Arrays.stream(pExtensions).map(e -> STDLIB + e).toArray(String[]::new);
    return XtendFacade.create(new ExecutionContextImpl(new HashMap<>()), lExtensions);
  }

  @Test
  void testCounter( ) {
    XtendFacade lFacade = createFacade("counter");
    assertEquals(0, lFacade.call("counterGet", "a"));
    assertEquals(1, lFacade.call("counterInc", "a"));
    assertEquals(2, lFacade.call("counterInc", "a"));
    assertEquals(1, lFacade.call("counterInc", "b"));
    assertEquals(1, lFacade.call("counterDec", "a"));
    assertEquals(1, lFacade.call("counterGet", "a"));
    assertEquals(0, lFacade.call("counterReset", "a"));
    assertEquals(0, lFacade.call("counterGet", "a"));
    assertEquals(1, lFacade.call("counterGet", "b"));
  }

  @Test
  void testStateIsBoundToExecutionContext( ) {
    XtendFacade lFirst = createFacade("counter", "globalvar");
    lFirst.call("counterInc", "a");
    lFirst.call("storeGlobalVar", "key", "value");

    XtendFacade lSecond = createFacade("counter", "globalvar");
    assertEquals(0, lSecond.call("counterGet", "a"));
    assertNull(lSecond.call("getGlobalVar", "key"));
  }

  @Test
  void testGlobalVariables( ) {
    XtendFacade lFacade = createFacade("globalvar");
    assertNull(lFacade.call("getGlobalVar", "key"));
    assertEquals("value", lFacade.call("storeGlobalVar", "key", "value"));
    assertEquals("value", lFacade.call("getGlobalVar", "key"));
    assertEquals("value", lFacade.call("removeGlobalVar", "key"));
    assertNull(lFacade.call("getGlobalVar", "key"));

    lFacade.call("storeGlobalVar", "first", 1L);
    lFacade.call("storeGlobalVar", "second", 2L);
    lFacade.call("clearGlobalVars");
    assertNull(lFacade.call("getGlobalVar", "first"));
    assertNull(lFacade.call("getGlobalVar", "second"));
  }

  @Test
  void testElementProperties( ) {
    XtendFacade lFacade = createFacade("elementprops");
    Object lElement = new Object();
    Object lOther = new Object();
    lFacade.call("setProperty", lElement, "name", "value");
    assertEquals("value", lFacade.call("getProperty", lElement, "name"));
    assertNull(lFacade.call("getProperty", lOther, "name"));
    lFacade.call("setProperty", lElement, "name", null);
    assertNull(lFacade.call("getProperty", lElement, "name"));
  }

  @Test
  void testProperties( ) {
    PropertiesReader lReader = new PropertiesReader();
    lReader.addPropertiesFile("com/anaptecs/jeaf/xpand/stdlib/test.properties");
    Issues lIssues = new IssuesImpl();
    lReader.checkConfiguration(lIssues);
    lReader.invoke(new WorkflowContextDefaultImpl(), new NullProgressMonitor(), lIssues);
    assertFalse(lIssues.hasErrors(), lIssues::toString);

    XtendFacade lFacade = createFacade("properties");
    assertEquals("JEAF Generator", lFacade.call("getProperty", "generator.name"));
    assertEquals("3.0.0", lFacade.call("getProperty", "generator.version"));
    assertNull(lFacade.call("getProperty", "unknown"));
  }

  @Test
  void testUID( ) {
    XtendFacade lFacade = createFacade("uid");
    Object lElement = new Object();
    Object lUID = lFacade.call("uid", lElement);
    assertSame(lUID, lFacade.call("uid", lElement));
    assertNotEquals(lUID, lFacade.call("uid", new Object()));
  }

  @Test
  void testCollections( ) {
    XtendFacade lFacade = createFacade("collections");
    // Object array is required as call(String, List) would pass the list elements as separate parameters.
    Object[] lParameters = { Arrays.asList("a", null, "b", null) };
    assertEquals(List.of("a", "b"), lFacade.call("filterNulls", lParameters));
  }
}
