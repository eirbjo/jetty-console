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

package org.simplericity.jettyconsole.creator;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: bjorsnos
 * Date: Dec 28, 2008
 * Time: 12:40:40 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Creator {
    void setWorkingDirectory(File workingDirectory);

    void setSourceWar(File file);

    void setBackgroundImage(URL url);

    void setDestinationWar(File destinationFile);

    void create() throws CreatorExecutionException;

    void setName(String name);

    void setAdditionalDependecies(List<URL> additionalDependencies);

    void setCoreDependency(URL coreDependencyUrl);

    void setProperties(String properties);

    void setManifestEntries(Map<String, String> manifestEntries);
}
