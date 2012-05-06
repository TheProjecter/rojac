package org.xblackcat.rojac.service.storage.database;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.rojac.service.progress.IProgressListener;
import org.xblackcat.rojac.service.progress.ProgressChangeEvent;
import org.xblackcat.rojac.service.progress.ProgressState;
import org.xblackcat.rojac.service.storage.*;
import org.xblackcat.rojac.service.storage.database.connection.DatabaseSettings;
import org.xblackcat.rojac.service.storage.database.convert.Converters;
import org.xblackcat.rojac.service.storage.database.helper.IQueryHelper;
import org.xblackcat.rojac.service.storage.database.helper.QueryHelperFactory;
import org.xblackcat.rojac.util.DatabaseUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 27.09.11 17:16
 *
 * @author xBlackCat
 */
public class DBStructureChecker implements IStructureChecker {
    private static final Log log = LogFactory.getLog(DBStructureChecker.class);

    private final Map<SQL, List<SQL>> initializationQueries;
    private final IQueryHelper helper;

    public DBStructureChecker(DatabaseSettings settings) throws StorageInitializationException {
        this.helper = QueryHelperFactory.createHelper(settings);
        try {
            initializationQueries = DatabaseUtils.loadInitializeSQLs(helper.getEngine());
        } catch (IOException e) {
            throw new StorageInitializationException("Can not load initialization routines", e);
        }
    }

    @Override
    public void check() throws StorageCheckException {
        check(null);
    }

    @Override
    public void check(IProgressListener progressListener) throws StorageCheckException {
        check(false, progressListener);
    }

    @Override
    public void check(boolean onlyTest) throws StorageCheckException {
        check(onlyTest, null);
    }

    @Override
    public void check(boolean onlyTest, IProgressListener progressListener) throws StorageCheckException {
        if (progressListener == null) {
            //  NPE and null checks avoiding
            progressListener = new IProgressListener() {
                @Override
                public void progressChanged(ProgressChangeEvent e) {
                    // Nothing
                }
            };
        }

        if (log.isInfoEnabled()) {
            log.info("Check database storage structure started [" + helper.getEngine() + "]");
        }

        int amountChecks = initializationQueries.size();
        int checkIdx = 0;

        for (Map.Entry<SQL, List<SQL>> entry : initializationQueries.entrySet()) {
            boolean success = false;
            SQL check = entry.getKey();
            if (log.isTraceEnabled()) {
                log.trace("Perform check " + check);
            }
            progressListener.progressChanged(new ProgressChangeEvent(this, ProgressState.Start, checkIdx, amountChecks));

            try {
                Boolean c = helper.executeSingle(Converters.TO_BOOLEAN, check.getSql());
                success = Boolean.TRUE.equals(c);
            } catch (StorageException | IllegalStateException e) {
                if (log.isTraceEnabled()) {
                    log.trace(check + " check failed. Reason: " + e.getLocalizedMessage());
                }
            }

            if (!success) {
                if (onlyTest) {
                    throw new StorageCheckException("Database has invalid structure: " + check + " check failed");
                }

                // If c is null or FALSE - abort.
                if (log.isTraceEnabled()) {
                    log.trace(check + " check failed. Perform initialization.");
                }

                int initAmount = entry.getValue().size();

                int initIdx = checkIdx * initAmount;
                int initBound = amountChecks * initAmount;

                for (SQL sql : entry.getValue()) {
                    try {
                        if (log.isTraceEnabled()) {
                            log.trace("Perform initialization command " + sql);
                        }
                        progressListener.progressChanged(new ProgressChangeEvent(this, ProgressState.Work, initIdx, initBound));
                        if (sql.isSimpleQuery()) {
                            helper.update(sql.getSql());
                        } else {
                            String queries[] = sql.getSql().split(";");

                            String getRowsQuery = queries[0];

                            queries = ArrayUtils.subarray(queries, 1, queries.length);

                            Collection<Object[]> rows = new ArrayList<>();

                            try (IResult<Object[]> rowsList = helper.execute(Converters.TO_OBJECT_ROW_CONVERTER, getRowsQuery)) {
                                CollectionUtils.addAll(rows, rowsList.iterator());
                            }

                            for (String query : queries) {
                                helper.updateBatch(query, null, rows);
                            }
                        }
                    } catch (StorageException | IllegalStateException e) {
                        log.error("Can not perform initialization procedure " + sql, e);
                        throw new StorageCheckException("Can not execute " + sql, e);
                    }

                    initIdx++;
                }

                // Perform post-check
                try {
                    if (log.isTraceEnabled()) {
                        log.trace("Perform post-initialization check " + check);
                    }
                    Boolean c = helper.executeSingle(Converters.TO_BOOLEAN, check.getSql());
                    success = Boolean.TRUE.equals(c);
                } catch (StorageException e) {
                    throw new StorageCheckException("Post check failed for " + check, e);
                }

                if (!success) {
                    throw new StorageCheckException("Post check failed for " + check);
                }
            }

            checkIdx++;
        }
    }

}
