package org.xblackcat.rojac.gui.view.thread;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.rojac.data.Forum;
import org.xblackcat.rojac.gui.IRootPane;
import org.xblackcat.rojac.gui.view.message.AItemView;
import org.xblackcat.rojac.i18n.Messages;
import org.xblackcat.rojac.service.ProcessPacket;
import org.xblackcat.rojac.service.ServiceFactory;
import org.xblackcat.rojac.service.janus.commands.AffectedMessage;
import org.xblackcat.rojac.service.storage.IForumAH;
import org.xblackcat.rojac.service.storage.StorageException;
import org.xblackcat.rojac.util.RojacWorker;
import org.xblackcat.rojac.util.WindowsUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * @author xBlackCat
 */

public abstract class AThreadView extends AItemView {
    private static final Log log = LogFactory.getLog(TreeThreadView.class);

    protected final IThreadControl<Post> threadControl;
    protected final JLabel forumName = new JLabel();
    protected final AThreadModel<Post> model = new SortedThreadsModel();
    protected int forumId;

    protected AThreadView(IRootPane mainFrame, IThreadControl<Post> threadControl) {
        super(mainFrame);
        this.threadControl = threadControl;
    }

    protected void initializeLayout() {
        // Initialize tree
        add(forumName, BorderLayout.NORTH);

        JPanel internalPane = new JPanel(new BorderLayout(0, 0));

        JComponent threadsContainer = getThreadsContainer();
        JScrollPane sp = new JScrollPane(threadsContainer);
        internalPane.add(sp, BorderLayout.CENTER);

        JButton newThreadButton = WindowsUtils.setupImageButton("new_thread", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainFrame.editMessage(forumId, null);
            }
        }, Messages.VIEW_THREAD_BUTTON_NEW_THREAD);
        JButton prevUnreadButton = WindowsUtils.setupImageButton("prev_unread", new PreviousUnreadSelector(), Messages.VIEW_THREAD_BUTTON_PREVIOUS_UNREAD);
        JButton nextUnreadButton = WindowsUtils.setupImageButton("next_unread", new NextUnreadSelector(), Messages.VIEW_THREAD_BUTTON_NEXT_UNREAD);

        JToolBar toolbar = WindowsUtils.createToolBar(newThreadButton, null, prevUnreadButton, nextUnreadButton);
        internalPane.add(toolbar, BorderLayout.NORTH);

        add(internalPane, BorderLayout.CENTER);
    }

    protected void loadForumInfo(final int forumId) {
        this.forumId = forumId;

        executor.execute(new ForumInfoLoader(forumId));
    }

    @Override
    public void loadItem(AffectedMessage itemId) {
        int forumId = threadControl.loadThreadByItem(model, itemId);
        loadForumInfo(forumId);
    }

    @Override
    public void processPacket(ProcessPacket ids) {
        if (ids.containsForum(forumId)) {
            switch (ids.getType()) {
                case ForumsLoaded:
                    // Do nothing
                    break;
                case AddMessages:
                case UpdateMessages:
                    updateMessages(ids);
                    break;
                case SetForumRead:
                    threadControl.markForumRead(model, true);
                    break;
                case SetForumUnread:
                    threadControl.markForumRead(model, false);
                    break;
                case SetThreadRead:
                    for (AffectedMessage threadRootId : ids.getAffectedMessages(forumId)) {
                        threadControl.markThreadRead(model, threadRootId.getMessageId(), true);
                    }
                    break;
                case SetThreadUnread:
                    for (AffectedMessage threadRootId : ids.getAffectedMessages(forumId)) {
                        threadControl.markThreadRead(model, threadRootId.getMessageId(), false);
                    }
                    break;
                case SetPostRead:
                    for (AffectedMessage postId : ids.getAffectedMessages(forumId)) {
                        threadControl.markPostRead(model, postId.getMessageId(), true);
                    }
                    break;
                case SetPostUnread:
                    for (AffectedMessage postId : ids.getAffectedMessages(forumId)) {
                        threadControl.markPostRead(model, postId.getMessageId(), false);
                    }
                    break;
            }
        }
    }

    private void updateMessages(ProcessPacket ids) {
        AffectedMessage[] messageIds = (AffectedMessage[]) ArrayUtils.addAll(
                ids.getAffectedMessages(forumId),
                ids.getAffectedMessages(AffectedMessage.DEFAULT_FORUM)
        );

        Post currentPost = getSelectedItem();

        threadControl.updateItem(model, messageIds);

        selectItem(currentPost);
    }

    protected abstract void selectItem(Post post);

    protected abstract Post getSelectedItem();

    protected abstract JComponent getThreadsContainer();

    private void selectNextUnread(Post currentPost) {
        Post nextUnread = getNextUnread(currentPost, 0);
        if (nextUnread != null) {
            selectItem(nextUnread);
        }
    }

    private void selectPrevUnread(Post currentPost) {
        Post prevUnread = getPrevUnread(currentPost);
        if (prevUnread != null) {
            selectItem(prevUnread);
        }
    }

    private Post getNextUnread(Post post, int idx) {
        if (post == null) {
            post = model.getRoot();
            if (post.getSize() == 0) {
                return null;
            }
        }

        if (post.getLoadingState() == LoadingState.NotLoaded && post.isRead() == ReadStatus.ReadPartially) {
            // Has unread children but their have not loaded yet.
            threadControl.loadChildren(model, post, new LoadNextUnread());
            // Change post selection when children are loaded
            return null;
        }

        if (post.getLoadingState() == LoadingState.Loaded && idx >= post.getSize() || post.isRead() == ReadStatus.Read) {
            // All items in the subtree are read.
            // Go to parent and search again
            Post parent = post.getParent();
            if (parent != null) {
                int nextIdx = parent.getIndex(post) + 1;
                return getNextUnread(parent, nextIdx);
            } else {
                return null;
            }
        }

        int i = idx;
        while (i < post.getSize()) {
            Post p = post.getChild(i);
            switch (p.isRead()) {
                case Read:
                    // Go to next child of post
                    i++;
                    break;
                case ReadPartially:
                    switch (p.getLoadingState()) {
                        case Loaded:
                            // Go deep to next child of the child
                            i = 0;
                            post = p;
                            break;
                        case NotLoaded:
                            threadControl.loadChildren(model, p, new LoadNextUnread());
                        case Loading:
                            return null;
                    }
                    break;
                case Unread:
                    return p;
            }
        }

        // Thread has unread posts only before the selected post. Go to parent and continue the search.
        Post parent = post.getParent();
        if (parent != null) {
            int nextIdx = parent.getIndex(post) + 1;
            return getNextUnread(parent, nextIdx);
        } else {
            return null;
        }
    }

    private Post getPrevUnread(Post post) {
        try {
            if (post == null) {
                return findLastUnreadPost(model.getRoot());
            }

            Post parent = post.getParent();
            if (parent == null) {
                return null;
            }

            int idx = parent.getIndex(post) - 1;

            while (idx >= 0) {
                Post p = parent.getChild(idx);
                switch (p.isRead()) {
                    case Read:
                        idx--;
                        break;
                    case ReadPartially:
                    case Unread:
                        return findLastUnreadPost(p);
                }
            }

            switch (parent.isRead()) {
                case Read:
                case ReadPartially:
                    return getPrevUnread(parent);
                case Unread:
                    return parent;
            }
        } catch (RuntimeException e) {
            // Just go through to restart search later
        }

        return null;
    }

    /**
     * Searches for the last unread post in the tree thread.
     *
     * @param post root of subtree.
     *
     * @return last unread post in subtree or <code>null</code> if no unread post is exist in subtree.
     *
     * @throws RuntimeException will be thrown in case when data loading is needed to make correct search.
     */
    private Post findLastUnreadPost(Post post) throws RuntimeException {
        if (post.isRead() == ReadStatus.Read) {
            return null;
        }

        switch (post.getLoadingState()) {
            case NotLoaded:
                threadControl.loadChildren(model, post, new LoadPreviousUnread());
            case Loading:
                throw new RuntimeException("Restart search later");
        }

        int idx = post.getSize() - 1;
        while (idx >= 0) {
            Post p = findLastUnreadPost(post.getChild(idx));
            if (p != null) {
                return p;
            }
            idx --;
        }

        if (post.isRead() == ReadStatus.Unread) {
            return post;
        }

        return null;
    }

    private class ForumInfoLoader extends RojacWorker<Void, Forum> {
        private final int forumId;

        public ForumInfoLoader(int forumId) {
            this.forumId = forumId;
        }

        @Override
        protected Void perform() throws Exception {
            IForumAH fah = ServiceFactory.getInstance().getStorage().getForumAH();

            try {
                publish(fah.getForumById(forumId));
            } catch (StorageException e) {
                log.error("Can not load forum information for forum id = " + forumId, e);
            }

            return null;
        }

        @Override
        protected void process(List<Forum> chunks) {
            for (Forum f : chunks) {
                forumName.setText(f.getForumName() + "/" + f.getShortForumName());
            }
        }
    }

    private class PreviousUnreadSelector implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Post currentPost = getSelectedItem();
            selectPrevUnread(currentPost);
        }
    }

    private class NextUnreadSelector implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Post currentPost = getSelectedItem();
            selectNextUnread(currentPost);
        }
    }

    private class LoadNextUnread implements IItemProcessor<Post> {
        @Override
        public void processItem(Post item) {
            selectNextUnread(item);
        }
    }

    private class LoadPreviousUnread implements IItemProcessor<Post> {
        @Override
        public void processItem(Post item) {
            Post prevUnread = findLastUnreadPost(item);
            if (prevUnread != null) {
                selectItem(prevUnread);
            }
        }
    }
}
