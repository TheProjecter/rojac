package org.xblackcat.rojac.gui.view.model;

import gnu.trove.set.hash.TIntHashSet;
import org.xblackcat.rojac.RojacDebugException;
import org.xblackcat.rojac.data.Forum;
import org.xblackcat.rojac.data.ForumMessageData;
import org.xblackcat.rojac.data.MessageData;
import org.xblackcat.rojac.gui.IAppControl;
import org.xblackcat.rojac.gui.OpenMessageMethod;
import org.xblackcat.rojac.gui.popup.PopupMenuBuilder;
import org.xblackcat.rojac.gui.theme.ReadStatusIcon;
import org.xblackcat.rojac.gui.view.thread.ThreadToolbarActions;
import org.xblackcat.rojac.service.datahandler.*;
import org.xblackcat.rojac.service.options.Property;
import org.xblackcat.rojac.util.RojacUtils;

import javax.swing.*;

/**
 * Control class of all threads of the specified forum.
 *
 * @author xBlackCat
 */

class SortedForumModelControl extends AThreadsModelControl {
    private static final ThreadToolbarActions[] TOOLBAR_CONFIG = new ThreadToolbarActions[]{
            ThreadToolbarActions.NewThread,
            null,
            ThreadToolbarActions.ToThreadRoot,
            ThreadToolbarActions.PreviousPost,
            ThreadToolbarActions.NextPost,
            ThreadToolbarActions.PreviousUnread,
            ThreadToolbarActions.NextUnread,
            null,
            ThreadToolbarActions.MarkSubTreeRead,
            ThreadToolbarActions.MarkThreadRead
    };

    @Override
    public void fillModelByItemId(final AThreadModel<Post> model, final int forumId) {
        assert RojacUtils.checkThread(true);

        final ForumRoot rootItem = new ForumRoot(forumId);

        model.setRoot(rootItem);

        final ForumInfoLoader infoLoader = new ForumInfoLoader(model, forumId) {
            @Override
            protected void done() {
                super.done();
                if (model.getRoot() != null) {
                    new ThreadsLoader(postProcessor, model, forumId).execute();
                }
            }
        };
        infoLoader.execute();
    }

    private void markForumRead(AThreadModel<Post> model, boolean read) {
        assert RojacUtils.checkThread(true);

        // Root post is ForumRoot object
        model.getRoot().setRead(read);

        model.subTreeNodesChanged(model.getRoot());
    }

    @Override
    public boolean isRootVisible() {
        assert RojacUtils.checkThread(true);

        return false;
    }

    private void updateModel(final Runnable postProcessor, final AThreadModel<Post> model, int... threadIds) {
        assert RojacUtils.checkThread(true);

        TIntHashSet filledThreads = new TIntHashSet();

        Post root = model.getRoot();
        // Should be ForumRoot object
        assert root instanceof ForumRoot;

        for (Post post : root.childrenPosts) {
            assert post instanceof Thread;

            if (post.getThreadRoot().isFilled()) {
                filledThreads.add(post.getThreadRoot().getMessageId());
            }
        }

        // Retain only changed filled thread to reload.
        filledThreads.retainAll(threadIds);

        // Reload filled threads.
        final int[] filledThreadIds = filledThreads.toArray();

        for (int threadId : filledThreadIds) {
            Post post = root.getMessageById(threadId);
            if (post != null) {
                // Update thread children
                final Thread t = post.getThreadRoot();

                if (t.isFilled()) {
                    // Thread is already filled - update children
                    loadThread(model, t, new Runnable() {
                        @Override
                        public void run() {
                            model.nodeStructureChanged(t);
                            if (postProcessor != null) {
                                postProcessor.run();
                            }
                        }
                    });
                } else {
                    throw new RojacDebugException("Expected filled thread #" + threadId);
                }
            } else {
                throw new RojacDebugException("Thread #" + threadId + " not found");
            }
        }

        // Reload forum threads list.
        new ThreadsLoader(postProcessor, model, root.getForumId()).execute();
    }

    @Override
    public String getTitle(AThreadModel<Post> model) {
        // Root is ForumRoot object
        Post root = model.getRoot();
        if (root != null) {
            if (root.getMessageData() != null) {
                if (root.getMessageData().getSubject() != null) {
                    return root.getMessageData().getSubject();
                }
            }
            return "#" + root.getForumId();
        }

        return "#";
    }

    @Override
    public void processPacket(final AThreadModel<Post> model, IPacket p, final Runnable postProcessor) {
        final int forumId = model.getRoot().getForumId();

        new PacketDispatcher(
                new IPacketProcessor<SetForumReadPacket>() {
                    @Override
                    public void process(SetForumReadPacket p) {
                        if (p.getForumId() == forumId) {
                            markForumRead(model, p.isRead());
                        }
                    }
                },
                new IPacketProcessor<SetSubThreadReadPacket>() {
                    @Override
                    public void process(SetSubThreadReadPacket p) {
                        if (p.getForumId() == forumId) {
                            markThreadRead(model, p.getPostId(), p.isRead());
                        }
                    }
                },
                new IPacketProcessor<SetPostReadPacket>() {
                    @Override
                    public void process(SetPostReadPacket p) {
                        if (p.getPost().getForumId() == forumId) {
                            markPostRead(model, p.getPost().getMessageId(), p.isRead());
                        }
                    }
                },
                new IPacketProcessor<SetReadExPacket>() {
                    @Override
                    public void process(SetReadExPacket p) {
                        if (!p.isForumAffected(forumId)) {
                            // Current forum is not changed - have a rest
                            return;
                        }

                        boolean newReadState = p.isRead();
                        Post root = model.getRoot();

                        // First, queue for update a not loaded threads.
                        for (int topicId : p.getThreadIds()) {
                            Post post = root.getMessageById(topicId);

                            if (post == null) {
                                // Topic from another forum - skip.
                                continue;
                            }

                            assert post instanceof Thread : post;
                            assert post.getThreadRoot() == post : post;
                            Thread topic = (Thread) post;

                            if (!topic.isFilled()) {
                                // Queue updatestat data.
                                new ThreadUnreadPostsLoader(topic, model).execute();
                            }
                        }

                        // Second - update already loaded posts.
                        for (int postId : p.getMessageIds()) {
                            Post post = root.getMessageById(postId);

                            if (post != null) {
                                post.setRead(newReadState);
                                model.pathToNodeChanged(post);
                            }
                        }
                    }
                },
                new IPacketProcessor<SynchronizationCompletePacket>() {
                    @Override
                    public void process(SynchronizationCompletePacket p) {
                        if (!p.isForumAffected(forumId)) {
                            // Current forum is not changed - have a rest
                            return;
                        }

                        updateModel(postProcessor, model, p.getThreadIds());
                    }
                },
                new IPacketProcessor<SubscriptionChangedPacket>() {
                    @Override
                    public void process(SubscriptionChangedPacket p) {
                        final Post root = model.getRoot();
                        final MessageData data = root.getMessageData();

                        if (data instanceof ForumMessageData) {
                            Forum f = ((ForumMessageData) data).getForum();

                            for (SubscriptionChangedPacket.Subscription s : p.getNewSubscriptions()) {
                                if (s.getForumId() == f.getForumId()) {
                                    f.setSubscribed(s.isSubscribed());

                                    model.nodeChanged(root);
                                    return;
                                }
                            }
                        }
                    }
                }
        ).dispatch(p);
    }

    @Override
    public Icon getTitleIcon(AThreadModel<Post> model) {
        if (model.getRoot() != null) {
            return ReadStatusIcon.Forum.getIcon(model.getRoot().isRead());
        }

        return null;
    }

    @Override
    public JPopupMenu getTitlePopup(AThreadModel<Post> model, IAppControl appControl) {
        final MessageData data = model.getRoot().getMessageData();

        if (data instanceof ForumMessageData) {
            Forum f = ((ForumMessageData) data).getForum();

            return PopupMenuBuilder.getForumViewTabMenu(f, appControl);
        }

        return null;
    }


    @Override
    public ThreadToolbarActions[] getToolbar() {
        return TOOLBAR_CONFIG;
    }

    @Override
    public void unloadThread(AThreadModel<Post> model, Post item) {
        Thread thread = (Thread) item;

        thread.clearThread();
        model.nodeStructureChanged(thread);
    }

    @Override
    public Property<OpenMessageMethod> getOpenMessageMethod() {
        return Property.OPEN_MESSAGE_BEHAVIOUR_FORUM_VIEW;
    }
}