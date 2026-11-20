package com.anaptecs.jeaf.xpand.uml2;

/**
 * Tests the UML2 type system with the MagicDraw export "Eclipse UML2 (v4.x) XMI" of the JEAF test model.
 */
public class UML2TypeSystemV4Test extends AbstractUML2TypeSystemTest {
  @Override
  protected String getModelArchive( ) {
    return "jeaf-test-model-uml2-v4.zip";
  }

  @Override
  protected String getExpectedUMLNamespace( ) {
    return "http://www.eclipse.org/uml2/4.0.0/UML";
  }
}
