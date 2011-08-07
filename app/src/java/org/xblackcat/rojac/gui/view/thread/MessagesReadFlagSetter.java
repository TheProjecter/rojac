package org.xblackcat.rojac.gui.view.thread;

import org.xblackcat.rojac.data.MessageData;
import org.xblackcat.rojac.service.ServiceFactory;
import org.xblackcat.rojac.service.datahandler.IDataDispatcher;
import org.xblackcat.rojac.service.datahandler.SetReadExPacket;
import org.xblackcat.rojac.service.storage.IStorage;
import org.xblackcat.rojac.util.RojacWorker;

import java.util.Collection;


/**
 * @author xBlackCat
 */
public class MessagesReadFlagSetter extends RojacWorker<Void, MessageData> {
    private final IDataDispatcher dispatcher = ServiceFactory.getInstance().getDataDispatcher();

    private Collection<MessageData> posts;
    private boolean read;

    public MessagesReadFlagSetter(boolean read, Collection<MessageData> posts) {
        this.posts = posts;
        this.read = read;
    }

    @Override
    protected Void perform() throws Exception {
        IStorage storage = ServiceFactory.getInstance().getStorage();
        for (MessageData post : posts) {
            storage.getMessageAH().updateMessageReadFlag(post.getMessageId(), read);
        }
        return null;
    }

    @Override
    protected void done() {
        dispatcher.processPacket(new SetReadExPacket(posts, read));
    }
}
