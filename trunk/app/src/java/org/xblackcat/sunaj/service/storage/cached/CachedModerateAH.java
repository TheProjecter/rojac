package org.xblackcat.sunaj.service.storage.cached;

import org.xblackcat.sunaj.data.Moderate;
import org.xblackcat.sunaj.service.storage.IModerateAH;
import org.xblackcat.sunaj.service.storage.StorageException;

/**
 * Date: 16.04.2007
 *
 * @author ASUS
 */
final class CachedModerateAH implements IModerateAH,IPurgable {
    private final Cache<Moderate[]> messageModaratesCache = new Cache<Moderate[]>();

    private final IModerateAH moderateAH;

    CachedModerateAH(IModerateAH moderateAH) {
        this.moderateAH = moderateAH;
    }

    public void storeModerateInfo(Moderate mi) throws StorageException {
        moderateAH.storeModerateInfo(mi);
        messageModaratesCache.remove(mi.getMessageId());
    }

    public boolean removeModerateInfosByMessageId(int id) throws StorageException {
        if (moderateAH.removeModerateInfosByMessageId(id)) {
            messageModaratesCache.remove(id);
            return true;
        } else {
            return false;
        }
    }

    public Moderate[] getModeratesByMessageId(int messageId) throws StorageException {
        Moderate[] moderates = messageModaratesCache.get(messageId);
        if (moderates != null) {
            moderates = moderateAH.getModeratesByMessageId(messageId);
            if (moderates != null) {
                messageModaratesCache.put(messageId, moderates);
            }
        }
        return moderates;
    }

    public void purge() {
        messageModaratesCache.purge();
    }
}
