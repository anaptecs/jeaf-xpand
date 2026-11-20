package com.anaptecs.jeaf.xpand.uml2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;
import java.util.Map;

import org.eclipse.emf.mwe.core.WorkflowContext;
import org.eclipse.emf.mwe.core.WorkflowContextDefaultImpl;
import org.eclipse.emf.mwe.core.issues.Issues;
import org.eclipse.emf.mwe.core.issues.IssuesImpl;
import org.eclipse.emf.mwe.core.monitor.NullProgressMonitor;
import org.eclipse.uml2.uml.Model;
import org.eclipse.xtend.XtendFacade;
import org.eclipse.xtend.check.CheckFacade;
import org.eclipse.xtend.expression.ExecutionContextImpl;
import org.eclipse.xtend.expression.ExpressionFacade;
import org.eclipse.xtend.typesystem.emf.XmiReader;
import org.eclipse.xtend.typesystem.uml2.Setup;
import org.eclipse.xtend.typesystem.uml2.UML2MetaModel;
import org.eclipse.xtend.typesystem.uml2.profile.ProfileMetaModel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests the UML2 type system incl. UML profiles using the JEAF test model.
 */
public class UML2TypeSystemTest {
  private static final String MODEL_DIRECTORY = "target/test-model/xmi/";

  private static final String SLOT = "model";

  private static Model model;

  private static ExecutionContextImpl context;

  @BeforeAll
  static void loadModel( ) {
    new Setup();

    XmiReader lReader = new XmiReader();
    lReader.setModelFile(MODEL_DIRECTORY + "JEAF_Test.uml");
    lReader.setOutputSlot(SLOT);
    lReader.setFirstElementOnly(true);
    WorkflowContext lWorkflowContext = new WorkflowContextDefaultImpl();
    Issues lIssues = new IssuesImpl();
    lReader.invoke(lWorkflowContext, new NullProgressMonitor(), lIssues);
    assertFalse(lIssues.hasErrors(), lIssues::toString);
    model = assertInstanceOf(Model.class, lWorkflowContext.get(SLOT));

    ProfileMetaModel lProfileMetaModel = new ProfileMetaModel();
    lProfileMetaModel.addProfile(MODEL_DIRECTORY + "JMM.profile.uml");
    context = new ExecutionContextImpl();
    context.registerMetaModel(new UML2MetaModel());
    context.registerMetaModel(lProfileMetaModel);
  }

  private static Object eval( String pExpression ) {
    return new ExpressionFacade(context).evaluate(pExpression, Map.of("model", model));
  }

  private static String findClass( String pName ) {
    return "model.eAllContents.typeSelect(uml::Class).selectFirst(c | c.name == '" + pName + "')";
  }

  @Test
  void testModel( ) {
    assertEquals("Model", eval("model.name"));
    assertEquals(true, eval("uml::Model.isInstance(model)"));
    assertEquals(true, eval("uml::Package.isInstance(model)"));
  }

  @Test
  void testClasses( ) {
    assertEquals(true, eval(findClass("AbstractPOJO") + ".isAbstract"));
    assertEquals(false, eval(findClass("MutableChildPOJO") + ".isAbstract"));
    assertEquals(List.of("ImmutablePOJOParent"), eval(findClass("MutableChildPOJO") + ".superClass.name"));
    assertEquals(List.of("writeable", "booleanDefault"), eval(findClass("MutableChildPOJO") + ".ownedAttribute.name"));
    assertEquals(true, eval(findClass("MutableChildPOJO")
        + ".ownedAttribute.selectFirst(a | a.name == 'booleanDefault').isReadOnly"));
  }

  @Test
  void testStereotypes( ) {
    assertEquals(true, eval("JMM::POJO.isInstance(" + findClass("AbstractPOJO") + ")"));
    assertEquals(false, eval("JMM::JSONDefaultImpl.isInstance(" + findClass("AbstractPOJO") + ")"));
    assertEquals(true, eval("JMM::POJO.isInstance(" + findClass("MutableChildPOJO") + ")"));
    assertEquals(true, eval("JMM::JSONDefaultImpl.isInstance(" + findClass("MutableChildPOJO") + ")"));
    assertEquals(182L, eval("model.eAllContents.typeSelect(JMM::POJO).size"));
  }

  @Test
  void testTaggedValues( ) {
    String lProperty = "((JMM::Size) " + findClass("OtherPojo")
        + ".ownedAttribute.selectFirst(a | a.name == 'derivedString'))";
    assertEquals(1, eval(lProperty + ".min"));
    assertEquals(111, eval(lProperty + ".max"));
  }

  @Test
  void testExtensions( ) {
    XtendFacade lFacade = XtendFacade.create(context, "com::anaptecs::jeaf::xpand::uml2::Extensions");
    assertEquals("Model::com::anaptecs::jeaf::junit::pojo::MutableChildPOJO",
        lFacade.call("fullName", eval(findClass("MutableChildPOJO"))));
    assertEquals(List.of("abtractAttr"), lFacade.call("attributeNames", eval(findClass("AbstractPOJO"))));
  }

  @Test
  void testChecks( ) {
    Issues lIssues = new IssuesImpl();
    CheckFacade.checkAll("com::anaptecs::jeaf::xpand::uml2::Checks", List.of(eval(findClass("AbstractPOJO")),
        eval(findClass("MutableChildPOJO"))), context, lIssues);
    assertEquals(0, lIssues.getErrors().length);
    assertEquals(1, lIssues.getWarnings().length);
    assertEquals("Abstract POJO AbstractPOJO", lIssues.getWarnings()[0].getMessage());
  }
}
