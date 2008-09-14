package org.xblackcat.rojac.gui;

import org.xblackcat.rojac.data.Forum;
import org.xblackcat.rojac.gui.frame.progress.ITask;

/**
 * Date: 20 ���� 2008
 *
 * @author xBlackCat
 */

public interface IRootPane {
    void openForumTab(Forum f);

    void showProgressDialog(ITask task);
}
