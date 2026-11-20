package com.anaptecs.jeaf.xpand.uml2;

/**
 * Tests the UML2 type system with the MagicDraw export "Eclipse UML2 (v5.x) XMI" of the JEAF test model.
 */
public class UML2TypeSystemV5Test extends AbstractUML2TypeSystemTest {
  @Override
  protected String getModelArchive( ) {
    return "jeaf-test-model-uml2-v5.zip";
  }

  @Override
  protected String getExpectedUMLNamespace( ) {
    return "http://www.eclipse.org/uml2/5.0.0/UML";
  }
}
