// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.buildinggeneralization;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * Building Generalization plugin.
 */
public class BuildingGeneralizationPlugin extends Plugin {
    
    /**
     * Constructs a new {@code BuildingGeneralizationPlugin}.
     * @param info plugin information
     */
    public BuildingGeneralizationPlugin(PluginInformation info) {
        super(info);
        MainApplication.getMenu().toolsMenu.add(new BuildingGeneralizationAction());
    }
}
