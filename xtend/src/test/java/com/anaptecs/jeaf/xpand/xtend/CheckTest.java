package com.anaptecs.jeaf.xpand.xtend;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.eclipse.emf.mwe.core.issues.Issues;
import org.eclipse.emf.mwe.core.issues.IssuesImpl;
import org.eclipse.xtend.check.CheckFacade;
import org.eclipse.xtend.expression.ExecutionContextImpl;
import org.junit.jupiter.api.Test;

public class CheckTest {
  @Test
  void testChecks( ) {
    Issues lIssues = new IssuesImpl();
    CheckFacade.checkAll("com::anaptecs::jeaf::xpand::xtend::Checks", List.of("", "abc", "Abc"),
        new ExecutionContextImpl(), lIssues);

    assertEquals(1, lIssues.getErrors().length);
    assertEquals("Name must not be empty", lIssues.getErrors()[0].getMessage());
    assertEquals("", lIssues.getErrors()[0].getElement());

    assertEquals(1, lIssues.getWarnings().length);
    assertEquals("Name should start with an upper case letter: abc", lIssues.getWarnings()[0].getMessage());
    assertEquals("abc", lIssues.getWarnings()[0].getElement());
  }
}
