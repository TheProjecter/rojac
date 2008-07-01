package org.xblackcat.sunaj.service.storage.cached;

import org.xblackcat.sunaj.data.User;
import org.xblackcat.sunaj.service.storage.IUserAH;
import org.xblackcat.sunaj.service.storage.StorageException;

/**
 * Date: 16.04.2007
 *
 * @author ASUS
 */
final class CachedUserAH implements IUserAH, IPurgable {
    private final Cache<User> userCache = new Cache<User>();

    private final IUserAH userAH;

    CachedUserAH(IUserAH userAH) {
        this.userAH = userAH;
    }

    public void storeUser(User ui) throws StorageException {
        userAH.storeUser(ui);
    }

    public boolean removeUser(int id) throws StorageException {
        if (userAH.removeUser(id)) {
            userCache.remove(id);
            return true;
        } else {
            return false;
        }
    }

    public User getUserById(int id) throws StorageException {
        User user = userCache.get(id);
        if (user == null) {
            user = userAH.getUserById(id);
            if (user != null) {
                userCache.put(id, user);
            }
        }
        return user;
    }

    public int[] getAllUserIds() throws StorageException {
        return userAH.getAllUserIds();
    }

    public void purge() {
        userCache.purge();
    }
}
