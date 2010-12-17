package org.xblackcat.rojac.gui.view.model;

import org.xblackcat.rojac.data.IFavorite;
import org.xblackcat.rojac.i18n.Messages;

/**
 * @author xBlackCat
 */

public enum FavoriteType {
    Thread(Messages.Favorite_Name_UnreadTreadPosts) {
        @Override
        protected IFavorite createFavorite(Integer id, String config) {
            return new ThreadFavorite(id, config);
        }
    },
    UserPosts(Messages.Favorite_Name_UserPosts) {
        @Override
        protected IFavorite createFavorite(Integer id, String config) {
            return new UserPostFavorite(id, config);
        }
    },
    SubThread(Messages.Favorite_Name_PostResponses) {
        @Override
        protected IFavorite createFavorite(Integer id, String config) {
            return new SubThreadFavorite(id, config);
        }
    },
    UserResponses(Messages.Favorite_Name_UserResponses) {
        @Override
        protected IFavorite createFavorite(Integer id, String config) {
            return new UserResponseFavorite(id, config);
        }
    },
    Category(Messages.Favorite_Name_Category) {
        @Override
        protected IFavorite createFavorite(Integer id, String config) {
            return new CategoryFavorite(id, config);
        }
    };

    private final Messages typeName;

    FavoriteType(Messages typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName.get();
    }

    protected abstract IFavorite createFavorite(Integer id, String config);

    /**
     * Restores a favorite, basing on information
     *
     *
     * @param id
     * @param type
     * @param config
     *
     * @return
     *
     * @throws IllegalArgumentException if this favorite type is not exists
     */
    public static IFavorite restoreFavorite(int id, String type, String config) throws IllegalArgumentException {
        return valueOf(type).createFavorite(id, config);
    }
}
