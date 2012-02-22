package org.xblackcat.rojac.gui.popup;

import org.xblackcat.rojac.data.IFavorite;
import org.xblackcat.rojac.data.MessageData;
import org.xblackcat.rojac.gui.IAppControl;
import org.xblackcat.rojac.gui.OpenMessageMethod;
import org.xblackcat.rojac.gui.view.forumlist.ForumData;
import org.xblackcat.rojac.gui.view.model.FavoriteType;
import org.xblackcat.rojac.gui.view.model.Post;
import org.xblackcat.rojac.i18n.Messages;
import org.xblackcat.rojac.service.options.Property;
import org.xblackcat.rojac.util.LinkUtils;

import javax.swing.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Class for prepare pop-up menus for the application.
 *
 * @author xBlackCat
 */
public final class PopupMenuBuilder {
    private PopupMenuBuilder() {
    }

    /**
     * Constructs a pop-up menu for link in message pane.
     *
     * @param url        URL object of the link.
     * @param stringUrl  Text representing of the link.
     * @param text       Text associated with the link.
     * @param appControl back link to main window control.
     *
     * @return new popup menu for the specified url.
     */
    public static JPopupMenu getLinkMenu(URL url, String stringUrl, String text, IAppControl appControl) {
        if (url == null) {
            // Invalid url. Try to parse it from text.

            try {
                url = new URL(text);
                // Url in the 'text' field, so assume that text in the 'description' field 
                text = stringUrl;
                stringUrl = url.toExternalForm();
            } catch (MalformedURLException e) {
                // url can not be obtained neither from text nor from description.
            }
        }

        Integer messageId = LinkUtils.getMessageIdFromUrl(stringUrl);
        if (messageId == null) {
            messageId = LinkUtils.getMessageIdFromUrl(text);
        }

        // Build menu
        final JPopupMenu menu = new JPopupMenu();

        String headerText = messageId == null ? text : "#" + messageId;
        JMenuItem headerItem = new JMenuItem(headerText);
        headerItem.setEnabled(false);
        menu.add(headerItem);

        menu.addSeparator();
        if (messageId != null) {
            menu.add(new OpenMessageMenuItem(messageId, appControl, Messages.Popup_View_ThreadsTree_OpenMessage, null));
            menu.add(new OpenMessageMenuItem(messageId, appControl, Messages.Popup_View_ThreadsTree_OpenMessage_NewTab, OpenMessageMethod.NewTab));
            menu.add(new OpenMessageMenuItem(messageId, appControl, Messages.Popup_MessageTab_OpenMessageInThread, OpenMessageMethod.InThread));

            menu.addSeparator();
            MenuHelper.addOpenLink(menu, Messages.Popup_Link_Open_InBrowser_Message, stringUrl);
            MenuHelper.addOpenLink(menu, Messages.Popup_Link_Open_InBrowser_Thread, LinkUtils.buildThreadLink(messageId));

        } else {
            MenuHelper.addOpenLink(menu, Messages.Popup_Link_Open_InBrowser, stringUrl);
        }

        menu.add(MenuHelper.copyToClipboard(stringUrl));
        if (messageId != null) {
            menu.add(MenuHelper.copyLinkSubmenu(messageId));
        }

        return menu;
    }

    public static JPopupMenu getForumViewMenu(ForumData forum, IAppControl appControl) {
        JPopupMenu menu = new JPopupMenu(forum.getForum().getForumName());

        final boolean subscribed = forum.isSubscribed();
        final int forumId = forum.getForumId();

        menu.add(new OpenForumAction(appControl, forumId));
        menu.addSeparator();

        menu.add(new SetForumReadMenuItem(Messages.Popup_View_SetReadAll, forumId, true));
        menu.add(new SetForumReadMenuItem(Messages.Popup_View_SetUnreadAll, forumId, false));

        menu.add(new ExtendedMarkRead(Messages.Popup_View_ThreadsTree_Mark_Extended, forum.getForum(), appControl.getMainFrame()));

        menu.addSeparator();

        JCheckBoxMenuItem mi = new JCheckBoxMenuItem(Messages.Popup_View_Forums_Subscribe.get(), subscribed);
        mi.addActionListener(new SubscribeChangeListener(forumId, subscribed));
        menu.add(mi);


        return menu;
    }

    public static JPopupMenu getTreeViewMenu(Post message, IAppControl appControl, boolean addFavorites) {
        int messageId = message.getMessageId();
        final JPopupMenu menu = new JPopupMenu("#" + messageId);

        JMenuItem item = new JMenuItem("#" + messageId);
        item.setEnabled(false);

        menu.add(item);

        menu.add(new OpenMessageMenuItem(messageId, appControl, Messages.Popup_View_ThreadsTree_OpenMessage_NewTab, OpenMessageMethod.NewTab));
        menu.add(new OpenMessageMenuItem(messageId, appControl, Messages.Popup_MessageTab_OpenMessageInThread, OpenMessageMethod.InThread));
        menu.add(new OpenMessageMenuItem(messageId, appControl, Messages.Popup_MessageTab_OpenMessageInForum, OpenMessageMethod.InForum));

        menu.addSeparator();

        menu.add(new SetMessageReadMenuItem(Messages.Popup_View_ThreadsTree_Mark_Read, message.getMessageData(), true));
        menu.add(new SetMessageReadMenuItem(Messages.Popup_View_ThreadsTree_Mark_Unread, message.getMessageData(), false));

        menu.add(MenuHelper.markReadUnreadSubmenu(message, appControl.getMainFrame()));

        menu.addSeparator();
        MenuHelper.addOpenLink(menu, Messages.Popup_Link_Open_InBrowser_Message, LinkUtils.buildMessageLink(messageId));
        MenuHelper.addOpenLink(menu, Messages.Popup_Link_Open_InBrowser_Thread, LinkUtils.buildThreadLink(messageId));
        menu.add(MenuHelper.copyLinkSubmenu(messageId));

        if (addFavorites) {
            menu.addSeparator();
            menu.add(MenuHelper.favoritesSubmenu(message.getMessageData(), appControl));
        }

        return menu;
    }

    public static JPopupMenu getFavoritesMenu(IFavorite favorite, IAppControl appControl) {
        final JPopupMenu menu = new JPopupMenu(favorite.getName());

        final int favoriteId = favorite.getId();
        menu.add(new OpenFavoriteAction(appControl, favoriteId));
        menu.addSeparator();

        menu.add(new RemoveFavoriteAction(favoriteId));

        return menu;
    }

    public static JPopupMenu getForumViewTabMenu(ForumData forum, IAppControl appControl) {
        JPopupMenu menu = new JPopupMenu(forum.getForum().getForumName());

        final boolean subscribed = forum.isSubscribed();
        final int forumId = forum.getForumId();

        menu.add(new SetForumReadMenuItem(Messages.Popup_View_SetReadAll, forumId, true));
        menu.add(new SetForumReadMenuItem(Messages.Popup_View_SetUnreadAll, forumId, false));

        menu.add(new ExtendedMarkRead(Messages.Popup_View_ThreadsTree_Mark_Extended, forum.getForum(), appControl.getMainFrame()));

        menu.addSeparator();

        JCheckBoxMenuItem mi = new JCheckBoxMenuItem(Messages.Popup_View_Forums_Subscribe.get(), subscribed);
        mi.addActionListener(new SubscribeChangeListener(forumId, subscribed));
        menu.add(mi);

        return menu;
    }

    public static JPopupMenu getThreadViewTabMenu(Post post, IAppControl appControl) {
        int messageId = post.getMessageId();
        JPopupMenu menu = new JPopupMenu("#" + messageId);

        menu.add(new OpenMessageMenuItem(messageId, appControl, Messages.Popup_MessageTab_OpenMessageInForum, OpenMessageMethod.InForum));

        menu.addSeparator();

        menu.add(new SetThreadReadMenuItem(Messages.Popup_View_SetReadAll, post, true));
        menu.add(new SetThreadReadMenuItem(Messages.Popup_View_SetUnreadAll, post, false));

        menu.add(new ExtendedMarkRead(Messages.Popup_View_ThreadsTree_Mark_Extended, post.getMessageData(), appControl.getMainFrame()));

        menu.addSeparator();
        MenuHelper.addOpenLink(menu, Messages.Popup_Link_Open_InBrowser_Thread, LinkUtils.buildThreadLink(messageId));
        menu.add(MenuHelper.copyLinkSubmenu(messageId));

        menu.addSeparator();
        String text = Messages.Popup_Favorites_Add.get(Messages.Popup_Favorites_Add_Thread.get().toLowerCase(Property.ROJAC_GUI_LOCALE.get()));
        menu.add(new AddToFavoriteMenuItem(text, FavoriteType.Thread, post.getMessageData().getThreadRootId()));

        return menu;
    }

    public static JPopupMenu getMessageViewTabMenu(MessageData message, IAppControl appControl) {
        int messageId = message.getMessageId();
        final JPopupMenu menu = new JPopupMenu("#" + messageId);

        JMenuItem item = new JMenuItem("#" + messageId);
        item.setEnabled(false);

        menu.add(item);

        menu.add(new OpenMessageMenuItem(messageId, appControl, Messages.Popup_MessageTab_OpenMessageInThread, OpenMessageMethod.InThread));
        menu.add(new OpenMessageMenuItem(messageId, appControl, Messages.Popup_MessageTab_OpenMessageInForum, OpenMessageMethod.InForum));

        menu.addSeparator();
        MenuHelper.addOpenLink(menu, Messages.Popup_Link_Open_InBrowser_Message, LinkUtils.buildMessageLink(messageId));
        MenuHelper.addOpenLink(menu, Messages.Popup_Link_Open_InBrowser_Thread, LinkUtils.buildThreadLink(messageId));
        menu.add(MenuHelper.copyLinkSubmenu(messageId));

        return menu;
    }

    public static JPopupMenu getRecentPostsMenu(MessageData threadRoot, IAppControl appControl) {
        int messageId = threadRoot.getMessageId();
        final JPopupMenu menu = new JPopupMenu("#" + messageId);

        JMenuItem item = new JMenuItem("#" + messageId);
        item.setEnabled(false);

        menu.add(item);

        menu.add(new OpenMessageMenuItem(messageId, appControl, Messages.Popup_MessageTab_OpenMessageInThread, OpenMessageMethod.InThread));
        menu.add(new OpenMessageMenuItem(messageId, appControl, Messages.Popup_MessageTab_OpenMessageInForum, OpenMessageMethod.InForum));

        menu.addSeparator();

        menu.add(new SetThreadReadMenuItem(Messages.Popup_View_SetReadAll, threadRoot, true));
        menu.add(new SetThreadReadMenuItem(Messages.Popup_View_SetUnreadAll, threadRoot, false));

        menu.addSeparator();
        MenuHelper.addOpenLink(menu, Messages.Popup_Link_Open_InBrowser_Thread, LinkUtils.buildThreadLink(messageId));
        menu.add(MenuHelper.copyLinkSubmenu(messageId));

        menu.addSeparator();
        String text = Messages.Popup_Favorites_Add.get(Messages.Popup_Favorites_Add_Thread.get().toLowerCase(Property.ROJAC_GUI_LOCALE.get()));
        menu.add(new AddToFavoriteMenuItem(text, FavoriteType.Thread, threadRoot.getThreadRootId()));

        return menu;
    }

    public static JPopupMenu getMessagesListTabMenu(Post post, IAppControl appControl) {
        JPopupMenu menu = new JPopupMenu("#");

        menu.add(new SetMessageListReadMenuItem(Messages.Popup_View_SetReadAll, post, true));
        menu.add(new SetMessageListReadMenuItem(Messages.Popup_View_SetUnreadAll, post, false));

        menu.add(new ExtendedMarkRead(Messages.Popup_View_ThreadsTree_Mark_Extended, appControl.getMainFrame()));

        return menu;
    }
}