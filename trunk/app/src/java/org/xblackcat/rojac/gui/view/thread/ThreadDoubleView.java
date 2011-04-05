package org.xblackcat.rojac.gui.view.thread;

import org.xblackcat.rojac.gui.*;
import org.xblackcat.rojac.gui.view.AnItemView;
import org.xblackcat.rojac.gui.view.ComplexState;
import org.xblackcat.rojac.gui.view.ThreadState;
import org.xblackcat.rojac.gui.view.message.MessageView;
import org.xblackcat.rojac.service.datahandler.IPacket;
import org.xblackcat.rojac.service.datahandler.IPacketProcessor;
import org.xblackcat.rojac.util.RojacUtils;
import org.xblackcat.rojac.util.ShortCutUtils;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author xBlackCat
 */

public class ThreadDoubleView extends AnItemView {
    private final IItemView masterView;
    private final IItemView slaveView;

    /**
     * Create combined forum thread view. Contains from master (upper component) and slave (lover component).
     *
     * @param mv
     * @param sv
     * @param verticalSplit
     * @param appControl
     */
    public ThreadDoubleView(IItemView mv, IItemView sv, boolean verticalSplit, IAppControl appControl) {
        // Copy master view id
        super(mv.getId(), appControl);
        this.masterView = mv;
        this.slaveView = sv;

        masterView.addActionListener(new IActionListener() {
            public void itemGotFocus(Integer forumId, Integer messageId) {
                slaveView.loadItem(messageId);
            }

            public void itemLostFocus(Integer forumId, Integer messageId) {
            }

            public void itemUpdated(Integer forumId, Integer messageId) {
            }
        });

        slaveView.addActionListener(new IActionListener() {
            public void itemGotFocus(Integer forumId, Integer messageId) {
            }

            public void itemLostFocus(Integer forumId, Integer messageId) {
            }

            public void itemUpdated(Integer forumId, Integer messageId) {
            }
        });

        masterView.addStateChangeListener(new IStateListener() {
            @Override
            public void stateChanged(IView source, IViewState newState) {
                fireViewStateChanged();
            }
        });

        JSplitPane splitPane = new JSplitPane();
        splitPane.setBottomComponent(slaveView.getComponent());
        splitPane.setTopComponent(masterView.getComponent());
        splitPane.setOrientation(verticalSplit ? JSplitPane.VERTICAL_SPLIT : JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(200);
        splitPane.setResizeWeight(.1);
        add(splitPane);

        ShortCutUtils.mergeInputMaps(this, slaveView.getComponent());
        ShortCutUtils.mergeInputMaps(this, masterView.getComponent());

        slaveView.getComponent().addPropertyChangeListener(MessageView.MESSAGE_VIEWED_FLAG, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                masterView.getComponent().firePropertyChange(
                        MessageView.MESSAGE_VIEWED_FLAG,
                        ((Integer) evt.getOldValue()).intValue(),
                        ((Integer) evt.getNewValue()).intValue()
                );
            }
        });
    }

    public void loadItem(int itemId) {
        masterView.loadItem(itemId);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    protected IPacketProcessor<IPacket>[] getProcessors() {
        return new IPacketProcessor[]{
                new IPacketProcessor<IPacket>() {
                    @Override
                    public void process(IPacket p) {
                        masterView.processPacket(p);
                        slaveView.processPacket(p);
                    }
                }
        };
    }

    @Override
    public void makeVisible(int messageId) {
        masterView.makeVisible(messageId);
        slaveView.makeVisible(messageId);
    }

    @Override
    public ComplexState getState() {
        assert RojacUtils.checkThread(true, AThreadView.class);

        return new ComplexState(masterView.getState(), slaveView.getState());
    }

    @Override
    public void setState(IViewState state) {
        assert RojacUtils.checkThread(true, AThreadView.class);

        if (state == null) {
            return;
        }

        if (!(state instanceof ComplexState) && !(state instanceof ThreadState)) {
            RojacUtils.fireDebugException("Invalid state object " + state.toString() + " [" + state.getClass() + "]");
            return;
        }

        // Old state support
        if (state instanceof ThreadState) {
            masterView.setState(state);
            return;
        }

        ComplexState s = (ComplexState) state;

        masterView.setState(s.getMasterState());
        slaveView.setState(s.getSlaveState());
    }

    @Override
    public boolean containsItem(int messageId) {
        return masterView.containsItem(messageId);
    }

    @Override
    public void addActionListener(IActionListener l) {
        masterView.addActionListener(l);
        slaveView.addActionListener(l);
    }

    @Override
    public void removeActionListener(IActionListener l) {
        masterView.removeActionListener(l);
        slaveView.removeActionListener(l);
    }

    @Override
    public String getTabTitle() {
        return masterView.getTabTitle();
    }
}
