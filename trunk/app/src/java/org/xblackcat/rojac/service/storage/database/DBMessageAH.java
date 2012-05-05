package org.xblackcat.rojac.service.storage.database;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;
import org.xblackcat.rojac.data.AffectedMessage;
import org.xblackcat.rojac.data.ItemStatisticData;
import org.xblackcat.rojac.data.MessageData;
import org.xblackcat.rojac.data.Role;
import org.xblackcat.rojac.service.datahandler.SetReadExPacket;
import org.xblackcat.rojac.service.storage.IBatchTracker;
import org.xblackcat.rojac.service.storage.IMessageAH;
import org.xblackcat.rojac.service.storage.IResult;
import org.xblackcat.rojac.service.storage.StorageException;
import org.xblackcat.rojac.service.storage.database.convert.Converters;
import ru.rsdn.Janus.JanusMessageInfo;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author ASUS
 */

final class DBMessageAH extends AnAH implements IMessageAH {
    DBMessageAH(IQueryHolder helper) {
        super(helper);
    }

    public void storeMessage(JanusMessageInfo fm, boolean read) throws StorageException {
        helper.update(DataQuery.STORE_OBJECT_MESSAGE,
                fm.getMessageId(),
                fm.getTopicId(),
                fm.getParentId(),
                fm.getUserId(),
                fm.getForumId(),
                fm.getArticleId(),
                fm.getUserTitleColor(),
                Role.getUserType(fm.getUserRole()).ordinal(),
                fm.getMessageDate().getTimeInMillis(),
                fm.getUpdateDate().getTimeInMillis(),
                fm.getLastModerated().getTimeInMillis(),
                fm.getSubject(),
                fm.getMessageName(),
                fm.getUserNick(),
                fm.getUserTitle(),
                fm.getMessage(),
                read,
                fm.getParentId()
        );
    }

    public String getMessageBodyById(int messageId) throws StorageException {
        return helper.executeSingle(Converters.TO_STRING, DataQuery.GET_OBJECT_MESSAGE_BODY, messageId);
    }

    public int[] getBrokenTopicIds() throws StorageException {
        return helper.getIds(DataQuery.GET_BROKEN_TOPIC_IDS);
    }

    public void updateMessage(JanusMessageInfo m, boolean read) throws StorageException {
        helper.update(DataQuery.UPDATE_OBJECT_MESSAGE,
                m.getTopicId(),
                m.getParentId(),
                m.getUserId(),
                m.getForumId(),
                m.getArticleId(),
                m.getUserTitleColor(),
                Role.getUserType(m.getUserRole()).ordinal(),
                m.getMessageDate().getTimeInMillis(),
                m.getUpdateDate().getTimeInMillis(),
                m.getLastModerated().getTimeInMillis(),
                m.getSubject(),
                m.getMessageName(),
                m.getUserNick(),
                m.getUserTitle(),
                m.getMessage(),
                read,
                m.getMessageId());
    }

    @Override
    public void updateLastPostInfo(IBatchTracker tracker, TIntHashSet threadIds) throws StorageException {
        Collection<Object[]> params = new ArrayList<>();

        TIntIterator iterator = threadIds.iterator();
        while (iterator.hasNext()) {
            params.add(new Integer[]{iterator.next()});
        }

        tracker.setBatch(0, 3);
        helper.updateBatch(DataQuery.UPDATE_TOPIC_MESSAGES_SET_REPLIES_AMOUNT, tracker, params);

        tracker.setBatch(1, 3);
        helper.updateBatch(DataQuery.UPDATE_TOPIC_MESSAGES_SET_LASTPOST_ID, tracker, params);

        tracker.setBatch(2, 3);
        helper.updateBatch(DataQuery.UPDATE_TOPIC_MESSAGES_SET_LASTPOST_DATE, tracker, params);
    }

    public void updateMessageReadFlag(int messageId, boolean read) throws StorageException {
        helper.update(DataQuery.UPDATE_MESSAGE_READ_FLAG,
                read,
                messageId);
    }

    public boolean isExist(int messageId) throws StorageException {
        return helper.executeSingle(Converters.TO_BOOLEAN,
                DataQuery.IS_MESSAGE_EXISTS,
                messageId);
    }

    @Override
    public IResult<MessageData> getMessagesDataByTopicId(int threadId, int forumId) throws StorageException {
        return helper.execute(
                Converters.TO_MESSAGE_DATA,
                DataQuery.GET_OBJECTS_MESSAGE_DATA,
                threadId,
                forumId);
    }

    @Override
    public int[] getLatestTopics(int limit) throws StorageException {
        return helper.getIds(
                DataQuery.GET_LATEST_TOPIC_LIST,
                limit
        );
    }

    @Override
    public org.xblackcat.rojac.service.storage.IResult<ItemStatisticData<MessageData>> getTopicMessagesDataByForumId(int forumId, int userId) throws StorageException {
        return helper.execute(
                Converters.TO_THREAD_DATA,
                DataQuery.GET_TOPIC_MESSAGE_DATA_BY_FORUM_ID,
                userId,
                forumId);
    }

    @Override
    public IResult<MessageData> getUserPosts(int userId) throws StorageException {
        return helper.execute(
                Converters.TO_MESSAGE_DATA,
                DataQuery.GET_OBJECTS_MESSAGE_DATA_USER_POSTS,
                userId);
    }

    @Override
    public IResult<MessageData> getUserReplies(int userId) throws StorageException {
        return helper.execute(
                Converters.TO_MESSAGE_DATA,
                DataQuery.GET_OBJECTS_MESSAGE_DATA_USER_REPLIES,
                userId);
    }

    @Override
    public MessageData getMessageData(int messageId) throws StorageException {
        return helper.executeSingle(Converters.TO_MESSAGE_DATA, DataQuery.GET_OBJECT_MESSAGE_DATA, messageId);
    }

    @Override
    public void updateThreadReadFlag(int topicId, boolean read) throws StorageException {
        helper.update(DataQuery.UPDATE_MESSAGE_READ_FLAG,
                read,
                topicId);
        helper.update(DataQuery.UPDATE_THREAD_READ_FLAG,
                read,
                topicId);
    }

    @Override
    public void updateMessageRatingCache(int id, String ratingsCache) throws StorageException {
        helper.update(DataQuery.UPDATE_MESSAGE_RATING_CACHE,
                ratingsCache,
                id);
    }


    public SetReadExPacket setThreadReadBeforeDate(long dateline, boolean read, int forumId, int threadId) throws StorageException {
        SetReadExPacket result;
        try (IResult<AffectedMessage> toUpdate = helper.execute(
                Converters.TO_AFFECTED_MESSAGE_CONVERTER,
                DataQuery.GET_TOPIC_MESSAGES_READ_FLAG_BEFORE,
                read,
                dateline,
                forumId,
                threadId,
                threadId
        )) {
            result = new SetReadExPacket(read, toUpdate);
        }

        helper.update(DataQuery.UPDATE_TOPIC_MESSAGES_READ_FLAG_BEFORE, read, dateline, forumId, threadId, threadId);

        return result;
    }

    @Override
    public SetReadExPacket setThreadReadAfterDate(long dateline, boolean read, int forumId, int threadId) throws StorageException {
        SetReadExPacket result;
        try (IResult<AffectedMessage> toUpdate = helper.execute(
                Converters.TO_AFFECTED_MESSAGE_CONVERTER,
                DataQuery.GET_TOPIC_MESSAGES_READ_FLAG_AFTER,
                read,
                dateline,
                forumId,
                threadId,
                threadId
        )) {
            result = new SetReadExPacket(read, toUpdate);
        }

        helper.update(DataQuery.UPDATE_TOPIC_MESSAGES_READ_FLAG_AFTER, read, dateline, forumId, threadId, threadId);

        return result;
    }

    @Override
    public SetReadExPacket setForumReadBeforeDate(long dateline, boolean read, int forumId) throws StorageException {
        SetReadExPacket result;
        try (IResult<AffectedMessage> toUpdate = helper.execute(
                Converters.TO_AFFECTED_MESSAGE_CONVERTER,
                DataQuery.GET_FORUM_MESSAGES_READ_FLAG_BEFORE,
                read,
                dateline,
                forumId
        )) {
            result = new SetReadExPacket(read, toUpdate);
        }

        helper.update(DataQuery.UPDATE_FORUM_MESSAGES_READ_FLAG_BEFORE, read, dateline, forumId);

        return result;
    }

    @Override
    public SetReadExPacket setForumReadAfterDate(long dateline, boolean read, int forumId) throws StorageException {
        SetReadExPacket result;

        try (IResult<AffectedMessage> toUpdate = helper.execute(
                Converters.TO_AFFECTED_MESSAGE_CONVERTER,
                DataQuery.GET_FORUM_MESSAGES_READ_FLAG_AFTER,
                read,
                dateline,
                forumId
        )) {
            result = new SetReadExPacket(read, toUpdate);
        }

        helper.update(DataQuery.UPDATE_FORUM_MESSAGES_READ_FLAG_AFTER, read, dateline, forumId);

        return result;
    }

    @Override
    public SetReadExPacket setReadBeforeDate(long dateline, boolean read) throws StorageException {
        SetReadExPacket result;
        try (IResult<AffectedMessage> toUpdate = helper.execute(
                Converters.TO_AFFECTED_MESSAGE_CONVERTER,
                DataQuery.GET_MESSAGES_READ_FLAG_BEFORE,
                read,
                dateline
        )) {
            result = new SetReadExPacket(read, toUpdate);
        }

        helper.update(DataQuery.UPDATE_MESSAGES_READ_FLAG_BEFORE, read, dateline);

        return result;
    }

    @Override
    public SetReadExPacket setReadAfterDate(long dateline, boolean read) throws StorageException {
        SetReadExPacket result;
        try (IResult<AffectedMessage> toUpdate = helper.execute(
                Converters.TO_AFFECTED_MESSAGE_CONVERTER,
                DataQuery.GET_MESSAGES_READ_FLAG_AFTER,
                read,
                dateline
        )) {
            result = new SetReadExPacket(read, toUpdate);
        }

        helper.update(DataQuery.UPDATE_MESSAGES_READ_FLAG_AFTER, read, dateline);

        return result;
    }

    @Override
    public IResult<MessageData> getIgnoredTopicsList() throws StorageException {
        return helper.execute(
                Converters.TO_MESSAGE_DATA,
                DataQuery.GET_IGNORED_TOPIC_MESSAGE_DATA
        );
    }
}
