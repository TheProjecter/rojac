package org.xblackcat.rojac.data.favorite;

import org.xblackcat.rojac.service.ServiceFactory;
import org.xblackcat.rojac.service.storage.StorageException;

/**
 * @author xBlackCat
 */

class UnreadPostsInThreadFavorite extends AnItemFavorite {
    UnreadPostsInThreadFavorite(Integer id, String config) {
        super(id, config);
    }

    @Override
    public FavoriteType getFavoriteType() {
        return FavoriteType.UnreadPostsInThread;
    }

    @Override
    protected int loadAmount() throws StorageException {
        return ServiceFactory.getInstance().getStorage().getMessageAH().getUnreadReplaysInThread(itemId);
    }
}
