package org.xblackcat.rojac.gui.tray;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.rojac.util.RojacUtils;
import org.xblackcat.utils.ResourceUtils;

import java.awt.*;

/**
 * @author xBlackCat
 */

public class RojacTray {
    private static final Log log = LogFactory.getLog(RojacTray.class);

    private final boolean supported;
    private RojacState state = RojacState.Initialized;
    protected final TrayIcon trayIcon;

    public RojacTray() {
        boolean supported = SystemTray.isSupported();
        trayIcon = new TrayIcon(ResourceUtils.loadImage(""), RojacUtils.VERSION_STRING);

        if (supported) {
            try {
                SystemTray.getSystemTray().add(trayIcon);
            } catch (AWTException e) {
                log.error("Can not add tray icon", e);
                supported = false;
            }
        }

        this.supported = supported;
    }

    private void updateTray() {

    }
}