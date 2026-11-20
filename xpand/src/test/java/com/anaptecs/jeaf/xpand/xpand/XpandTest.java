package com.anaptecs.jeaf.xpand.xpand;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import org.eclipse.internal.xpand2.pr.ProtectedRegionResolverImpl;
import org.eclipse.xpand2.XpandExecutionContextImpl;
import org.eclipse.xpand2.XpandFacade;
import org.eclipse.xpand2.output.Outlet;
import org.eclipse.xpand2.output.OutputImpl;
import org.eclipse.internal.xpand2.pr.ProtectedRegionResolver;
import org.eclipse.xtend.expression.ResourceManagerDefaultImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class XpandTest {
  private static final String PACKAGE = "com::anaptecs::jeaf::xpand::xpand::";

  @TempDir
  private Path outputDirectory;

  private XpandExecutionContextImpl createContext( ProtectedRegionResolver pProtectedRegionResolver ) {
    OutputImpl lOutput = new OutputImpl();
    lOutput.addOutlet(new Outlet(outputDirectory.toString()));
    Outlet lSourceOutlet = new Outlet(outputDirectory.resolve("src").toString());
    lSourceOutlet.setName("src");
    lSourceOutlet.setOverwrite(false);
    lOutput.addOutlet(lSourceOutlet);
    ResourceManagerDefaultImpl lResourceManager = new ResourceManagerDefaultImpl();
    lResourceManager.setFileEncoding("UTF-8");
    return new XpandExecutionContextImpl(lResourceManager, lOutput, pProtectedRegionResolver, new HashMap<>(), null,
        null, null, null);
  }

  private ProtectedRegionResolver createProtectedRegionResolver( ) {
    ProtectedRegionResolverImpl lResolver = new ProtectedRegionResolverImpl();
    lResolver.setSrcPathes(outputDirectory.toString());
    lResolver.setFileEncoding("UTF-8");
    return lResolver;
  }

  private String read( String pFile ) throws IOException {
    return Files.readString(outputDirectory.resolve(pFile), StandardCharsets.UTF_8);
  }

  @Test
  void testTemplateExpansion( ) throws IOException {
    XpandFacade.create(createContext(null)).evaluate(PACKAGE + "Template::Root", List.of("a", "b", "c"));
    assertEquals("1/3:a, 2/3:b, 3/3:c (last)\n- A\n- B\n- C\n", read("result.txt"));
  }

  @Test
  void testAdvice( ) throws IOException {
    XpandExecutionContextImpl lContext = createContext(null);
    lContext.registerAdvices(PACKAGE + "Advices");
    XpandFacade.create(lContext).evaluate(PACKAGE + "Template::Root", List.of("a", "b"));
    assertEquals("1/2:a, 2/2:b (last)\n- A\n  (advised)\n- B\n  (advised)\n", read("result.txt"));
  }

  @Test
  void testProtectedRegions( ) throws IOException {
    XpandFacade.create(createContext(createProtectedRegionResolver())).evaluate(PACKAGE + "Code::Class", "Sample");
    String lGenerated = read("Sample.java");
    assertEquals("public class Sample {\n" + "  // PROTECTED REGION ID(Sample.body) ENABLED START\n"
        + "  // TODO implement\n" + "  // PROTECTED REGION END\n" + "}\n", lGenerated);

    Files.writeString(outputDirectory.resolve("Sample.java"),
        lGenerated.replace("// TODO implement", "int value = 42;"), StandardCharsets.UTF_8);

    XpandFacade.create(createContext(createProtectedRegionResolver())).evaluate(PACKAGE + "Code::Class", "Sample");
    assertEquals(lGenerated.replace("// TODO implement", "int value = 42;"), read("Sample.java"));
  }

  @Test
  void testOutletWithoutOverwrite( ) throws IOException {
    XpandFacade.create(createContext(null)).evaluate(PACKAGE + "Code::Readme", "first");
    assertEquals("first\n", read("src/README.txt"));
    XpandFacade.create(createContext(null)).evaluate(PACKAGE + "Code::Readme", "second");
    assertEquals("first\n", read("src/README.txt"));
  }
}
