package org.xblackcat.rojac.gui.view;

import org.xblackcat.rojac.data.Forum;
import org.xblackcat.rojac.data.ForumStatistic;

/**
 * @author xBlackCat
 */

public class ForumData {
    private final int forumId;
    private Forum forum;
    private ForumStatistic stat;
    private boolean subscribed;

    public ForumData(int forumId) {
        this.forumId = forumId;
    }

    public ForumData(Forum forum) {
        this.forum = forum;
        forumId = forum.getForumId();
        subscribed = forum.isSubscribed();
    }

    public Forum getForum() {
        return forum;
    }

    public void setForum(Forum forum) {
        this.forum = forum;
    }

    public int getForumId() {
        return forumId;
    }

    public ForumStatistic getStat() {
        return stat;
    }

    public void setStat(ForumStatistic stat) {
        this.stat = stat;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ForumData forumData = (ForumData) o;

        return forumId == forumData.forumId;

    }

    @Override
    public int hashCode() {
        return forumId;
    }
}
