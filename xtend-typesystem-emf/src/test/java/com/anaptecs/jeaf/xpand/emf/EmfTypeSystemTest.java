package com.anaptecs.jeaf.xpand.emf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.mwe.core.WorkflowContext;
import org.eclipse.emf.mwe.core.WorkflowContextDefaultImpl;
import org.eclipse.emf.mwe.core.issues.Issues;
import org.eclipse.emf.mwe.core.issues.IssuesImpl;
import org.eclipse.emf.mwe.core.monitor.NullProgressMonitor;
import org.eclipse.xtend.expression.ExecutionContextImpl;
import org.eclipse.xtend.expression.ExpressionFacade;
import org.eclipse.xtend.typesystem.emf.EcoreUtil2;
import org.eclipse.xtend.typesystem.emf.EmfMetaModel;
import org.eclipse.xtend.typesystem.emf.XmiReader;
import org.eclipse.xtend.typesystem.emf.XmiWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class EmfTypeSystemTest {
  private static final String METAMODEL = "com/anaptecs/jeaf/xpand/emf/library.ecore";

  private static final String MODEL = "com/anaptecs/jeaf/xpand/emf/library.xmi";

  private static final String SLOT = "model";

  /**
   * Loading an ecore file creates and registers a new EPackage, so it is loaded only once to keep model and type
   * system consistent.
   */
  private static final EPackage LIBRARY_PACKAGE = EcoreUtil2.getEPackage(METAMODEL);

  private final ExpressionFacade facade;

  public EmfTypeSystemTest( ) {
    ExecutionContextImpl lContext = new ExecutionContextImpl();
    lContext.registerMetaModel(new EmfMetaModel(LIBRARY_PACKAGE));
    facade = new ExpressionFacade(lContext);
  }

  private static EObject readModel( String pModelFile ) {
    XmiReader lReader = new XmiReader();
    lReader.setModelFile(pModelFile);
    lReader.setOutputSlot(SLOT);
    lReader.setFirstElementOnly(true);
    WorkflowContext lWorkflowContext = new WorkflowContextDefaultImpl();
    Issues lIssues = new IssuesImpl();
    lReader.invoke(lWorkflowContext, new NullProgressMonitor(), lIssues);
    assertFalse(lIssues.hasErrors(), lIssues::toString);
    return (EObject) lWorkflowContext.get(SLOT);
  }

  private Object eval( String pExpression, Object pModel ) {
    return facade.evaluate(pExpression, Map.of("library", pModel));
  }

  @Test
  void testReadModel( ) {
    EObject lLibrary = readModel(MODEL);
    assertEquals("Library", lLibrary.eClass().getName());
    assertEquals("City Library", eval("library.name", lLibrary));
    assertEquals(3L, eval("library.books.size", lLibrary));
    assertEquals(List.of("Moby Dick", "Leaves of Grass", "On the Origin of Species"),
        eval("library.books.title", lLibrary));
  }

  @Test
  void testTypes( ) {
    EObject lLibrary = readModel(MODEL);
    assertEquals("library::Library", eval("library.metaType.name", lLibrary));
    assertEquals(true, eval("library::Item.isInstance(library.books.first())", lLibrary));
    assertEquals(false, eval("library::Book.isInstance(library)", lLibrary));
    assertEquals(3L, eval("library.eAllContents.typeSelect(library::Item).size", lLibrary));
    assertEquals(List.of("emf::EObject", "library::Item"), eval("library::Book.superTypes.name", lLibrary));
  }

  @Test
  void testAttributesAndEnums( ) {
    EObject lLibrary = readModel(MODEL);
    assertEquals(635, eval("library.books.first().pages", lLibrary));
    assertEquals(List.of("Leaves of Grass"),
        eval("library.books.select(b | b.genre == library::Genre::POETRY).title", lLibrary));
    assertEquals(List.of("Moby Dick", "On the Origin of Species"),
        eval("library.books.select(b | b.pages > 100).title", lLibrary));
    assertEquals(List.of("Leaves of Grass", "On the Origin of Species", "Moby Dick"),
        eval("library.books.sortBy(b | b.pages).title", lLibrary));
  }

  @Test
  void testContainment( ) {
    EObject lLibrary = readModel(MODEL);
    assertEquals(lLibrary, eval("library.books.first().eContainer", lLibrary));
    assertEquals(lLibrary, eval("library.books.last().eRootContainer", lLibrary));
    assertEquals(3L, eval("library.eContents.size", lLibrary));
  }

  @Test
  void testEcoreMetaModel( ) {
    ExecutionContextImpl lContext = new ExecutionContextImpl();
    lContext.registerMetaModel(new EmfMetaModel(EcorePackage.eINSTANCE));
    ExpressionFacade lFacade = new ExpressionFacade(lContext);
    Map<String, Object> lVariables = Map.of("library", LIBRARY_PACKAGE);

    assertEquals(List.of("Library", "Item", "Book", "Genre"), lFacade.evaluate("library.eClassifiers.name", lVariables));
    assertEquals(List.of("title", "pages", "genre"),
        lFacade.evaluate(
            "library.eClassifiers.typeSelect(ecore::EClass).selectFirst(c | c.name == 'Book').eAllStructuralFeatures.name",
            lVariables));
    assertEquals(List.of("Item"),
        lFacade.evaluate("library.eClassifiers.typeSelect(ecore::EClass).select(c | c.abstract).name", lVariables));
    // Enum literals are treated as enum values, so their name is not accessible as property.
    assertEquals(List.of("NOVEL", "POETRY", "SCIENCE"), lFacade
        .evaluate("library.eClassifiers.typeSelect(ecore::EEnum).first().eLiterals.collect(l | l.toString())", lVariables));
  }

  @Test
  void testCreateAndModifyObjects( ) {
    EObject lLibrary = readModel(MODEL);
    Object lBook = eval("let b = new library::Book : b.setTitle('Faust') -> b.setPages(512) -> library.books.add(b) -> b",
        lLibrary);
    assertInstanceOf(EObject.class, lBook);
    assertEquals("Faust", eval("library.books.last().title", lLibrary));
    assertEquals(512, eval("library.books.last().pages", lLibrary));
    assertEquals(4L, eval("library.books.size", lLibrary));
  }

  @Test
  void testWriteModel( @TempDir Path pDirectory ) {
    EObject lLibrary = readModel(MODEL);
    eval("library.setName('Renamed Library')", lLibrary);

    Path lFile = pDirectory.resolve("written.xmi");
    WorkflowContext lWorkflowContext = new WorkflowContextDefaultImpl();
    lWorkflowContext.set(SLOT, lLibrary);
    XmiWriter lWriter = new XmiWriter();
    lWriter.setModelFile(lFile.toString());
    lWriter.setInputSlot(SLOT);
    Issues lIssues = new IssuesImpl();
    lWriter.checkConfiguration(lIssues);
    lWriter.invoke(lWorkflowContext, new NullProgressMonitor(), lIssues);
    assertFalse(lIssues.hasErrors(), lIssues::toString);
    assertTrue(Files.exists(lFile));

    EObject lReloaded = readModel(lFile.toString());
    assertNotNull(lReloaded);
    assertEquals("Renamed Library", eval("library.name", lReloaded));
    assertEquals(3L, eval("library.books.size", lReloaded));
  }
}
