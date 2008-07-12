package org.xblackcat.sunaj.service.storage.database;

import org.xblackcat.sunaj.data.Forum;
import org.xblackcat.sunaj.service.storage.IForumAH;
import org.xblackcat.sunaj.service.storage.StorageException;
import org.xblackcat.sunaj.service.storage.database.convert.Converters;

import java.util.Collection;

/**
 * Date: 8 ���� 2007
 *
 * @author ASUS
 */
final class DBForumAH implements IForumAH {
    private final IQueryExecutor helper;

    DBForumAH(IQueryExecutor helper) {
        this.helper = helper;
    }

    public void storeForum(Forum f) throws StorageException {
        helper.update(DataQuery.STORE_OBJECT_FORUM,
                f.getForumId(),
                f.getForumGroupId(),
                f.getRated(),
                f.getInTop(),
                f.getRateLimit(),
                f.isSubscribed(),
                f.getShortForumName(),
                f.getForumName());
    }

    public boolean removeForum(int id) throws StorageException {
        return helper.update(DataQuery.REMOVE_OBJECT_FORUM, id) > 0;
    }

    public Forum getForumById(int forumId) throws StorageException {
        return helper.executeSingle(Converters.TO_FORUM_CONVERTER, DataQuery.GET_OBJECT_FORUM, forumId);
    }

    public int[] getForumIdsInGroup(int forumGroupId) throws StorageException {
        return helper.getIds(DataQuery.GET_IDS_FORUM_BY_FORUM_GROUP, forumGroupId);
    }

    public int[] getSubscribedForumIds() throws StorageException {
        return helper.getIds(DataQuery.GET_IDS_FORUM_SUBSCRIBED);
    }

    public void updateForum(Forum f) throws StorageException {
        helper.update(DataQuery.UPDATE_OBJECT_FORUM,
                f.getForumGroupId(),
                f.getRated(),
                f.getInTop(),
                f.getRateLimit(),
                f.getShortForumName(),
                f.getForumName(),
                f.getForumId());
    }

    public void setSubscribeForum(int forumId, boolean subscribe) throws StorageException {
        helper.update(DataQuery.UPDATE_OBJECT_FORUM_SUBSCRIBE,
                subscribe,
                forumId);
    }

    public int getMessagesInForum(int forumId) throws StorageException {
        return helper.executeSingle(Converters.TO_INTEGER_CONVERTER,
                DataQuery.GET_MESSAGES_NUMBER_IN_FORUM,
                forumId);
    }

    public Forum[] getAllForums() throws StorageException {
        Collection<Forum> fc = helper.execute(Converters.TO_FORUM_CONVERTER, DataQuery.GET_OBJECTS_FORUM);
        return fc.toArray(new Forum[fc.size()]);
    }
}
