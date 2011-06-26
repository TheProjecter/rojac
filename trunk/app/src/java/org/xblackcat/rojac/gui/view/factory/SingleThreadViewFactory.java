package org.xblackcat.rojac.gui.view.factory;

import org.xblackcat.rojac.gui.IAppControl;
import org.xblackcat.rojac.gui.IItemView;
import org.xblackcat.rojac.gui.view.ViewId;
import org.xblackcat.rojac.gui.view.message.MessageView;
import org.xblackcat.rojac.gui.view.model.ModelControls;
import org.xblackcat.rojac.gui.view.thread.ThreadDoubleView;
import org.xblackcat.rojac.gui.view.thread.TreeTableThreadView;

/**
 * @author xBlackCat
 */

class SingleThreadViewFactory implements IViewFactory {
    @Override
    public IItemView makeView(ViewId id, IAppControl appControl) {
        IItemView threadView = new TreeTableThreadView(id, appControl, ModelControls.SINGLE_THREAD);
        IItemView messageView = new MessageView(id, appControl);

        return new ThreadDoubleView(threadView, messageView, true, appControl);
    }
}