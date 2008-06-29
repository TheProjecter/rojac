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

    private final IUserAH userDAO;

    CachedUserAH(IUserAH userDAO) {
        this.userDAO = userDAO;
    }

    public void storeUser(User ui) throws StorageException {
        userDAO.storeUser(ui);
    }

    public boolean removeUser(int id) throws StorageException {
        if (userDAO.removeUser(id)) {
            userCache.remove(id);
            return true;
        } else {
            return false;
        }
    }

    public User getUserById(int id) throws StorageException {
        User user = userCache.get(id);
        if (user == null) {
            user = userDAO.getUserById(id);
            if (user != null) {
                userCache.put(id, user);
            }
        }
        return user;
    }

    public int[] getAllUserIds() throws StorageException {
        return userDAO.getAllUserIds();
    }

    public void purge() {
        userCache.purge();
    }
}