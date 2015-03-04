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

package org.simplericity.jettyconsole.plugins.spdy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 *
 */
public class SpdyAgent {

    public static void agentmain(String options, Instrumentation instrumentation) throws Exception {


        final JarFile jarFile = new JarFile(options);

        instrumentation.appendToBootstrapClassLoaderSearch(jarFile);

        instrumentation.addTransformer(new ClassFileTransformer() {
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                if(className.startsWith("sun/security/ssl"))  {
                    ZipEntry entry = jarFile.getEntry(className + ".class");
                    if(entry != null) {
                        try {
                            return toBytes(jarFile.getInputStream(entry));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                }
                return null;
            }
        });
    }

    private static byte[] toBytes(InputStream in) {
        byte[] buffer = new byte[4096];

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
