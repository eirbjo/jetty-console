package org.simplericity.jettyconsole.io;

/*
 * Copyright 2007 Eirik Bjorsnos.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import javax.swing.*;
import java.io.OutputStream;
import java.io.IOException;

/**
 * OutputStream implementation that will write to a JTextArea.
 */
public class JTextAreaOutputStream extends OutputStream {

    private JTextArea text;

    public JTextAreaOutputStream(JTextArea text) {
        this.text = text;
    }

    public void write(int i) throws IOException {
        write(new byte[] {(byte)i}, 0,1);
    }

    public void write(byte[] bytes) throws IOException {
        write(bytes, 0, bytes.length);
    }

    public void write(final byte[] bytes, final int i, final int i1) throws IOException {

        if(bytes != null && i1 != 0) {
            final String s = new String(bytes, i, i1);

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    text.append(s);
                    text.setCaretPosition(text.getDocument().getLength());
                    int size = 100000;
                    int maxOverflow= 500;
                    int overflow = text.getDocument().getLength() - size;
                    if (overflow >= maxOverflow) {
                        text.replaceRange("", 0, overflow);
                    }
                }
            });
        }
    }
}
