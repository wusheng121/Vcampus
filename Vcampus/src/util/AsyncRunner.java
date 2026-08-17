package util;

import javax.swing.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 后台异步执行 + EDT 回调的统一入口，包装 SwingWorker。
 * 用于把同步网络调用移出 EDT，避免 UI 冻结，并提供统一加载态/错误提示。
 *
 * <p>用法：
 * <pre>{@code
 *   AsyncRunner.run(
 *       () -> controller.login(id, pwd),          // 后台线程
 *       resp -> { /* EDT: 处理结果 *\/ },          // onDone
 *       () -> btnLogin.setEnabled(false),         // onStart（EDT，进加载态）
 *       () -> btnLogin.setEnabled(true)          // onStop（EDT，退出加载态）
 *   );
 * }</pre>
 */
public final class AsyncRunner {
    private AsyncRunner() {
    }

    public static <T> void run(Supplier<T> background,
                               Consumer<T> onDone,
                               Runnable onStart,
                               Runnable onStop) {
        if (onStart != null) onStart.run();
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return background.get();
            }

            @Override
            protected void done() {
                try {
                    T result = get();
                    if (onDone != null) onDone.accept(result);
                } catch (Exception ex) {
                    Throwable cause = (ex instanceof ExecutionException && ex.getCause() != null)
                            ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(null,
                            "操作失败：" + (cause.getMessage() == null ? cause.toString() : cause.getMessage()),
                            "错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    if (onStop != null) onStop.run();
                }
            }
        }.execute();
    }

    /** 无加载态切换的简化重载。 */
    public static <T> void run(Supplier<T> background, Consumer<T> onDone) {
        run(background, onDone, null, null);
    }
}
