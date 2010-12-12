package org.xblackcat.rojac.gui;

import net.infonode.docking.View;
import org.xblackcat.rojac.gui.view.ViewId;

import java.awt.*;

/**
 * @author xBlackCat
 */

public interface IAppControl {
    /**
     * Returns a main application window.
     *
     * @return main application window.
     */
    Window getMainFrame();

    /**
     * Show edit dialog. Possible combinations are: <ul> <li>messageId is <code>null</code> and forumId specifies a
     * forum - create a new thread in the specified forum. <li>messageId specifies a message and forumId specifies a
     * forum - create an answer on specified message. <li>messageId specifies a message and forumId is <code>null</code>
     * - edit specified un-posted message. </ul>
     *
     * @param forumId   forum id or <code>null</code>.
     * @param messageId message id or <code>null</code>.
     */
    void editMessage(Integer forumId, Integer messageId);

    /**
     * Opens a message by id
     *
     * @param messageId target message id.
     */
    void openMessage(int messageId);

    /**
     * Adds (or make visible if already added) a tab with the type and root content id.
     *
     * @param viewId identify of view to open or create.
     *
     * @return opened view (created or already existed)
     */
    View openTab(ViewId viewId);

    /**
     * Closes a tab with specified type and id.
     *
     * @param viewId target tab content id.
     */
    void closeTab(ViewId viewId);
}
