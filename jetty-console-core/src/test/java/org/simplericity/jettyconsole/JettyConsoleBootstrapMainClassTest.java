package org.simplericity.jettyconsole;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 *
 */
public class JettyConsoleBootstrapMainClassTest {

    String options = "option:ajpPort\n" +
                "sample:n\n" +
                "desc:Create an AJP listener on port n (default: don't listen on AJP)\n" +
                "section:AJP\n" +
                "\n" +
                "option:headless\n" +
                "desc:Don't open graphical console, even if available\n" +
                "section:Options\n";

    List<String> lines = Arrays.asList(options.split("\n"));

    @Test
    public void shouldParseOptions() {

        List<JettyConsoleBootstrapMainClass.Option> opts = JettyConsoleBootstrapMainClass.parseOptions(lines);

        assertEquals(2, opts.size());

        JettyConsoleBootstrapMainClass.Option one = opts.get(0);

        assertEquals("ajpPort", one.getName());
        assertEquals("n", one.getSample());
        assertEquals("Create an AJP listener on port n (default: don't listen on AJP)", one.getDescription());
        assertEquals("AJP", one.getSection());


    }


}
