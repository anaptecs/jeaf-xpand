package com.anaptecs.jeaf.xpand.xtend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.eclipse.xtend.XtendFacade;
import org.eclipse.xtend.expression.EvaluationException;
import org.eclipse.xtend.expression.ExecutionContextImpl;
import org.eclipse.xtend.type.impl.java.JavaBeansMetaModel;
import org.junit.jupiter.api.Test;

public class XtendTest {
  private final XtendFacade facade;

  public XtendTest( ) {
    ExecutionContextImpl lContext = new ExecutionContextImpl();
    lContext.registerMetaModel(new JavaBeansMetaModel());
    facade = XtendFacade.create(lContext, "com::anaptecs::jeaf::xpand::xtend::Functions");
  }

  @Test
  void testRecursion( ) {
    ExpressionTest.assertInteger(1, facade.call("factorial", 1L));
    ExpressionTest.assertInteger(120, facade.call("factorial", 5L));
  }

  @Test
  void testCachedFunction( ) {
    Object lFirst = facade.call("greet", "Ada");
    assertEquals("Hello Ada", lFirst);
    assertSame(lFirst, facade.call("greet", "Ada"));
  }

  @Test
  void testPolymorphicDispatch( ) {
    assertEquals("string x", facade.call("describe", "x"));
    assertEquals("integer 7", facade.call("describe", 7L));
    assertEquals("object", facade.call("describe", 1.5));
  }

  @Test
  void testCreateExtension( ) {
    Object lList = facade.call("newList", "a");
    assertEquals(List.of("a"), lList);
    assertSame(lList, facade.call("newList", "a"));
  }

  @Test
  void testJavaExtensions( ) {
    assertEquals("ABC", facade.call("upper", "abc"));
    assertEquals("prefix-abc", facade.call("prefixed", "abc"));
  }

  @Test
  void testExceptionOfJavaExtension( ) {
    EvaluationException lException = assertThrows(EvaluationException.class, ( ) -> facade.call("fail", "broken"));
    assertInstanceOf(IllegalStateException.class, lException.getCause());
    assertEquals("broken", lException.getCause().getMessage());
  }
}
