package org.xblackcat.sunaj.service.storage.cached;

import org.xblackcat.sunaj.data.Forum;
import org.xblackcat.sunaj.service.storage.IForumAH;
import org.xblackcat.sunaj.service.storage.StorageException;

/**
 * Date: 16.04.2007
 *
 * @author ASUS
 */
final class CachedForumAH implements IForumAH, IPurgable {
    private final Cache<Forum> forumCache = new Cache<Forum>();
    private final ObjectCache<int[]> subscribedForums = new ObjectCache<int[]>();

    private final IForumAH forumDAO;

    CachedForumAH(IForumAH forumDAO) {
        this.forumDAO = forumDAO;
    }

    public void storeForum(Forum f) throws StorageException {
        forumDAO.storeForum(f);
    }

    public boolean removeForum(int id) throws StorageException {
        if (forumDAO.removeForum(id)) {
            forumCache.remove(id);
            return true;
        } else {
            return false;
        }
    }

    public Forum getForumById(int forumId) throws StorageException {
        Forum forum = forumCache.get(forumId);
        if (forum == null) {
            forum = forumDAO.getForumById(forumId);
            if (forum != null) {
                forumCache.put(forumId, forum);
            }
        }
        return forum;
    }

    public int[] getForumIdsInGroup(int forumGroupId) throws StorageException {
        return forumDAO.getForumIdsInGroup(forumGroupId);
    }

    public int[] getAllForumIds() throws StorageException {
        return forumDAO.getAllForumIds();
    }

    public int[] getSubscribedForumIds() throws StorageException {
        int[] ids = subscribedForums.get();
        if (ids == null) {
            ids = forumDAO.getSubscribedForumIds();
            if (ids != null) {
                subscribedForums.put(ids);
            }
        }
        return ids;
    }

    public void purge() {
        forumCache.purge();
        subscribedForums.purge();
    }
}