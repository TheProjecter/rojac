package org.xblackcat.rojac.gui.theme;

/**
 * @author xBlackCat
 */

public final class IconPack {
    private final String name;
    private final String pathPrefix;

    public IconPack(String name, String pathPrefix) {
        this.name = name;
        this.pathPrefix = pathPrefix;
    }

    public String getName() {
        return name;
    }

    public String getPathPrefix() {
        return pathPrefix;
    }
}
