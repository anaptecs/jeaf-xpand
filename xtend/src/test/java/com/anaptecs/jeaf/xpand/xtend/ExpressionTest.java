package com.anaptecs.jeaf.xpand.xtend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.xtend.expression.ExecutionContextImpl;
import org.eclipse.xtend.expression.ExpressionFacade;
import org.eclipse.xtend.type.impl.java.JavaBeansMetaModel;
import org.junit.jupiter.api.Test;

public class ExpressionTest {
  private final ExpressionFacade facade;

  public ExpressionTest( ) {
    ExecutionContextImpl lContext = new ExecutionContextImpl();
    lContext.registerMetaModel(new JavaBeansMetaModel());
    facade = new ExpressionFacade(lContext);
  }

  private Object eval( String pExpression ) {
    return facade.evaluate(pExpression);
  }

  /**
   * Integer arithmetic returns {@link BigInteger}, literals and lengths return {@link Long}, so only the value is compared.
   */
  static void assertInteger( long pExpected, Object pActual ) {
    assertInstanceOf(Number.class, pActual);
    assertEquals(BigInteger.valueOf(pExpected), new BigInteger(pActual.toString()));
  }

  static void assertIntegers( List<Long> pExpected, Object pActual ) {
    assertInstanceOf(Collection.class, pActual);
    List<BigInteger> lActual = new ArrayList<>();
    for (Object lNext : (Collection<?>) pActual) {
      assertInstanceOf(Number.class, lNext);
      lActual.add(new BigInteger(lNext.toString()));
    }
    assertEquals(pExpected.stream().map(BigInteger::valueOf).toList(), lActual);
  }

  @Test
  void testIntegerArithmetic( ) {
    assertInteger(3, eval("1 + 2"));
    assertInteger(-3, eval("7 - 10"));
    assertInteger(42, eval("6 * 7"));
    assertInteger(3, eval("7 / 2"));
    assertInteger(-5, eval("-5"));
  }

  @Test
  void testRealArithmetic( ) {
    assertEquals(3.75, eval("1.5 + 2.25"));
    assertEquals(-0.5, eval("1.5 - 2.0"));
    assertEquals(7.0, eval("2.0 * 3.5"));
    assertEquals(3.5, eval("7.0 / 2.0"));
    assertEquals(-1.5, eval("-1.5"));
  }

  @Test
  void testComparisons( ) {
    assertEquals(true, eval("3 > 2"));
    assertEquals(false, eval("2 >= 3"));
    assertEquals(true, eval("2 <= 2"));
    assertEquals(true, eval("2.5 < 3.5"));
    assertEquals(true, eval("2.5 >= 2.5"));
    assertEquals(false, eval("2.5 > 3.5"));
    assertEquals(true, eval("'a' < 'b'"));
    assertEquals(-1L, eval("'a'.compareTo('b')"));
    assertEquals(0L, eval("'a'.compareTo('a')"));
  }

  @Test
  void testEquality( ) {
    assertEquals(true, eval("1 == 1"));
    assertEquals(true, eval("1 != 2"));
    assertEquals(true, eval("1.5 == 1.5"));
    assertEquals(true, eval("1.5 != 2.5"));
    assertEquals(true, eval("'abc' == 'abc'"));
    assertEquals(true, eval("null == null"));
    assertEquals(true, eval("null != 'x'"));
  }

  @Test
  void testBooleanOperations( ) {
    assertEquals(true, eval("true && !false"));
    assertEquals(true, eval("false || true"));
    assertEquals(false, eval("!true"));
    assertEquals(false, eval("true implies false"));
  }

  @Test
  void testStringOperations( ) {
    assertEquals(5L, eval("'hello'.length"));
    assertEquals(true, eval("'hello'.startsWith('he')"));
    assertEquals(true, eval("'hello'.endsWith('lo')"));
    assertEquals(true, eval("'hello'.contains('ell')"));
    assertEquals(false, eval("'hello'.contains('xyz')"));
    assertEquals("HELLO", eval("'hello'.toUpperCase()"));
    assertEquals("Hello", eval("'hello'.toFirstUpper()"));
    assertEquals("ell", eval("'hello'.subString(1, 4)"));
    assertEquals(3L, eval("'a,b,c'.split(',').size"));
    assertEquals("ab", eval("'a' + 'b'"));
  }

  @Test
  void testCollections( ) {
    assertEquals(3L, eval("{1, 2, 3}.size"));
    assertEquals(true, eval("{}.isEmpty"));
    assertEquals(false, eval("{1}.isEmpty"));
    assertEquals(1L, eval("{'a', 'b'}.indexOf('b')"));
    assertEquals(2L, eval("{1, 2, 3}.select(e | e > 1).size"));
    assertIntegers(List.of(2L, 4L, 6L), eval("{1, 2, 3}.collect(e | e * 2)"));
    assertInteger(1, eval("{3, 1, 2}.sortBy(e | e).first()"));
    assertEquals(true, eval("{1, 2, 3}.exists(e | e == 2)"));
    assertEquals(true, eval("{1, 2, 3}.forAll(e | e > 0)"));
    assertEquals(2L, eval("{1, 2, 2}.toSet().size"));
  }

  @Test
  void testLetAndIf( ) {
    assertInteger(9, eval("let x = 3 : x * x"));
    assertEquals("yes", eval("1 > 0 ? 'yes' : 'no'"));
    assertEquals("no", eval("if 1 < 0 then 'yes' else 'no'"));
  }

  @Test
  void testTypes( ) {
    assertEquals(true, eval("String.isInstance('x')"));
    assertEquals(false, eval("Integer.isInstance('x')"));
    assertEquals("String", eval("'x'.metaType.name"));
  }

  @Test
  void testVariables( ) {
    assertInteger(5, facade.evaluate("a + b", Map.of("a", 2L, "b", 3L)));
  }

  @Test
  void testJavaBeans( ) {
    assertEquals("Ada (36)", facade.evaluate("person.name + ' (' + person.age + ')'", Map.of("person", new Person("Ada", 36))));
  }

  public static class Person {
    private final String name;

    private final int age;

    public Person( String pName, int pAge ) {
      name = pName;
      age = pAge;
    }

    public String getName( ) {
      return name;
    }

    public int getAge( ) {
      return age;
    }
  }
}
