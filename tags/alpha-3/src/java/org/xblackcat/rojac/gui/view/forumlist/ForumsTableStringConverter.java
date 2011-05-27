package org.xblackcat.rojac.gui.view.forumlist;

import org.xblackcat.rojac.data.Forum;

import javax.swing.table.TableModel;
import javax.swing.table.TableStringConverter;

/**
 * @author xBlackCat
 */
class ForumsTableStringConverter extends TableStringConverter {
    @Override
    public String toString(TableModel model, int row, int column) {
        Forum forum = ((ForumData) model.getValueAt(row, column)).getForum();
        return forum == null ? "" : forum.getForumName();
    }
}
