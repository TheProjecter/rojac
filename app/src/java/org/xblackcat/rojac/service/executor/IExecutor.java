package org.xblackcat.rojac.service.executor;

/**
 * Date: 31 ���� 2008
 *
 * @author xBlackCat
 */

public interface IExecutor {
    void execute(Runnable target, TaskType type);

    void execute(Runnable target);
}
