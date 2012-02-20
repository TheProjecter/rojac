package org.xblackcat.rojac.service.storage.database;

import org.xblackcat.rojac.service.progress.IProgressListener;
import org.xblackcat.rojac.service.storage.StorageCheckException;

/**
 * 27.09.11 17:17
 *
 * @author xBlackCat
 */
public interface IStructureChecker {
    /**
     * Checks and initialize database structure.
     *
     * @throws org.xblackcat.rojac.service.storage.StorageCheckException
     */
    void check() throws StorageCheckException;

    /**
     * Checks and initialize database structure.
     *
     * @throws StorageCheckException
     * @param progressListener
     */
    void check(IProgressListener progressListener) throws StorageCheckException;

    /**
     * Check database structure and initialize it if onlyTest is false.
     *
     * @param onlyTest if true perform only check without initialization.
     * @throws org.xblackcat.rojac.service.storage.StorageCheckException thrown if database check not passed.
     */
    void check(boolean onlyTest) throws StorageCheckException;

    /**
     * Check database structure and initialize it if onlyTest is false.
     *
     * @param onlyTest if true perform only check without initialization.
     * @param l
     * @throws StorageCheckException thrown if database check not passed.
     */
    void check(boolean onlyTest, IProgressListener progressListener) throws StorageCheckException;
}