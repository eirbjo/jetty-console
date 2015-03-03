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
