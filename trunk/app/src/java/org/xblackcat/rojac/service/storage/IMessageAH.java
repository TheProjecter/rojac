package org.xblackcat.rojac.service.storage;

import org.xblackcat.rojac.data.MessageData;
import org.xblackcat.rojac.data.ThreadStatData;
import ru.rsdn.Janus.JanusMessageInfo;

/**
 * @author ASUS
 */

public interface IMessageAH extends AH {
    /**
     * Stores a message record by id.
     *
     * @param fm   message data to be stored into database.
     * @param read read state of the message. <code>true</code> - message is read and <code>false</code> otherwise.
     *
     * @throws StorageException
     */
    void storeMessage(JanusMessageInfo fm, boolean read) throws StorageException;

    boolean removeForumMessage(int id) throws StorageException;

    /**
     * Loads a message body for specified message id.
     *
     * @param messageId id of message body to be loaded.
     *
     * @return the message body.
     *
     * @throws StorageException
     */
    String getMessageBodyById(int messageId) throws StorageException;

    /**
     * Returns a list of topics which roots have not loaded yet.
     *
     * @return topic ids to have been loaded.
     *
     * @throws StorageException
     */
    int[] getBrokenTopicIds() throws StorageException;

    /**
     * Updates a message record by id.
     *
     * @param mes  message data to be stored into database.
     * @param read read state of the message. <code>true</code> - message is read and <code>false</code> otherwise.
     *
     * @throws StorageException
     */
    void updateMessage(JanusMessageInfo mes, boolean read) throws StorageException;

    /**
     * Updates read flag of the specified message.
     *
     * @param messageId message id to be updated
     * @param read      new state of flag.
     *
     * @throws StorageException
     */
    void updateMessageReadFlag(int messageId, boolean read) throws StorageException;

    /**
     * Checks if a message with specified id is exists.
     *
     * @param messageId message id to check
     *
     * @return <code>true</code> if message already loaded and <code>false</code> elsewise.
     *
     * @throws StorageException
     */
    boolean isExist(int messageId) throws StorageException;

    /**
     * Loads messages of the specified thread.
     *
     * @param threadId
     * @param forumId
     *
     * @return array of messages data.
     *
     * @throws StorageException
     */
    MessageData[] getMessagesDataByTopicId(int threadId, int forumId) throws StorageException;

    /**
     * Returns messages data for specified forum id.
     *
     * @param forumId target forum id
     *
     * @return array of topic messages data.
     *
     * @throws StorageException
     */
    MessageData[] getTopicMessagesDataByForumId(int forumId) throws StorageException;

    ThreadStatData getThreadStatByThreadId(int forumId) throws StorageException;

    int getUnreadReplaysInThread(int threadId) throws StorageException;

    MessageData getMessageData(int messageId) throws StorageException;

    int getUnreadMessages() throws StorageException;

    int getUnreadReplies(int userId) throws StorageException;

    int getUnreadUserPosts(int userId) throws StorageException;

    void updateThreadReadFlag(int messageId, boolean read) throws StorageException;

    void updateMessageRatingCache(int id, String ratingsCache) throws StorageException;
}
