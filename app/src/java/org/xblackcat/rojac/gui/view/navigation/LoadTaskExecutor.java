package org.xblackcat.rojac.gui.view.navigation;

import org.xblackcat.rojac.util.RojacWorker;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author xBlackCat
 */
class LoadTaskExecutor extends RojacWorker<Void, LoadTaskExecutor.TaskResult<?>> {
    private final Collection<ALoadTask> tasks = new LinkedList<>();

    @SafeVarargs
    LoadTaskExecutor(Collection<? extends ALoadTask>... otherTasks) {
        for (Collection<? extends ALoadTask> tasks : otherTasks) {
            this.tasks.addAll(tasks);
        }
    }

    public Void perform() {
        for (final ALoadTask task : tasks) {
            try {
                final Object result = task.doBackground();

                @SuppressWarnings({"unchecked"})
                TaskResult taskResult = new TaskResult(task, result);

                publish(taskResult);
            } catch (Exception e) {
                // TODO: do something
            }
        }

        return null;
    }

    @Override
    protected void process(List<TaskResult<?>> chunks) {
        for (TaskResult<?> tr : chunks) {
            tr.doSwing();
        }
    }

    protected static final class TaskResult<T> {
        final ALoadTask<T> task;
        final T result;

        private TaskResult(ALoadTask<T> task, T result) {
            this.result = result;
            this.task = task;
        }

        void doSwing() {
            task.doSwing(result);
        }
    }
}