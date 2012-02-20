package org.xblackcat.rojac.gui.dialog.db;

import org.xblackcat.rojac.gui.component.ACancelAction;
import org.xblackcat.rojac.gui.component.AnOkAction;
import org.xblackcat.rojac.i18n.JLOptionPane;
import org.xblackcat.rojac.i18n.Message;
import org.xblackcat.rojac.service.storage.database.connection.DatabaseSettings;
import org.xblackcat.rojac.service.storage.importing.ImportProcessor;
import org.xblackcat.rojac.util.WindowsUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * 05.10.11 12:52
 *
 * @author xBlackCat
 */
public class ImportDialog extends JDialog {

    private final DBSettingsPane sourceStorage;
    private final DBSettingsPane destinationStorage;

    public ImportDialog(Window parent) {
        super(parent, ModalityType.DOCUMENT_MODAL);

        setTitle(Message.Dialog_Import_Title.get());

        sourceStorage = new DBSettingsPane(true);
        destinationStorage = new DBSettingsPane();

        LineBorder border = new LineBorder(Color.black, 1, true);
        sourceStorage.setBorder(new TitledBorder(border, Message.Dialog_Import_Label_Source.get(), TitledBorder.CENTER, TitledBorder.TOP));
        destinationStorage.setBorder(new TitledBorder(border, Message.Dialog_Import_Label_Destination.get(), TitledBorder.CENTER, TitledBorder.TOP));

        setContentPane(setupContentPage());

        pack();
        setSize(600, getHeight());
        setMinimumSize(getSize());
    }

    private JComponent setupContentPage() {
        JPanel cp = new JPanel(new BorderLayout(5, 5));

        cp.setBorder(new EmptyBorder(5, 5, 5, 5));

        cp.add(new JLabel(Message.Dialog_Import_Label.get()), BorderLayout.NORTH);

        JPanel centerPane = new JPanel(new GridLayout(1, 0, 5, 5));

        centerPane.add(sourceStorage);
        centerPane.add(destinationStorage);

        cp.add(centerPane, BorderLayout.CENTER);

        cp.add(WindowsUtils.createButtonsBar(
                this,
                Message.Button_Ok,
                new AnOkAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        DatabaseSettings source = sourceStorage.getCurrentSettings();
                        DatabaseSettings destination = destinationStorage.getCurrentSettings();

                        if (source == null || destination == null) {
                            JLOptionPane.showMessageDialog(
                                    ImportDialog.this,
                                    Message.Dialog_Import_Warning_Text.get(),
                                    Message.Dialog_Import_Warning_Title.get(),
                                    JOptionPane.WARNING_MESSAGE
                            );
                            return;
                        }

                        dispose();
                        new ImportProcessor(source, destination, getOwner()).execute();
                    }
                },
                new ACancelAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        dispose();
                    }
                }
        ), BorderLayout.SOUTH);

        return cp;
    }
}