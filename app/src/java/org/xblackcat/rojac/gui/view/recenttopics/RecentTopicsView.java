package org.xblackcat.rojac.gui.view.recenttopics;

import org.xblackcat.rojac.gui.IAppControl;
import org.xblackcat.rojac.gui.IViewLayout;
import org.xblackcat.rojac.gui.OpenMessageMethod;
import org.xblackcat.rojac.gui.PopupMouseAdapter;
import org.xblackcat.rojac.gui.popup.PopupMenuBuilder;
import org.xblackcat.rojac.gui.theme.ViewIcon;
import org.xblackcat.rojac.gui.view.AView;
import org.xblackcat.rojac.i18n.Message;
import org.xblackcat.rojac.service.datahandler.*;
import org.xblackcat.rojac.service.options.Property;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * @author xBlackCat
 */

public class RecentTopicsView extends AView {
    private final RecentThreadsModel model = new RecentThreadsModel();
    private final PacketDispatcher packetDispatcher = new PacketDispatcher(
            new IPacketProcessor<SynchronizationCompletePacket>() {
                @Override
                public void process(SynchronizationCompletePacket p) {
                    reloadLastPosts();
                }
            },
            new IPacketProcessor<OptionsUpdatedPacket>() {
                @Override
                public void process(OptionsUpdatedPacket p) {
                    if (p.isPropertyAffected(Property.VIEW_RECENT_TOPIC_LIST_SIZE)) {
                        reloadLastPosts();
                    }
                }
            },
            new IPacketProcessor<ReloadDataPacket>() {
                @Override
                public void process(ReloadDataPacket p) {
                    reloadLastPosts();
                }
            },
            new IPacketProcessor<IgnoreUpdatedPacket>() {
                @Override
                public void process(IgnoreUpdatedPacket p) {
                    reloadLastPosts();
                }
            }
    );

    public RecentTopicsView(IAppControl appControl) {
        super(appControl);
        setLayout(new BorderLayout(5, 5));

        initializeLayout();
    }

    private void reloadLastPosts() {
        if (!model.isLoading()) {
            model.clear();

            new LatestPostsLoader(model).execute();
        }
    }

    private void initializeLayout() {
        final JTable lastPostList = new JTable(model);
        lastPostList.setTableHeader(null);
        add(new JScrollPane(lastPostList));

        lastPostList.setDefaultRenderer(LastPostInfo.class, new TopicCellRenderer());

        lastPostList.addMouseListener(new PopupMouseAdapter() {
            private LastPostInfo getTopicInfo(MouseEvent e) {
                int ind = lastPostList.rowAtPoint(e.getPoint());

                return model.getValueAt(ind, 0);
            }

            @Override
            protected void triggerDoubleClick(MouseEvent e) {
                LastPostInfo info = getTopicInfo(e);
                OpenMessageMethod method = Property.OPEN_MESSAGE_BEHAVIOUR_RECENT_TOPICS.get();
                int messageId = info.getTopicRoot().getMessageId();

                appControl.openMessage(messageId, method);
            }

            @Override
            protected void triggerPopup(MouseEvent e) {
                LastPostInfo info = getTopicInfo(e);

                JPopupMenu menu = PopupMenuBuilder.getRecentPostsMenu(info.getTopicRoot(), appControl);

                final Point p = e.getPoint();
                menu.show(e.getComponent(), p.x, p.y);

            }
        });
    }

    @Override
    public IViewLayout storeLayout() {
        return null;
    }

    @Override
    public void setupLayout(IViewLayout o) {
    }

    @Override
    public String getTabTitle() {
        return Message.View_RecentTopics_Title.get();
    }

    @Override
    public Icon getTabTitleIcon() {
        return ViewIcon.RecentTopics;
    }

    @Override
    public JPopupMenu getTabTitleMenu() {
        return null;
    }

    @Override
    public final void processPacket(IPacket packet) {
        packetDispatcher.dispatch(packet);
    }
}