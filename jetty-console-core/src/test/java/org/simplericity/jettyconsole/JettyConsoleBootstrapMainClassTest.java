/*
 * Copyright 2015 Eirik Bjørsnøs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
