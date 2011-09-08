package org.xblackcat.rojac.service.progress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.rojac.i18n.Messages;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author xBlackCat
 */

public class ProgressController implements IProgressController {
    private static final Log log = LogFactory.getLog(ProgressController.class);
    private final java.util.Queue<ProgressChangeEvent> waitingEvents = new LinkedBlockingQueue<ProgressChangeEvent>();
    private boolean processorAimed = false;

    private final EventListenerList listenerList = new EventListenerList();

    @Override
    public void fireJobStart() {
        fireProgressChanged(new ProgressChangeEvent(this, ProgressState.Start, null, null));
    }

    @Override
    public void fireJobStart(Messages message, Object... arguments) {
        fireProgressChanged(new ProgressChangeEvent(this, ProgressState.Start, null, message.get(arguments)));
    }

    @Override
    public void fireJobProgressChanged(long progress, long total) {
        fireProgressChanged(new ProgressChangeEvent(this, ProgressState.Work, (int) (progress * 100. / total), null));
    }

    @Override
    public void fireJobProgressChanged(long amount) {
        fireProgressChanged(new ProgressChangeEvent(this, ProgressState.Work, (int) amount, null, false));
    }

    @Override
    public void fireJobProgressChanged(float progress, Messages message, Object... arguments) {
        fireProgressChanged(new ProgressChangeEvent(this, ProgressState.Work, (int) (progress * 100), message.get(arguments)));
    }

    @Override
    public void fireJobStop() {
        fireProgressChanged(new ProgressChangeEvent(this, ProgressState.Stop, null, null));
    }

    @Override
    public void fireJobStop(Messages message, Object... arguments) {
        fireProgressChanged(new ProgressChangeEvent(this, ProgressState.Stop, null, message.get(arguments)));
    }

    @Override
    public void fireIdle(Messages message, Object... arguments) {
        fireProgressChanged(new ProgressChangeEvent(this, ProgressState.Idle, null, message.get(arguments)));
    }

    @Override
    public void fireIdle() {
        fireProgressChanged(new ProgressChangeEvent(this, ProgressState.Idle, null, null));
    }

    @Override
    public void addProgressListener(IProgressListener listener) {
        listenerList.add(IProgressListener.class, listener);
    }

    @Override
    public void removeProgressListener(IProgressListener listener) {
        listenerList.remove(IProgressListener.class, listener);
    }

    @Override
    public void fireException(Messages message, Object... arguments) {
        fireProgressChanged(new ProgressChangeEvent(this, ProgressState.Exception, null, message.get(arguments)));
    }

    private void fireProgressChanged(final ProgressChangeEvent event) {

        if (EventQueue.isDispatchThread()) {
            processEvent(event);
        } else {
            synchronized (waitingEvents) {
                if (processorAimed) {
                    if (!waitingEvents.contains(event)) {
                        waitingEvents.offer(event);
                    }
                } else {
                    processorAimed = true;
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            ProgressChangeEvent e;
                            do {
                                synchronized (waitingEvents) {
                                    e = waitingEvents.poll();
                                    if (e == null) {
                                        processorAimed = false;
                                        break;
                                    }
                                }

                                processEvent(e);
                            } while (true);
                        }
                    });
                }
            }

            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        processEvent(event);
                    }
                });
            } catch (InterruptedException e) {
                log.error("Process progress event is interrupted.", e);
            } catch (InvocationTargetException e) {
                log.error("Can not process change event", e);
            }
        }
    }

    /**
     * Process event in EventDispatching thread.
     *
     * @param e event to process.
     */
    private void processEvent(ProgressChangeEvent e) {
        for (IProgressListener l : listenerList.getListeners(IProgressListener.class)) {
            l.progressChanged(e);
        }
    }
}