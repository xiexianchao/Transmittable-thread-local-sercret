package com.xiechao;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static com.xiechao.InheritableThreadLocalEnhance.Transmitter.capture;
import static com.xiechao.InheritableThreadLocalEnhance.Transmitter.replay;
import static com.xiechao.InheritableThreadLocalEnhance.Transmitter.restore;

public class RunnableEnhance implements Runnable {
    private final AtomicReference<Object> capturedRef;
    private final Runnable runnable;
    private final boolean releaseTtlValueReferenceAfterRun;

    private RunnableEnhance(Runnable runnable, boolean relesaeTtcValueReferenceAfterRun){
        this.releaseTtlValueReferenceAfterRun = relesaeTtcValueReferenceAfterRun;
        this.runnable = runnable;
        this.capturedRef = new AtomicReference<Object>(capture());
        System.out.println("捕获到" + Thread.currentThread().getName() +"线程中的inheritableThreadLocals:"
                + this.capturedRef.get());
    }
    public Runnable getRunnable(){
        return this.runnable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RunnableEnhance that = (RunnableEnhance) o;
        return Objects.equals(runnable, that.runnable);
    }

    @Override
    public int hashCode() {

        return Objects.hash(runnable);
    }

    @Override
    public String toString(){
        return this.getClass().getName() + "-" + runnable.toString();
    }

    public static RunnableEnhance get(Runnable runnable){
        return get(runnable,false,false);
    }
    public static RunnableEnhance get(Runnable runnable,boolean releaseTtlValueReferenceAfterRun){
        return get(runnable,releaseTtlValueReferenceAfterRun,false);
    }

    public static RunnableEnhance get(Runnable runnable,boolean releaseTtlValueReferenceAfterRun,boolean idempotent){
        if(null == runnable){
            return null;
        }
        if(runnable instanceof RunnableEnhance){
            if(idempotent){
                return (RunnableEnhance) runnable;
            }else{
                throw new IllegalStateException("Already RunnableEnhance");
            }
        }
        return new RunnableEnhance(runnable,releaseTtlValueReferenceAfterRun);
    }


    @Override
    public void run() {
        Object captured = capturedRef.get();  //获得parent thread的InheritableThreadLocalEnhance
        if(null == captured || releaseTtlValueReferenceAfterRun && ! capturedRef.compareAndSet(captured,null)){
            throw new IllegalStateException("TTL value reference is released after run!");
        }


        Object backup = replay(captured);  //先备份本线程的holder，但holder.size is always 0。再把captured放到本线程的holder中
      //  Object backup =  new HashMap<InheritableThreadLocalEnhance<?>,Object>();

        try{
            runnable.run();
        }finally {
            //clear
            restore(backup);
        }

    }
}
