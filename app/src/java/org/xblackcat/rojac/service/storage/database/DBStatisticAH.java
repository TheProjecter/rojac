package org.xblackcat.rojac.service.storage.database;

import org.xblackcat.rojac.data.ReadStatistic;
import org.xblackcat.rojac.data.ThreadStatData;
import org.xblackcat.rojac.service.storage.IStatisticAH;
import org.xblackcat.rojac.service.storage.StorageException;
import org.xblackcat.rojac.service.storage.database.convert.Converters;

/**
 * @author xBlackCat
 */
final class DBStatisticAH extends AnAH implements IStatisticAH {
    DBStatisticAH(IQueryHolder helper) {
        super(helper);
    }

    @Override
    public ThreadStatData getThreadStatByThreadId(int threadId) throws StorageException {
        return helper.executeSingle(Converters.TO_THREAD_DATA, DataQuery.GET_THREAD_STAT_DATA, threadId);
    }

    @Override
    public ReadStatistic getReplaysInThread(int threadId, int userId) throws StorageException {
        return helper.executeSingle(Converters.TO_READ_STATISTIC, DataQuery.GET_MESSAGES_STATISTIC_IN_THREAD, userId, threadId);
//        // T ODO: make single request instead of triple
//        int unreadReplies = helper.executeSingle(Converters.TO_NUMBER, DataQuery.GET_UNREAD_REPLIES_NUMBER_IN_THREAD, threadId, userId).intValue();
//        int unread = helper.executeSingle(Converters.TO_NUMBER, DataQuery.GET_UNREAD_MESSAGES_NUMBER_IN_THREAD, threadId).intValue();
//        int total = helper.executeSingle(Converters.TO_NUMBER, DataQuery.GET_MESSAGES_NUMBER_IN_THREAD, threadId).intValue();
//        return new ReadStatistic(unreadReplies, unread, total);
    }

    @Override
    public ReadStatistic getTotals(int userId) throws StorageException {
        // TODO: make single request instead of triple
        final int messages = helper.executeSingle(Converters.TO_NUMBER, DataQuery.GET_MESSAGES_NUMBER).intValue();
        final int unreadMessages = helper.executeSingle(Converters.TO_NUMBER, DataQuery.GET_UNREAD_MESSAGES_NUMBER).intValue();
        final int unreadReplies = helper.executeSingle(Converters.TO_NUMBER, DataQuery.GET_UNREAD_USER_REPLIES_NUMBER, userId).intValue();

        return new ReadStatistic(unreadReplies, unreadMessages, messages);
    }

    @Override
    public ReadStatistic getUserRepliesStat(int userId) throws StorageException {
        // TODO: make single request instead of double
        int unread = helper.executeSingle(Converters.TO_NUMBER, DataQuery.GET_UNREAD_USER_REPLIES_NUMBER, userId).intValue();
        int total = helper.executeSingle(Converters.TO_NUMBER, DataQuery.GET_USER_REPLIES_NUMBER, userId).intValue();
        return new ReadStatistic(0, unread, total);
    }

    @Override
    public ReadStatistic getUserPostsStat(int userId) throws StorageException {
        // TODO: make single request instead of double
        int unread = helper.executeSingle(Converters.TO_NUMBER, DataQuery.GET_UNREAD_USER_POSTS_NUMBER, userId).intValue();
        int total = helper.executeSingle(Converters.TO_NUMBER, DataQuery.GET_USER_POSTS_NUMBER, userId).intValue();
        return new ReadStatistic(0, unread, total);
    }
}