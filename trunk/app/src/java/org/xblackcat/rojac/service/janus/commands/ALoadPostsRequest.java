package org.xblackcat.rojac.service.janus.commands;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectProcedure;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.rojac.data.Message;
import org.xblackcat.rojac.data.Moderate;
import org.xblackcat.rojac.data.Rating;
import org.xblackcat.rojac.i18n.Messages;
import org.xblackcat.rojac.service.ServiceFactory;
import org.xblackcat.rojac.service.janus.data.TopicMessages;
import org.xblackcat.rojac.service.storage.*;

/**
 * @author xBlackCat
 */

abstract class ALoadPostsRequest extends ARequest {
    private static final Log log = LogFactory.getLog(ALoadPostsRequest.class);
    /**
     * Placeholder of updated message ids set.
     */
    protected final TIntHashSet processedMessages = new TIntHashSet();
    /**
     * Placeholder of updated forum ids set.
     */
    protected final TIntHashSet affectedForums = new TIntHashSet();
    protected final IRatingAH rAH;
    protected final IMessageAH mAH;
    protected final IModerateAH modAH;
    protected final IMiscAH miscAH;
    protected final IForumAH forumAH;

    private final TIntHashSet loadedMessages = new TIntHashSet();
    protected final TIntObjectHashMap<Long> messageDates = new TIntObjectHashMap<Long>();
    protected final TIntObjectProcedure<Long> updater = new TIntObjectProcedure<Long>() {
        @Override
        public boolean execute(int a, Long b) {
            try {
                mAH.updateMessageRecentDate(a, b);
            } catch (StorageException e) {
                log.warn("Can not update recent date for message [id=" + a + "]", e);
            }
            return true;
        }
    };

    public ALoadPostsRequest() {
        IStorage storage = ServiceFactory.getInstance().getStorage();        
        modAH = storage.getModerateAH();
        mAH = storage.getMessageAH();
        rAH = storage.getRatingAH();
        miscAH = storage.getMiscAH();
        forumAH = storage.getForumAH();
    }

    protected void storeNewPosts(IProgressTracker tracker, TopicMessages newPosts) throws StorageException {
        tracker.addLodMessage(Messages.SYNCHRONIZE_COMMAND_UPDATE_DATABASE);

        int count = 0;
        for (Message mes : newPosts.getMessages()) {
            tracker.updateProgress(count++, newPosts.getTotalRecords());
            int mId = mes.getMessageId();
            if (mAH.isExist(mId)) {
                mAH.updateMessage(mes);
            } else {
                mAH.storeMessage(mes);
            }

            loadedMessages.add(mId);

            long mesDate = mes.getMessageDate();
            messageDates.put(mId, mesDate);

            int topicId = mes.getTopicId();
            if (topicId != 0) {
                Long date;

                // Update topic dates
                addMessageDate(topicId, mesDate);

                // Update parent dates
                int parentId = mes.getParentId();
                addMessageDate(parentId, mesDate);

                processedMessages.add(parentId);
                processedMessages.add(topicId);
            }

            processedMessages.add(mId);
            int forumId = mes.getForumId();
            if (forumId > 0) {
                affectedForums.add(forumId);
            }
        }

        for (Moderate mod : newPosts.getModerates()) {
            tracker.updateProgress(count++, newPosts.getTotalRecords());
            modAH.storeModerateInfo(mod);
            processedMessages.add(mod.getMessageId());
            int forumId = mod.getForumId();
            if (forumId > 0) {
                affectedForums.add(forumId);
            }
        }
        for (Rating r : newPosts.getRatings()) {
            tracker.updateProgress(count++, newPosts.getTotalRecords());
            rAH.storeRating(r);
            processedMessages.add(r.getMessageId());
        }
    }

    protected final void postprocessingMessages() throws StorageException {
        updateParentsDate(loadedMessages.toArray());

        if (log.isDebugEnabled()) {
            log.debug("There are " + messageDates.size() + " messages to process");
        }

        // Store new dates
        messageDates.forEachEntry(updater);
    }

    private void addMessageDate(int parentId, long mesDate) {
        Long date;
        date = messageDates.get(parentId);
        if (date == null) {
            messageDates.put(parentId, mesDate);
        } else {
            if (mesDate > date) {
                messageDates.put(parentId, mesDate);
            }
        }
    }

    /**
     * Updates resentChildDate field for parents of a new messages.
     *
     * @param messageIds trove hash map with pairs of &lt;messageId, date&gt;
     *
     * @return an array of affected messages.
     */
    private void updateParentsDate(int[] messageIds) throws StorageException {
        TIntHashSet parents = new TIntHashSet();

        for (int mId : messageIds) {
            if (!messageDates.containsKey(mId)) {
                // Message id is absent
                continue;
            }

            int pmId = mAH.getParentIdByMessageId(mId);

            if (pmId == 0) {
                // Top level
                continue;
            }

            Long date = messageDates.get(mId);

            addMessageDate(pmId, date);

            parents.add(pmId);
            processedMessages.add(pmId);
        }

        if (!parents.isEmpty()) {
            updateParentsDate(parents.toArray());
        }
    }
}
