package com.xiechao;

import com.sun.org.apache.regexp.internal.RE;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InheritableThreadLocalEnhance<T> extends InheritableThreadLocal<T>   {
    private static final Logger logger = Logger.getLogger(InheritableThreadLocalEnhance.class.getName());


    protected T copy(T parentValue){
        return parentValue;
    }

    protected void beforeExecute(){}

    protected void afterExecute(){}

    @Override
    public final T get(){
        T value = super.get();          //从InheritableThreadLocal中取出
        if(null != value){
            addValue();                //为什么要put entry<this,null> in holder if absent
        }
        return value;
    }

    @Override
    public final void set(T value){
        super.set(value);            //首先会放到InheritableThreadLocal中去
        if( null == value){
            removeValue();
        }else{
            addValue();                //放到holder中
        }
    }
    @Override
    public final void remove(){
        removeValue();
        super.remove();
    }

    private void superRemove(){
        super.remove();
    }
    private T copyValue(){
        return copy(get());
    }

    //holder中Map的value永远是null
    //static：表示所有InheritableThreadLocalEnhance共享一个holder,但holder中存放的Map每个任务会有一个新的
    private static InheritableThreadLocal<Map<InheritableThreadLocalEnhance<?>,?>> holder =
            new InheritableThreadLocal<Map<InheritableThreadLocalEnhance<?>,?>>(){
                @Override
                protected Map<InheritableThreadLocalEnhance<?>, ?> initialValue() {
                    System.out.println(Thread.currentThread().getName() + "线程holder initialValue");
                    return new WeakHashMap<InheritableThreadLocalEnhance<?>,Object>();
                }

                @Override
                protected Map<InheritableThreadLocalEnhance<?>, ?> childValue(Map<InheritableThreadLocalEnhance<?>, ?> parentValue) {
                //    return super.childValue(parentValue);
                    System.out.println(Thread.currentThread().getName() + "线程执行holder childValue");
                    return new WeakHashMap<InheritableThreadLocalEnhance<?>,Object>();
                }
            };

    //put this InheritableThreadLocalEnhance in holder if absent
    private void addValue(){
        if( ! holder.get().containsKey(this)){
            holder.get().put(this,null);
        }
    }

    private void removeValue(){
        holder.get().remove(this);
    }

    private static void doExecuteCallback(boolean isBefore){
        for (Map.Entry<InheritableThreadLocalEnhance<?>,?> entry:holder.get().entrySet()) { //遍历holder
            InheritableThreadLocalEnhance<?> threadLocalEnhance = entry.getKey();
            try{
                if(isBefore){
                    threadLocalEnhance.beforeExecute();
                }else{
                    threadLocalEnhance.afterExecute();
                }
            }catch (Throwable throwable){
                if(logger.isLoggable(Level.WARNING)){
                    logger.log(Level.WARNING,"InheritableThreadLocalEnhance exception when" +
                            (isBefore? "beforeExecute" : "afterExecute") +
                            ", cause: " + throwable.toString(),throwable);
                }
            }
        }
    }

    static void dump(String title){
        if(title != null && title.length() >0 ){
            System.out.printf("Start InheritableThreadLocalEnhance[%s] Dump...\n", title);
        }else{
            System.out.println("Start InheritableThreadLocalEnhance Dump");
        }
        for (Map.Entry<InheritableThreadLocalEnhance<?>,?> entry:holder.get().entrySet()) {
            final InheritableThreadLocalEnhance<?> key = entry.getKey();
            System.out.println(key.get());
        }
        System.out.println("InheritableThreadLocalEnhance Dump end");
    }
    static void dump(){
        dump(null);
    }


    public static class Transmitter{
        //从holder中获得所有的ThreadLocalEnhance
        //返回一个map，key-InheritableThreadLocalEnhance value-Object
        public static Object capture(){
            Map<InheritableThreadLocalEnhance<?>,Object> captured = new HashMap<>();
            for (InheritableThreadLocalEnhance<?> threadLocalEnhance:holder.get().keySet()) {
                //key-threadLocalEnhance value-threadLocalEnhance.get()
                captured.put(threadLocalEnhance,threadLocalEnhance.copyValue());
            }
            return captured;
        }


        public static Object replay(Object captured){
            @SuppressWarnings("unchecked")
            Map<InheritableThreadLocalEnhance<?>,Object> capturedMap = (Map<InheritableThreadLocalEnhance<?>, Object>) captured;
            Map<InheritableThreadLocalEnhance<?>,Object> backup = new HashMap<InheritableThreadLocalEnhance<?>,Object>();


            //此处holder.size() always is 0
            //遍历holder,取出当前线程所有的InheritableThreadLocalEnhance备份到backup中，
            for (Iterator<? extends Map.Entry<InheritableThreadLocalEnhance<?>,?>> iterator = holder.get().entrySet().iterator();
                    iterator.hasNext();){
                Map.Entry<InheritableThreadLocalEnhance<?>,?> next = iterator.next();
                InheritableThreadLocalEnhance<?> threadLocal = next.getKey();

                //backup
                backup.put(threadLocal,threadLocal.get());

                //holder只保留在captureMap中的InheritableThreadLocalEnhance
                if(! capturedMap.containsKey(threadLocal))
                {
                    iterator.remove();
                    threadLocal.superRemove();
                }
            }
            setTtlValueTo(capturedMap);      //将在父线程中捕获到的InheritablThreadLocalEnhance放到子线程的holder中去
            doExecuteCallback(true);
            return backup;
        }



        public static void restore(Object backup){
            @SuppressWarnings("unchecked")
            Map<InheritableThreadLocalEnhance<?>,Object> backupMap =(Map<InheritableThreadLocalEnhance<?>,Object>) backup;
            doExecuteCallback(false);
            System.out.println(Thread.currentThread().getName() + ",backupMap:" + backupMap);
            //取出当前线程的所有InheritableThreadLocalEnhance
            for (Iterator<? extends Map.Entry<InheritableThreadLocalEnhance<?>,?>> iterator = holder.get().entrySet().iterator();
                    iterator.hasNext();) {

                Map.Entry<InheritableThreadLocalEnhance<?>,?> next = iterator.next();
                InheritableThreadLocalEnhance<?> threadLocal = next.getKey();

                System.out.println(Thread.currentThread().getName() + ",inheritablcThreadLocal-" + threadLocal
                + ",Object-" + threadLocal.get());


                /*只保留在backup中的InheritableThreadLocalEnhance*/
                if(! backupMap.containsKey(threadLocal)){
                    iterator.remove();
                    threadLocal.superRemove();
                }
            }
            for (Map.Entry<InheritableThreadLocalEnhance<?>,?> entry:holder.get().entrySet()) {
                System.out.println(Thread.currentThread().getName() + "then,holder: key-" + entry.getKey()
                + ",value-" + entry.getValue());
            }
            setTtlValueTo(backupMap);
        }

        //把ttlValues中的key-value拿出来，掉用set方法，先放到InheritableThreadLocal中去，在放到holder中去
        private static void setTtlValueTo(Map<InheritableThreadLocalEnhance<?>,Object> ttlValues){
            for (Map.Entry<InheritableThreadLocalEnhance<?>,Object> entry:ttlValues.entrySet()) {
                @SuppressWarnings("unchecked")
                InheritableThreadLocalEnhance<Object> threadLocal = (InheritableThreadLocalEnhance<Object>) entry.getKey();
                threadLocal.set(entry.getValue());
            }
        }



    }















}
