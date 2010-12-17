package org.xblackcat.rojac.gui.view.model;

import org.xblackcat.rojac.service.storage.StorageException;
import org.xblackcat.rojac.util.RojacWorker;

import java.util.List;

/**
 * @author xBlackCat
 */

abstract class AnItemFavorite extends AFavorite {
    protected final int itemId;

    protected Integer amount = null;

    protected AnItemFavorite(int id, String config) {
        this(id, Integer.parseInt(config));
    }

    public AnItemFavorite(int id, int itemId) {
        super(id);

        this.itemId = itemId;
    }

    @Override
    public String getConfig() {
        return String.valueOf(itemId);
    }

    @Override
    public boolean isHighlighted() {
        return amount != null && amount > 0;
    }

    @Override
    public String getStatistic() {
        if (amount != null) {
            return String.valueOf(amount);
        } else {
            return "...";
        }
    }

    @Override
    public void updateStatistic(Runnable callback) {
        new ValuesLoader(callback).execute();
    }

    protected abstract int loadAmount() throws StorageException;

    private class ValuesLoader extends RojacWorker<Void, Void> {
        private int amount;
        private String name;
        private final Runnable callback;
        private final boolean initName = isNameDefault();

        public ValuesLoader(Runnable callback) {
            this.callback = callback;
        }

        @Override
        protected Void perform() throws Exception {
            amount = loadAmount();
            if (initName) {
                name = loadName();
            }

            publish();
            return null;
        }

        @Override
        protected void process(List<Void> chunks) {
            AnItemFavorite.this.amount = amount;
            if (initName) {
                AnItemFavorite.this.setName(name);
            }
            callback.run();
        }

        @Override
        protected void done() {
        }
    }
}