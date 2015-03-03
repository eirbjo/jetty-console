package org.simplericity.jettyconsole.io;

import java.io.OutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

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

/**
 * OutputStream implementation that will write to a number of underlying
 * OutputStream.
 */
public class MultiOutputStream extends OutputStream {

    private List outputStreams = new ArrayList();

    public MultiOutputStream(OutputStream stream) {
        outputStreams.add(stream);
    }

    public void addOutputStream(OutputStream stream) {
        outputStreams.add(stream);
    }

    public void write(int i) throws IOException {
        for (int j = 0; j < outputStreams.size(); j++) {
            OutputStream stream = (OutputStream) outputStreams.get(j);
            stream.write(i);
        }
    }

    public void write(byte[] bytes) throws IOException {
        for (int i = 0; i < outputStreams.size(); i++) {
            OutputStream stream = (OutputStream) outputStreams.get(i);
            stream.write(bytes);
        }
    }

    public void write(byte[] bytes, int i, int i1) throws IOException {
        for (int j = 0; j < outputStreams.size(); j++) {
            OutputStream stream = (OutputStream) outputStreams.get(j);
            stream.write(bytes, i, i1);
        }
    }

    public void flush() throws IOException {
        for (int i = 0; i < outputStreams.size(); i++) {
            OutputStream stream = (OutputStream) outputStreams.get(i);
            stream.flush();
        }
    }

    public void close() throws IOException {
        for (int i = 0; i < outputStreams.size(); i++) {
            OutputStream stream = (OutputStream) outputStreams.get(i);
            stream.close();
        }
    }
}
