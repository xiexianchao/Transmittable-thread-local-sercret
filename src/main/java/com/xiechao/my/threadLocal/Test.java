package com.xiechao.my.threadLocal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Test {
    public static void main(String...args) throws InterruptedException {
     TransmittableThreadLocal<String> stringTransmittableThreadLocal = new TransmittableThreadLocal<>();
     stringTransmittableThreadLocal.set("xiechao");

     System.out.println("=== " + Thread.currentThread().getName() + "===");
     System.out.println(stringTransmittableThreadLocal.get());

     Runnable runnable = new Runnable() {
         @Override
         public void run() {
             System.out.println("=== " + Thread.currentThread().getName() + "===");
             System.out.println(stringTransmittableThreadLocal.get());
             stringTransmittableThreadLocal.set("谢超");
             System.out.println(stringTransmittableThreadLocal.get());
         }
     };
     TtlRunnable ttlRunnable = TtlRunnable.getRunnable(runnable);
     ExecutorService service = Executors.newFixedThreadPool(1);
     service.submit(ttlRunnable);
     TimeUnit.SECONDS.sleep(1);
     service.submit(ttlRunnable);
     TimeUnit.SECONDS.sleep(1);
     System.out.println("=== " + Thread.currentThread().getName() + "===");
     System.out.println(stringTransmittableThreadLocal.get());

     service.shutdown();
    }
}
