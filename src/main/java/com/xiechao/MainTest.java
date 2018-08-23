package com.xiechao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainTest {
    public static void main(String...args) throws InterruptedException {
        final InheritableThreadLocalEnhance<String> inheritableThreadLocalEnhance = new InheritableThreadLocalEnhance<>();
        inheritableThreadLocalEnhance.set("xiechao");


        System.out.println("==== " + Thread.currentThread().getName() + " ===");
        System.out.println(inheritableThreadLocalEnhance.get());
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
           //    final InheritableThreadLocalEnhance<String> inheritableThreadLocalEnhance1 = new InheritableThreadLocalEnhance<>();
            //   inheritableThreadLocalEnhance1.set("帕达欣");

                System.out.println("==== " + Thread.currentThread().getName() + " ===");
                System.out.println(inheritableThreadLocalEnhance.get());


                inheritableThreadLocalEnhance.set("zhangyuge");
        //        System.out.println("==== " + Thread.currentThread().getName() + " ===");
                System.out.println(inheritableThreadLocalEnhance.get());
            }
        };
        RunnableEnhance runnableEnhance = RunnableEnhance.get(runnable);



        TimeUnit.SECONDS.sleep(1);
        executorService.submit(runnableEnhance);
        TimeUnit.SECONDS.sleep(1);
        executorService.submit(runnableEnhance);
        TimeUnit.SECONDS.sleep(1);
        System.out.println("==== " + Thread.currentThread().getName() + " ===");
        System.out.println(inheritableThreadLocalEnhance.get());
      //  executorService.shutdown();
        System.out.println("**********************");
        inheritableThreadLocalEnhance.set("帕达欣");
        RunnableEnhance runnableEnhance1 = RunnableEnhance.get(runnable);
        executorService.submit(runnableEnhance1);
        TimeUnit.SECONDS.sleep(1);
        executorService.submit(runnableEnhance1);
        TimeUnit.SECONDS.sleep(1);

        System.out.println("==== " + Thread.currentThread().getName() + " ===");
        System.out.println(inheritableThreadLocalEnhance.get());
        executorService.shutdown();
    }
}
