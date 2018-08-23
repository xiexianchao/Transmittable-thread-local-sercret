package com.xiechao.my.threadLocal;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.xiechao.my.threadLocal.TransmittableThreadLocal.Transmitt.backup;
import static com.xiechao.my.threadLocal.TransmittableThreadLocal.Transmitt.capture;
import static com.xiechao.my.threadLocal.TransmittableThreadLocal.Transmitt.restore;

public class TtlRunnable implements Runnable{

    private AtomicReference<Object> reference ;
    private Runnable runnable;

    private TtlRunnable(Runnable runnable){
        this.runnable = runnable;
        this.reference = new AtomicReference<Object>(capture());
    }

    public static TtlRunnable getRunnable(Runnable runnable){
        return new TtlRunnable(runnable);
    }

    @Override
    public void run() {
        Object captured = reference.get();
        Object backup = backup(captured);
        runnable.run();
        restore(backup);
    }




}
