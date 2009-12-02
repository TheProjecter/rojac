package org.xblackcat.rojac.service.storage;

import org.xblackcat.rojac.data.Message;
import ru.rsdn.Janus.JanusMessageInfo;

/**
 * @author ASUS
 */

public interface IMessageAH extends AH {
    void storeMessage(JanusMessageInfo fm) throws StorageException;

    boolean removeForumMessage(int id) throws StorageException;

    Message getMessageById(int messageId) throws StorageException;

    String getMessageBodyById(int messageId) throws StorageException;

    int[] getMessageIdsByParentId(int parentMessageId) throws StorageException;

    int[] getMessageIdsByTopicId(int topicId) throws StorageException;

    int[] getAllMessageIds() throws StorageException;

    int[] getTopicMessageIdsByForumId(int forumId) throws StorageException;

    int[] getBrokenTopicIds() throws StorageException;

    /**
     * Updates a message record by id.
     *
     * @param mes
     *
     * @throws StorageException
     */
    void updateMessage(JanusMessageInfo mes) throws StorageException;

    void updateMessageRecentDate(int messageId, long recentDate) throws StorageException;

    void updateMessageReadFlag(int messageId, boolean read) throws StorageException;

    boolean isExist(int messageId) throws StorageException;

    int getParentIdByMessageId(int messageId) throws StorageException;
}
