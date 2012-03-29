package org.xblackcat.rojac.gui.view.message;

import net.java.balloontip.BalloonTip;
import net.java.balloontip.CustomBalloonTip;
import net.java.balloontip.positioners.LeftAbovePositioner;
import net.java.balloontip.styles.BalloonTipStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.rojac.RojacDebugException;
import org.xblackcat.rojac.data.MessageData;
import org.xblackcat.rojac.gui.IAppControl;
import org.xblackcat.rojac.gui.popup.PopupMenuBuilder;
import org.xblackcat.rojac.i18n.Message;
import org.xblackcat.rojac.service.options.Property;
import org.xblackcat.rojac.service.storage.IMessageAH;
import org.xblackcat.rojac.service.storage.Storage;
import org.xblackcat.rojac.util.*;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 13.03.12 16:05
 *
 * @author xBlackCat
 */
class HyperlinkHandler implements HyperlinkListener {
    private static final Log log = LogFactory.getLog(HyperlinkHandler.class);

    private final Map<Element, BalloonTip> openBalloons = new HashMap<>();

    private final IAppControl appControl;
    private final JTextPane invoker;

    public HyperlinkHandler(IAppControl appControl, JTextPane invoker) {
        this.appControl = appControl;
        this.invoker = invoker;
    }

    public void hyperlinkUpdate(HyperlinkEvent e) {
        Point l = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(l, invoker);
        int mouseY = l.y;

        Element element = e.getSourceElement();
        BalloonTip balloonTip = openBalloons.get(element);
        if (balloonTip != null) {
            balloonTip.refreshLocation();
            return;
        }

        URL url = e.getURL();
        String stringUrl = e.getDescription();
        String text = LinkUtils.getUrlText(element);

        if (url == null) {
            // Invalid url. Try to parse it from text.

            try {
                url = new URL(text);
                // Url in the 'text' field, so assume that text in the 'description' field
                text = stringUrl;
                stringUrl = url.toExternalForm();
            } catch (MalformedURLException ex) {
                // url can not be obtained neither from text nor from description.
            }
        }

        Integer messageId = LinkUtils.getMessageIdFromUrl(stringUrl);
        if (messageId == null) {
            messageId = LinkUtils.getMessageIdFromUrl(text);
        }

        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            if (url == null) {
                // TODO: show error or standard dialog
            } else if (messageId == null) {
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();

                    try {
                        desktop.browse(url.toURI());
                    } catch (IOException e1) {
                        log.error("Can not open url " + url.toExternalForm() + " in default browser");
                    } catch (URISyntaxException e1) {
                        log.error("Can not obtain URI of URL: " + url.toExternalForm());
                    }
                } else {
                    // TODO: show error dialog ???
                }
            } else {
                appControl.openMessage(messageId, Property.OPEN_MESSAGE_BEHAVIOUR_GENERAL.get());
            }
        } else if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {

            if (url == null) {
                // TODO: show error or standard dialog
            } else if (messageId == null) {
                if (LinkUtils.isYoutubeLink(url)) {
                    if (showYoutubePreviewBalloon(url, element, mouseY)) {
                        // Youtube link is resolved
                        return;
                    }
                }

                if (LinkUtils.isImageLink(url)) {
                    if (showImagePreviewBalloon(url, element, mouseY)) {
                        // Youtube link is resolved
                        return;
                    }
                }

                showHtmlPreviewBalloon(url, element, mouseY);
            } else {
                showMessageBalloon(messageId, element, mouseY);
            }
        }
    }

    private void showMessageBalloon(final int messageId, final Element element, final int mouseY) {
        new RojacWorker<Void, MessageData>() {
            @Override
            protected Void perform() throws Exception {
                publish(Storage.get(IMessageAH.class).getMessageData(messageId));

                return null;
            }

            @Override
            protected void process(List<MessageData> chunks) {
                for (MessageData h : chunks) {
                    AnUrlInfoPane postInfo;
                    if (h == null) {
                        postInfo = new NoPostInfoPane(messageId);
                    } else {
                        postInfo = new PostInfoPane(h);
                    }
                    setupBalloon(element, mouseY, postInfo);
                }
            }
        }.execute();
    }

    private void showHtmlPreviewBalloon(final URL url, final Element sourceElement, final int mouseY) {
        AnUrlInfoPane linkPreview = new HtmlPagePreview(url, new Runnable() {
            @Override
            public void run() {
                assert RojacUtils.checkThread(true);

                if (SWTUtils.isSwtEnabled) {
                    SWTUtils.getBrowser().stopLoading();
                    SWTUtils.getBrowser().navigate("about:blank");
                }
            }
        });

        setupBalloon(sourceElement, mouseY, linkPreview);
    }

    private boolean showYoutubePreviewBalloon(final URL url, final Element sourceElement, final int mouseY) {
        final String videoId = LinkUtils.getYoutubeVideoId(url);
        if (videoId == null) {
            return false;
        }

        final AnUrlInfoPane linkPreview = new YoutubePagePreview(url, videoId);

        setupBalloon(sourceElement, mouseY, linkPreview);
        return true;
    }

    private boolean showImagePreviewBalloon(final URL url, final Element sourceElement, final int mouseY) {
        final AnUrlInfoPane linkPreview = new ImagePreviewPane(url);

        setupBalloon(sourceElement, mouseY, linkPreview);
        return true;
    }

    /**
     * Generate a balloon tip. Returns null if
     *
     * @param sourceElement
     * @param y
     * @param info          @return
     */
    private void setupBalloon(final Element sourceElement, int y, AnUrlInfoPane info) {
        Rectangle r = getElementRectangle(sourceElement, y);

        Color color = new Color(0xFFFFCC);
        BalloonTipStyle tipStyle = BalloonTipUtils.createTipStyle(color);

        final Runnable onClose = info.getOnClose();

        JButton closeButton = BalloonTipUtils.balloonTipCloseButton(onClose);
        final BalloonTip balloonTip = new CustomBalloonTip(invoker, info, r, tipStyle, new LeftAbovePositioner(15, 15), closeButton);
        openBalloons.put(sourceElement, balloonTip);

        UIUtils.updateBackground(info, color);

        balloonTip.addHierarchyListener(new HierarchyListener() {
            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if (HierarchyEvent.SHOWING_CHANGED == (HierarchyEvent.SHOWING_CHANGED & e.getChangeFlags())) {
                    if (balloonTip.isShowing()) {
                        openBalloons.put(sourceElement, balloonTip);
                    } else {
                        openBalloons.remove(sourceElement);
                    }
                }
            }
        });

        balloonTip.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (e.getOppositeComponent() != null) {
                    balloonTip.closeBalloon();
                    if (onClose != null) {
                        onClose.run();
                    }
                }
            }
        });
        balloonTip.requestFocus();
        info.initialize(balloonTip);
        balloonTip.refreshLocation();
    }

    private Rectangle getElementRectangle(Element sourceElement, int y) {
        Rectangle r;
        try {
            int startOffset = sourceElement.getStartOffset();
            int endOffset = sourceElement.getEndOffset();

            Rectangle p0 = invoker.modelToView(startOffset);
            Rectangle p1 = invoker.modelToView(endOffset);

            if (p0.y == p1.y) {
                r = p0.union(p1);
            } else if (p0.y <= y && p0.y + p0.height >= y) {
                Dimension size = invoker.getSize();
                r = new Rectangle(p0.x, p0.y, size.width - p0.y, p0.height);
            } else {
                r = new Rectangle(0, p1.y, p1.y + p1.width, p1.height);
            }
        } catch (BadLocationException e1) {
            throw new RojacDebugException("Got invalid offset of element", e1);
        }
        return r;
    }

    private class NoPostInfoPane extends AnUrlInfoPane {
        private final int messageId;

        public NoPostInfoPane(int messageId) {
            super(LinkUtils.buildThreadLink(messageId), "#" + messageId);
            this.messageId = messageId;
        }

        @Override
        protected void loadUrlInfo(BalloonTip balloonTip) {
            infoLabel.setText(Message.PreviewLink_PostNotFound.get(messageId));
            infoLabel.setIcon(null);
            infoLabel.setToolTipText(null);

            JLabel advancedOptions = new JLabel(Message.PreviewLink_MoreActions.get());
            add(advancedOptions, BorderLayout.SOUTH);
            advancedOptions.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            advancedOptions.setHorizontalAlignment(SwingConstants.RIGHT);
            advancedOptions.addMouseListener(new PopupMenuShower(messageId, false));
        }
    }

    private class PostInfoPane extends AnUrlInfoPane {
        private final MessageData messageData;

        public PostInfoPane(MessageData messageData) {
            super(LinkUtils.buildThreadLink(messageData.getMessageId()), messageData.getSubject());
            this.messageData = messageData;
        }

        @Override
        protected void loadUrlInfo(BalloonTip balloonTip) {
            remove(infoLabel);

            JPanel cp = new JPanel(new BorderLayout(10, 10));
            cp.setOpaque(true);

            JLabel userInfo = new JLabel(messageData.getUserName());
            cp.add(userInfo, BorderLayout.CENTER);

            JLabel messageDate = new JLabel(MessageUtils.formatDate(messageData.getMessageDate()));
            cp.add(messageDate, BorderLayout.EAST);

            JLabel ratingInfo = new JLabel();
            ratingInfo.setHorizontalAlignment(SwingConstants.CENTER);
            ratingInfo.setIcon(MessageUtils.buildRateImage(messageData.getRating(), ratingInfo.getFont(), ratingInfo.getForeground()));
            cp.add(ratingInfo, BorderLayout.SOUTH);

            add(cp, BorderLayout.CENTER);

            JLabel advancedOptions = new JLabel(Message.PreviewLink_MoreActions.get());
            add(advancedOptions, BorderLayout.SOUTH);
            advancedOptions.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            advancedOptions.setHorizontalAlignment(SwingConstants.RIGHT);
            advancedOptions.addMouseListener(new PopupMenuShower(messageData.getMessageId(), true));

            UIUtils.updateBackground(this, getBackground());
        }
    }

    private class PopupMenuShower extends MouseAdapter {
        private final int messageId;
        private final boolean found;

        public PopupMenuShower(int messageId, boolean found) {
            this.messageId = messageId;
            this.found = found;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            Point l = MouseInfo.getPointerInfo().getLocation();
            SwingUtilities.convertPointFromScreen(l, invoker);
            JPopupMenu menu = PopupMenuBuilder.getPostMenu(messageId, appControl, found);
            menu.show(invoker, l.x, l.y);
        }
    }
}
