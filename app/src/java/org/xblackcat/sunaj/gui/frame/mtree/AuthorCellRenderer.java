package org.xblackcat.sunaj.gui.frame.mtree;

/**
 * Date: 15 ���� 2007
 *
 * @author xBlackCat
 */
final class AuthorCellRenderer extends MessageCellRenderer {

    protected boolean setupComponent(MessageItem item, boolean selected) {
        setText("Nick");

        return false;
    }
}