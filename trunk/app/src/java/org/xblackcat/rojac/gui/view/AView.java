package org.xblackcat.rojac.gui.view;

import org.xblackcat.rojac.gui.IAppControl;
import org.xblackcat.rojac.gui.IView;
import org.xblackcat.rojac.service.ServiceFactory;
import org.xblackcat.rojac.service.datahandler.IPacket;
import org.xblackcat.rojac.service.datahandler.IPacketProcessor;
import org.xblackcat.rojac.service.datahandler.PacketDispatcher;
import org.xblackcat.rojac.service.storage.IStorage;

import javax.swing.*;
import java.awt.*;

/**
 * @author xBlackCat
 */

public abstract class AView extends JPanel implements IView {
    protected final IStorage storage = ServiceFactory.getInstance().getStorage();
    private final PacketDispatcher packetDispatcher;
    private final ViewId id;

    protected final IAppControl appControl;

    protected AView(ViewId id, IAppControl appControl) {
        super(new BorderLayout());
        this.id = id;
        this.appControl = appControl;
        packetDispatcher = new PacketDispatcher(getProcessors());
    }

    protected abstract IPacketProcessor<IPacket>[] getProcessors();

    public void applySettings() {
    }

    public void storeSettings() {
    }

    public final JComponent getComponent() {
        return this;
    }

    @Override
    public ViewId getId() {
        return id;
    }

    @Override
    public final void processPacket(IPacket packet) {
        packetDispatcher.dispatch(packet);
    }
}
