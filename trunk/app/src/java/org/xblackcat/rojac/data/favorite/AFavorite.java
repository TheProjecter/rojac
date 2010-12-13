package org.xblackcat.rojac.data.favorite;

import org.xblackcat.rojac.util.RojacUtils;

/**
 * @author xBlackCat
 */

abstract class AFavorite implements IFavorite {
    protected final int id;
    private String name;

    protected AFavorite(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        assert RojacUtils.checkThread(true, getClass());
        if (name == null) {
            return "#" + id + " " + getType().getTypeName();
        } else {
            return name;
        }
    }

    protected final boolean isNameDefault() {
        return name == null;
    }

    protected void setName(String name) {
        assert RojacUtils.checkThread(true, getClass());
        this.name = name;
    }
}
