package org.xblackcat.rojac.gui.view.thread;

/**
 * Interface to avoid sub-classing Thread view class.
 */

interface IThreadControl<T extends ITreeItem<T>> {
    /**
     * (Re-)initializes threads model according to type of thread view.
     *
     * @param model  model to be initialized.
     *
     * @param itemId item id to identify how to initialize model.
     * @return forum id the view is belonged to. Used to load forum information.
     */
    int loadThreadByItem(AThreadModel<T> model, int itemId);

    /**
     * Updates the item information.
     *
     * @param model  item container model.
     * @param itemId item to be updated.
     */
    void updateItem(AThreadModel<T> model, int... itemId);

    /**
     * Initializes a procedure to load children of the item.
     *
     * @param model
     * @param item
     */
    void loadChildren(AThreadModel<T> model, T item);

    /**
     * Returns root item visibility state.
     * @return <code>true</code> if root item should be visible and <code>false</code> elsewise.
     */
    boolean isRootVisible();
}
