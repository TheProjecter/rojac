package org.xblackcat.rojac.gui.view.thread;

import org.xblackcat.rojac.gui.component.GrayedIcon;
import org.xblackcat.rojac.gui.view.model.ForumRoot;
import org.xblackcat.rojac.gui.view.model.Post;
import org.xblackcat.rojac.gui.view.model.PostUtils;
import org.xblackcat.rojac.util.UIUtils;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * @author xBlackCat
 */
class PostTreeCellRenderer extends DefaultTreeCellRenderer {
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        Post post = (Post) value;
        int style;
        switch (post.isRead()) {
            default:
            case Read:
                style = Font.PLAIN;
                break;
            case ReadPartially:
                style = Font.PLAIN;
                break;
            case Unread:
                style = Font.BOLD;
                break;
        }

        Font font = getFont().deriveFont(style);
        setFont(font);

        Icon icon = PostUtils.getPostIcon(post);
        if (!(post instanceof ForumRoot)) {
            boolean ignored;

            ignored = post.isIgnored() || PostUtils.isPostIgnoredByUser(post);

            if (ignored) {
                icon = new GrayedIcon(icon);
                setForeground(UIUtils.brighter(getForeground(), .3));
            }
        }

        setIcon(icon);

        setText(post.getMessageData().getSubject());

        return this;
    }

}
