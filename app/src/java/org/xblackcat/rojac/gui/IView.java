package org.xblackcat.rojac.gui;

import javax.swing.*;

/**
 * Date: 15 ��� 2008
 *
 * @author xBlackCat
 */

public interface IView extends IConfigurable {
    JComponent getComponent();

    void updateData(int... ids);
}
