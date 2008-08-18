package org.xblackcat.sunaj.i18n;

import org.xblackcat.utils.ResourceUtils;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Date: 23 ���� 2007
 *
 * @author xBlackCat
 */

public enum Messages {
    MAIN_WINDOW_TITLE,
    // Button texts
    BUTTON_OK,
    BUTTON_CANCEL,
    BUTTON_IGNORE,
    BUTTON_YES,
    BUTTON_NO,

    BUTTON_REPLY_TOOLTIP,

    // Main window views texts
    VIEW_FORUMS_TITLE,
    VIEW_FORUMS_TAB_TEXT,
    VIEW_FORUMS_BUTTON_UPDATE,
    // View modes descriptions
    VIEW_FORUMS_MODE_SHOWALL_TOOLTIP,
    VIEW_FORUMS_MODE_SHOWALL_TEXT,
    VIEW_FORUMS_MODE_NOTEMPTY_TOOLTIP,
    VIEW_FORUMS_MODE_NOTEMPTY_TEXT,
    VIEW_FORUMS_MODE_SUBSCRIBED_TOOLTIP,
    VIEW_FORUMS_MODE_SUBSCRIBED_TEXT,

    VIEW_FAVORITES_TITLE,
    VIEW_FAVORITES_TAB_TEXT,

    // Dialog texts
    /**
     * Parameters are: 1. Mark description
     */
    DIALOG_SET_MARK_MESSAGE,
    DIALOG_SET_MARK_TITLE,

    // Login dialog related messages
    DIALOG_LOGIN_TITLE,
    DIALOG_LOGIN_TEXT,
    DIALOG_LOGIN_USERNAME,
    DIALOG_LOGIN_PASSWORD,
    DIALOG_LOGIN_SAVE_PASSWORD,

    /**
     * Parameters are: 1. Mark description
     */
    ERROR_DIALOG_SET_MARK_MESSAGE,
    ERROR_DIALOG_SET_MARK_TITLE,

    MESSAGE_PANE_USER_LABEL,
    MESSAGE_PANE_DATE_LABEL,
    MESSAGE_PANE_TOOLBAR_TITLE_RATING,

    // Mark descriptions
    DESCRIPTION_MARK_SELECT,
    DESCRIPTION_MARK_PLUSONE,
    DESCRIPTION_MARK_AGREE,
    DESCRIPTION_MARK_DISAGREE,
    DESCRIPTION_MARK_X1,
    DESCRIPTION_MARK_X2,
    DESCRIPTION_MARK_X3,
    DESCRIPTION_MARK_SMILE,
    DESCRIPTION_MARK_REMOVE,

    //Smile descriptions
    DESCRIPTION_SMILE_SMILE,
    DESCRIPTION_SMILE_SAD,
    DESCRIPTION_SMILE_WINK,
    DESCRIPTION_SMILE_BIGGRIN,
    DESCRIPTION_SMILE_LOL,
    DESCRIPTION_SMILE_SMIRK,
    DESCRIPTION_SMILE_CONFUSED,
    DESCRIPTION_SMILE_NO,
    DESCRIPTION_SMILE_SUPER,
    DESCRIPTION_SMILE_SHUFFLE,
    DESCRIPTION_SMILE_WOW,
    DESCRIPTION_SMILE_CRASH,
    DESCRIPTION_SMILE_USER,
    DESCRIPTION_SMILE_MANIAC,
    DESCRIPTION_SMILE_DONOTKNOW;

    // Constants
    private static final String LOCALIZATION_BUNDLE_NAME = "localization/messages";

    private static ResourceBundle messages;

    private static final Lock readLock;
    private static final Lock writeLock;
    private static Locale locale;

    static {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        readLock = rwl.readLock();
        writeLock = rwl.writeLock();

        setLocale(null);
    }

    /**
     * Set the specified locale for messages
     *
     * @param loc
     */
    public static void setLocale(Locale loc) throws IllegalArgumentException {
        ResourceBundle m;
        if (loc != null) {
            m = ResourceBundle.getBundle(LOCALIZATION_BUNDLE_NAME, loc);
            if (!m.getLocale().equals(loc)) {
                throw new IllegalArgumentException("Can not load resources for " + loc + " locale.");
            }
        } else {
            m = ResourceBundle.getBundle(LOCALIZATION_BUNDLE_NAME);
        }

        writeLock.lock();
        try {
            messages = m;
            locale = m.getLocale();
        } finally {
            writeLock.unlock();
        }
    }

    public String getMessage(Object... params) throws MissingResourceException {
        String key = ResourceUtils.constantToProperty(name());

        String mes;

        Locale l;
        readLock.lock();
        try {
            mes = messages.getString(key);
            l = messages.getLocale();
        } finally {
            readLock.unlock();
        }
        if (mes != null) {
            try {
                mes = new String(mes.getBytes("ISO-8859-1"), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Invalid encoding for string of key " + key, e);
            }
            return String.format(l, mes, params).trim();
        } else {
            return key;
        }
    }

    public String toString() {
        return "Constant: " + name() + " (" + ResourceUtils.constantToProperty(name()) + ")";
    }

    public static Locale getLocale() {
        readLock.lock();
        try {
            return locale;
        } finally {
            readLock.unlock();
        }
    }
}
