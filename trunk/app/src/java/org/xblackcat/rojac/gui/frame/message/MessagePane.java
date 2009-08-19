package org.xblackcat.rojac.gui.frame.message;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flexdock.util.SwingUtility;
import org.xblackcat.rojac.data.Mark;
import org.xblackcat.rojac.data.Message;
import org.xblackcat.rojac.gui.IInternationazable;
import org.xblackcat.rojac.gui.popup.PopupMenuBuilder;
import org.xblackcat.rojac.i18n.JLOptionPane;
import org.xblackcat.rojac.i18n.Messages;
import org.xblackcat.rojac.service.ServiceFactory;
import org.xblackcat.rojac.service.converter.IMessageParser;
import org.xblackcat.rojac.service.storage.IStorage;
import org.xblackcat.rojac.service.storage.StorageException;
import org.xblackcat.rojac.util.LinkUtils;
import org.xblackcat.utils.ResourceUtils;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.Date;

/**
 * Date: 31 ���� 2007
 *
 * @author xBlackCat
 */

public class MessagePane extends AMessageView implements IInternationazable {
    private static final Log log = LogFactory.getLog(MessagePane.class);
    private final IMessageParser rsdnToHtml = ServiceFactory.getInstance().getMessageConverter();

    private final IStorage storage = ServiceFactory.getInstance().getStorage();
    private final JTextPane messageTextPane = new JTextPane();

    private final JLabel labelTopic = new JLabel();
    private final JButton marksButton = new JButton();
    private final JLabel userInfoLabel = new JLabel();
    private final JLabel messageDateLabel = new JLabel();

    private int messageId;
    private JButton answer;
    private static final Insets BUTTON_MARGIN = new Insets(2, 2, 2, 2);
    private JLabel userLabel;
    private JLabel dateLabel;

    public MessagePane() {
        super(new BorderLayout());

        initialize();

        loadLabels();
    }

    private void initialize() {
        messageTextPane.setEditorKit(new HTMLEditorKit());
        messageTextPane.setEditable(false);
        messageTextPane.addHyperlinkListener(new HyperlinkHandler());

        add(createTitleBar(), BorderLayout.NORTH);
        add(new JScrollPane(messageTextPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
    }

    private Component createTitleBar() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        JPanel labelPane = new JPanel(new BorderLayout());
        p.add(labelPane, BorderLayout.NORTH);

        labelPane.add(labelTopic, BorderLayout.CENTER);
        labelPane.add(marksButton, BorderLayout.EAST);

        marksButton.setDefaultCapable(false);
        marksButton.setFocusable(false);
        marksButton.setFocusPainted(false);
        marksButton.setMargin(BUTTON_MARGIN);
        marksButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                RatingDialog rd = new RatingDialog(SwingUtilities.windowForComponent(MessagePane.this), messageId);
                SwingUtility.center(rd, marksButton);
                rd.setVisible(true);
            }
        });
        marksButton.setRolloverEnabled(true);

        final IconsModel marksModel = new IconsModel(
                Mark.PlusOne,
                Mark.Agree,
                Mark.Disagree,
                Mark.Smile,
                Mark.x1,
                Mark.x2,
                Mark.x3
        );

        final MarkRenderer renderer = new MarkRenderer(ResourceUtils.loadImageIcon("/images/marks/select.gif"));

        final JComboBox marks = new JComboBox(marksModel);
        marks.setFocusable(false);
        marks.setToolTipText(Messages.DESCRIPTION_MARK_SELECT.getMessage());
        marks.setRenderer(renderer);
        marks.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                marks.setPopupVisible(false);
                chooseMark(marksModel.getSelectedItem());
                marksModel.reset();
            }
        });

        answer = new JButton();
        answer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // TODO: replace with proper action
                JLOptionPane.showMessageDialog(MessagePane.this, "Answer on message [id=" + messageId + "]");
            }
        });
        answer.setIcon(ResourceUtils.loadImageIcon("/images/actions/reply.gif"));
        answer.setFocusable(false);
        answer.setMargin(BUTTON_MARGIN);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        controls.add(answer);
        controls.add(marks);
        p.add(controls, BorderLayout.EAST);

        userLabel = new JLabel();
        dateLabel = new JLabel();

        JPanel labels = new JPanel(new GridLayout(0, 1));
        p.add(labels, BorderLayout.WEST);
        labels.add(userLabel);
        labels.add(dateLabel);

        JPanel infoPane = new JPanel(new GridLayout(0, 1));
        p.add(infoPane, BorderLayout.CENTER);

        infoPane.add(userInfoLabel);
        infoPane.add(messageDateLabel);

        return p;
    }

    /**
     * Assigns specified mark to the message
     *
     * @param mark new mark
     */
    private void chooseMark(Mark mark) {
        if (JOptionPane.YES_OPTION ==
                JLOptionPane.showConfirmDialog(
                        this,
                        Messages.DIALOG_SET_MARK_MESSAGE.getMessage(mark),
                        Messages.DIALOG_SET_MARK_TITLE.getMessage(),
                        JOptionPane.YES_NO_OPTION
                )) {
            try {
                storage.getNewRatingAH().storeNewRating(messageId, mark);
                updateMarksPane(messageId);
            } catch (StorageException e) {
                JLOptionPane.showMessageDialog(
                        this,
                        Messages.ERROR_DIALOG_SET_MARK_MESSAGE.getMessage(mark),
                        Messages.ERROR_DIALOG_SET_MARK_MESSAGE.getMessage(),
                        JOptionPane.DEFAULT_OPTION
                );
                if (log.isWarnEnabled()) {
                    log.warn("Cann't store mark " + mark + " for message [id=" + messageId + "].", e);
                }
            }
        }
    }

    public void viewItem(int messageId) {
        this.messageId = messageId;

        Message mes;
        try {
            mes = storage.getMessageAH().getMessageById(messageId);

            String message = mes.getMessage();
            String converted = rsdnToHtml.convert(message);
            messageTextPane.setText(converted);
            labelTopic.setText(mes.getSubject());
            userInfoLabel.setText(mes.getUserNick());
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Messages.getLocale());
            messageDateLabel.setText(df.format(new Date(mes.getMessageDate())));

            updateMarksPane(messageId);
        } catch (StorageException e) {
            throw new RuntimeException("Can't load message id = " + messageId, e);
        }
    }

    public void updateItem(int messageId) {
        if (messageId == this.messageId) {
            viewItem(messageId);
        }
    }

    private void updateMarksPane(int messageId) throws StorageException {
        Mark[] ratings = storage.getRatingAH().getRatingMarksByMessageId(messageId);

        Mark[] ownRatings = storage.getNewRatingAH().getNewRatingMarksByMessageId(messageId);

        fillMarksButton((Mark[]) ArrayUtils.addAll(ratings, ownRatings));
    }

    private void fillMarksButton(Mark[] ratings) {
        boolean empty = ArrayUtils.isEmpty(ratings);

        marksButton.setVisible(!empty);
        if (empty) {
            marksButton.setText("No marks");
            return;
        }

        int smiles = 0;
        int agrees = 0;
        int disagrees = 0;
        int plusOnes = 0;
        int rate = 0;
        int rateAmount = 0;

        for (Mark r : ratings) {
            switch (r) {
                case Agree:
                    ++agrees;
                    break;
                case Disagree:
                    ++disagrees;
                    break;
                case PlusOne:
                    ++plusOnes;
                    break;
                case Remove:
                    // Do nothig
                    break;
                case Smile:
                    ++smiles;
                    break;
                case x3:
                    ++rate;
                case x2:
                    ++rate;
                case x1:
                    ++rate;
                    ++rateAmount;
                    break;
            }
        }

        StringBuilder text = new StringBuilder("<html><body>");

        if (rateAmount > 0) {
            text.append("<b>");
            text.append(rate);
            text.append("(");
            text.append(rateAmount);
            text.append(")</b> ");
        }

        text.append(addInfo(Mark.PlusOne, plusOnes));
        text.append(addInfo(Mark.Agree, agrees));
        text.append(addInfo(Mark.Disagree, disagrees));
        text.append(addInfo(Mark.Smile, smiles));

        marksButton.setText(text.toString());
        revalidate();
    }

    private String addInfo(Mark m, int amount) {
        if (amount > 0) {
            String res = "&nbsp;<img src='" + m.getUrl().toString() + "'>";
            if (amount > 1) {
                return res + "<i>x" + amount + "</i>";
            } else {
                return res;
            }
        }
        return "";
    }

    public void loadLabels() {
        answer.setToolTipText(Messages.BUTTON_REPLY_TOOLTIP.getMessage());
        userLabel.setText(Messages.MESSAGE_PANE_USER_LABEL.getMessage());
        dateLabel.setText(Messages.MESSAGE_PANE_DATE_LABEL.getMessage());
    }

    private class HyperlinkHandler implements HyperlinkListener {
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                JPopupMenu menu = PopupMenuBuilder.getLinkMenu(
                        e.getURL(),
                        e.getDescription(),
                        LinkUtils.getUrlText(e.getSourceElement())
                );

                Point l = MouseInfo.getPointerInfo().getLocation();
                SwingUtilities.convertPointFromScreen(l, messageTextPane);
                menu.show(messageTextPane, l.x, l.y);
            } else if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                
            }
        }
    }

}
