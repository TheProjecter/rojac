package org.xblackcat.rojac.gui.view.model;

import gnu.trove.map.hash.TIntObjectHashMap;
import org.xblackcat.rojac.data.MessageData;
import org.xblackcat.rojac.data.ReadStatistic;
import org.xblackcat.rojac.service.options.Property;

import java.util.*;

/**
 * @author xBlackCat
 */

public class Thread extends Post {
    private TIntObjectHashMap<Post> threadPosts = new TIntObjectHashMap<>();
    private final static Comparator<MessageData> SORT_BY_PARENTS = new Comparator<MessageData>() {
        @Override
        public int compare(MessageData o1, MessageData o2) {
            return o1.getParentId() - o2.getParentId();
        }
    };

    // State fields
    /**
     * State flag to indicate if this thread is filled with posts or not. If the thread is filled - statistic will be
     * calculated from real posts data. A stat data from DB is user in other case.
     */
    private boolean filled = false;
    private LoadingState loadingState = LoadingState.NotLoaded;

    private long lastPostDate;
    private int replyAmount;
    private int unreadPosts;
    private int unreadReplies;

    /**
     * Aux constructor for newly created thread.
     *
     * @param messageData
     * @param parent
     */
    Thread(MessageData messageData, ForumRoot parent) {
        this(messageData, parent, messageData.getMessageDate(), 0, 0, 0);
        filled = true;
    }

    public Thread(MessageData messageData, ForumRoot parent, long lastPostDate, int replyAmount, int unreadPosts, int unreadReplies) {
        super(messageData, parent, null);
        this.lastPostDate = lastPostDate;
        this.replyAmount = replyAmount;
        this.unreadPosts = unreadPosts;
        this.unreadReplies = unreadReplies;

        threadPosts.put(messageData.getMessageId(), this);
    }

    public Thread getThreadRoot() {
        return this;
    }

    @Override
    public boolean isIgnored() {
        return messageData.isIgnored();
    }

    @Override
    public int getIndex(Post p) {
        if (!filled) {
            return -1;
        }
        return super.getIndex(p);
    }

    @Override
    public Post getChild(int idx) {
        if (filled) {
            return super.getChild(idx);
        } else {
            throw new IndexOutOfBoundsException("There is no responses on post " + messageData);
        }
    }

    @Override
    public long getLastPostDate() {
        if (filled) {
            return super.getLastPostDate();
        } else if (replyAmount == 0) {
            return messageData.getMessageDate();
        } else {
            return lastPostDate;
        }
    }

    @Override
    public int compareTo(Post o) {
        long postDate = o.getLastPostDate();
        long thisPostDate = getLastPostDate();

        return thisPostDate == postDate ? 0 : postDate > thisPostDate ? 1 : -1;
    }

    @Override
    public int getSize() {
        if (filled) {
            return super.getSize();
        } else {
            return 0;
        }
    }

    @Override
    public int getPostAmount() {
        if (filled) {
            // Do not count itself.
            return threadPosts.size() - 1;
        } else {
            return replyAmount;
        }
    }

    public void setLoadingState(LoadingState loadingState) {
        this.loadingState = loadingState;
    }

    @Override
    public LoadingState getLoadingState() {
        return !filled && replyAmount == 0 ? LoadingState.Loaded : loadingState;
    }

    @Override
    public ReadStatus isRead() {
        if (filled) {
            return super.isRead();
        } else if (getMessageData().isRead()) {
            if (unreadPosts > 0) {
                return ReadStatus.ReadPartially;
            } else {
                return ReadStatus.Read;
            }
        } else {
            return ReadStatus.Unread;
        }
    }

    @Override
    public boolean isLeaf() {
        return filled ? super.isLeaf() : replyAmount == 0;
    }

    public boolean isFilled() {
        return filled;
    }

    @Override
    public boolean hasUnreadReply() {
        if (filled) {
            return super.hasUnreadReply();
        } else {
            return unreadReplies > 0;
        }
    }

    @Override
    public Post getMessageById(int messageId) {
        return threadPosts.get(messageId);
    }

    public void setUnreadPosts(ReadStatistic unreadPosts) {
        this.unreadPosts = unreadPosts.getUnreadMessages();
        this.unreadReplies = unreadPosts.getUnreadReplies();
    }

    public int getUnreadPosts() {
        return unreadPosts;
    }

    public void clearThread() {
        lastPostDate = getLastPostDate();
        replyAmount = getPostAmount();

        unreadPosts = 0;
        unreadReplies = 0;
        int myId = Property.RSDN_USER_ID.get(-1);
        
        for (Post p : threadPosts.valueCollection()) {
            if (p != this && !p.messageData.isRead()) {
                unreadPosts++;
                if (p.messageData.getParentUserId() == myId) {
                    unreadReplies ++;
                }
            }
        }

        childrenPosts.clear();
        threadPosts.clear();
        threadPosts.put(getMessageId(), this);
        setLoadingState(LoadingState.NotLoaded);
        filled = false;
    }

    public void fillThread(Collection<MessageData> po) {
        List<MessageData> posts = new ArrayList<>(po);
        Collections.sort(posts, SORT_BY_PARENTS);

        for (MessageData post : posts) {
            Post p = threadPosts.get(post.getMessageId());

            if (p == null) {
                // New post
                Post parent = threadPosts.get(post.getParentId());

                if (parent == null) {
                    // TODO: Update full tree after moving/extract thread while synchronization.
//                    throw new RojacDebugException("No parent for post #" + post.getMessageId() + " in thread #" + getMessageId());
                    continue;
                }

                Post newPost = new Post(post, parent, this);
                threadPosts.put(post.getMessageId(), newPost);

                // Postpone sorting
                parent.childrenPosts.add(newPost);
            } else {
                // Post exists

                p.setMessageData(post);
            }
        }

        // Sorting...
        deepResort();
        if (!filled) {
            filled = true;
        }
    }

    @Override
    protected void setDeepRead(boolean read) {
        if (filled) {
            // Thread has already filled - go through nodes and set them as read.
            super.setDeepRead(read);
        } else {
            // Thread hasn't filled yet - just reset statistic data.
            setRead(read);
            unreadPosts = read ? 0 : replyAmount;
        }
    }
}