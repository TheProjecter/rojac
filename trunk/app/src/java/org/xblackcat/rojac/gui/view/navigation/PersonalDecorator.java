package org.xblackcat.rojac.gui.view.navigation;

import org.xblackcat.rojac.data.MessageData;
import org.xblackcat.rojac.data.NewMessage;
import org.xblackcat.rojac.data.UnreadStatData;
import org.xblackcat.rojac.gui.theme.ReadStatusIcon;
import org.xblackcat.rojac.i18n.Message;
import org.xblackcat.rojac.service.options.Property;
import org.xblackcat.rojac.service.storage.IMiscAH;
import org.xblackcat.rojac.service.storage.INewMessageAH;
import org.xblackcat.rojac.service.storage.IStatisticAH;
import org.xblackcat.rojac.service.storage.Storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * @author xBlackCat Date: 22.07.11
 */
class PersonalDecorator extends ADecorator {
    private final GroupItem<PersonalItem> personal = new GroupItem<>(Message.View_Navigation_Item_Personal, ReadStatusIcon.Personal);

    private final PersonalItem outBox = new OutboxItem();
    private final PersonalItem myResponses = new MyResponsesItem();
    private final PersonalItem ignoredTopics = new IgnoredTopicsItem();

    PersonalDecorator(IModelControl modelControl) {
        super(modelControl);

        personal.add(outBox);
        personal.add(myResponses);
        personal.add(ignoredTopics);
    }

    @Override
    AnItem[] getItemsList() {
        return new AnItem[]{
                personal
        };
    }

    public Collection<ALoadTask> reloadInfo(boolean fullReload) {
        Collection<ALoadTask> tasks = new LinkedList<>();
        Integer userId = Property.RSDN_USER_ID.get();
        if (userId != null) {
            tasks.add(new AnswersReloadTask(userId));
        }

        if (fullReload) {
            tasks.add(new OutboxReloadTask());
        }

        return tasks;
    }

    public Collection<OutboxReloadTask> reloadOutbox() {
        return Collections.singleton(new OutboxReloadTask());
    }

    public Collection<IgnoredReloadTask> reloadIgnored() {
        return Collections.singleton(new IgnoredReloadTask());
    }

    public Collection<ALoadTask> alterReadStatus(MessageData post, boolean read) {
        Collection<ALoadTask> tasks = new ArrayList<>(4);
        if (post.getParentUserId() == Property.RSDN_USER_ID.get()) {
            tasks.add(new AdjustResponsesTask(myResponses, read ? -1 : 1));
        }

        return tasks;
    }

    private class AdjustResponsesTask extends AnAdjustUnreadTask<PersonalItem> {
        public AdjustResponsesTask(PersonalItem item, int adjustDelta) {
            super(item, adjustDelta);
        }

        @Override
        void doSwing(Void data) {
            UnreadStatData stat = item.getStat();
            int unread = stat.getUnread() + adjustDelta;
            if (unread < 0) {
                unread = 0;
            } else if (unread > stat.getTotal()) {
                unread = stat.getTotal();
            }

            final UnreadStatData newStat = new UnreadStatData(unread, stat.getTotal());
            item.setStat(newStat);

            modelControl.itemUpdated(item);
        }
    }

    private class AnswersReloadTask extends ALoadTask<UnreadStatData> {
        private final int userId;

        private AnswersReloadTask(int userId) {
            this.userId = userId;
        }

        @Override
        UnreadStatData doBackground() throws Exception {
            IStatisticAH statisticAH = Storage.get(IStatisticAH.class);
            return statisticAH.getUserRepliesStat(userId);
        }

        @Override
        void doSwing(UnreadStatData data) {
            myResponses.setStat(data);
            modelControl.itemUpdated(myResponses);
        }
    }

    private class OutboxReloadTask extends ALoadTask<UnreadStatData> {
        @Override
        UnreadStatData doBackground() throws Exception {
            Collection<NewMessage> newMessages = Storage.get(INewMessageAH.class).getAllNewMessages();
            int toSend = 0;
            for (NewMessage nm : newMessages) {
                if (!nm.isDraft()) {
                    ++toSend;
                }
            }
            
            return new UnreadStatData(toSend, newMessages.size());
        }

        @Override
        void doSwing(UnreadStatData data) {
            if (data != null) {
                outBox.setStat(data);

                modelControl.itemUpdated(outBox);
            }
        }
    }

    private class IgnoredReloadTask extends ALoadTask<Integer> {
        @Override
        Integer doBackground() throws Exception {
            return Storage.get(IMiscAH.class).getAmountOfIgnoredTopics();
        }

        @Override
        void doSwing(Integer data) {
            if (data != null) {
                ignoredTopics.setStat(new UnreadStatData(0, data));

                modelControl.itemUpdated(ignoredTopics);
            }
        }
    }
}
