package org.xblackcat.rojac.service.janus.commands;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.rojac.data.Version;
import org.xblackcat.rojac.data.VersionType;
import org.xblackcat.rojac.i18n.Message;
import org.xblackcat.rojac.service.janus.IJanusService;
import org.xblackcat.rojac.service.janus.JanusServiceException;
import org.xblackcat.rojac.service.janus.data.NewData;
import org.xblackcat.rojac.service.storage.StorageException;
import ru.rsdn.Janus.RequestForumInfo;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import static org.xblackcat.rojac.service.options.Property.SYNCHRONIZER_LOAD_MESSAGES_PORTION;

/**
 * @author xBlackCat
 */

class GetNewPostsRequest extends LoadExtraMessagesRequest {
    private static final Log log = LogFactory.getLog(GetNewPostsRequest.class);

    protected int loadData(IProgressTracker tracker, IJanusService janusService) throws StorageException, RsdnProcessorException {
        int[] forumIds = forumAH.getSubscribedForumIds();

        String idsList = Arrays.toString(forumIds);
        tracker.addLodMessage(Message.Synchronize_Command_Name_NewPosts, idsList);
        if (ArrayUtils.isEmpty(forumIds)) {
            if (log.isWarnEnabled()) {
                log.warn("You should select at least one forum to start synchronization.");
            }
            return 0;
        }

        if (log.isDebugEnabled()) {
            log.debug("Load new messages for forums [id=" + idsList + "]");
        }

        Collection<RequestForumInfo> forumInfo = new LinkedList<RequestForumInfo>();
        Map<Integer, Number> messagesInForums = forumAH.getMessagesInForums(forumIds);
        for (int forumId : forumIds) {
            forumInfo.add(new RequestForumInfo(forumId, messagesInForums.get(forumId).intValue() == 0));
        }

        Integer limit = SYNCHRONIZER_LOAD_MESSAGES_PORTION.get();

        tracker.addLodMessage(Message.Synchronize_Message_Portion, limit);

        Version messagesVersion = DataHelper.getVersion(VersionType.MESSAGE_ROW_VERSION);
        Version moderatesVersion = DataHelper.getVersion(VersionType.MODERATE_ROW_VERSION);
        Version ratingsVersion = DataHelper.getVersion(VersionType.RATING_ROW_VERSION);

        int ownUserId = 0;
        int portionSize;
        do {
            if (ratingsVersion.isEmpty()) {
                ratingsVersion = moderatesVersion;
            }

            NewData data;
            try {
                data = janusService.getNewData(
                        forumInfo.toArray(new RequestForumInfo[forumInfo.size()]), ratingsVersion,
                        messagesVersion,
                        moderatesVersion,
                        ArrayUtils.EMPTY_INT_ARRAY,
                        ArrayUtils.EMPTY_INT_ARRAY,
                        limit
                );
            } catch (JanusServiceException e) {
                throw new RsdnProcessorException("Can not load new portion of data", e);
            }

            if (ownUserId == 0) {
                ownUserId = data.getOwnUserId();
            }

            portionSize = data.getMessages().length;

            storeNewPosts(tracker, data);

            ratingsVersion = data.getRatingRowVersion();
            messagesVersion = data.getForumRowVersion();
            moderatesVersion = data.getModerateRowVersion();

            DataHelper.setVersion(VersionType.MESSAGE_ROW_VERSION, messagesVersion);
            DataHelper.setVersion(VersionType.MODERATE_ROW_VERSION, moderatesVersion);
            DataHelper.setVersion(VersionType.RATING_ROW_VERSION, ratingsVersion);

        } while (portionSize == limit);

        super.loadData(tracker, janusService);

        return ownUserId;
    }
}
