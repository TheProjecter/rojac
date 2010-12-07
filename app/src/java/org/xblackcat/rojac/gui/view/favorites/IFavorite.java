package org.xblackcat.rojac.gui.view.favorites;

/**
 * Base interface for hold information about favorite. It contains own renderer.
 *
 * @author xBlackCat
 */

public interface IFavorite {
    void updateStatistic(Runnable runnable);

    FavoriteType getFavoriteType();

    /**
     * Returns a favorite-specific config to store in database.
     *
     * @return parsable config string.
     */
    String getConfig();

    boolean isMarked();

    String getName();

    String getStatistic();
}
