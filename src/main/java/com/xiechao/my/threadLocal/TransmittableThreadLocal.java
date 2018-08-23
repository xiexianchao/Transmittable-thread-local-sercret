package com.xiechao.my.threadLocal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

public class TransmittableThreadLocal<T> extends InheritableThreadLocal<T> {
    @Override
    public T get() {
      //  return super.get();
       T value = super.get();
       if(holder.get().containsKey(this)) {
           return value;
       } else {
           return null;
       }
    }

    @Override
    public void set(T value) {
        super.set(value);
        if(null == value){
            if(holder.get().containsKey(this)) {
                holder.get().remove(this);
            }
        }else{
            holder.get().put(this,null);
        }
    }

    @Override
    public void remove() {
        super.remove();
        if(holder.get().containsKey(this)){
            holder.get().remove(this);
        }
    }

    private static InheritableThreadLocal<Map<TransmittableThreadLocal<?>,?>> holder =
            new InheritableThreadLocal<Map<TransmittableThreadLocal<?>,?>>(){
                @Override
                protected Map<TransmittableThreadLocal<?>, ?> initialValue() {
                    //return super.initialValue();
                    return new WeakHashMap<>();
                }

                @Override
                protected Map<TransmittableThreadLocal<?>, ?> childValue(Map<TransmittableThreadLocal<?>, ?> parentValue) {
                   // return super.childValue(parentValue);
                    return new WeakHashMap<>(parentValue);
                }
            };


    static class Transmitt{
        public static Object capture(){
            Map<TransmittableThreadLocal<?>,Object> map = new HashMap<>();
            for (Map.Entry<TransmittableThreadLocal<?>,?> entry:holder.get().entrySet()) {
                TransmittableThreadLocal<?> threadLocal = entry.getKey();
                map.put(threadLocal,threadLocal.get());
            }
            return map;
        }

        public static Object backup(Object captured){
            @SuppressWarnings("unchecked")
            Map<TransmittableThreadLocal<?>,Object> capturedMap = (Map<TransmittableThreadLocal<?>,Object>) captured;
            Map<TransmittableThreadLocal<?>,Object> backupMap = new HashMap<>();
            for (Iterator<? extends  Map.Entry<TransmittableThreadLocal<?>,?>> iterator = holder.get().entrySet().iterator();iterator.hasNext();)
            {
                @SuppressWarnings("unchecked")
                Map.Entry<TransmittableThreadLocal<?>,Object> entry = (Map.Entry<TransmittableThreadLocal<?>, Object>) iterator.next();
                TransmittableThreadLocal<Object> threadLocal = (TransmittableThreadLocal<Object>) entry.getKey();

                backupMap.put(threadLocal,threadLocal.get());

                if(! capturedMap.containsKey(threadLocal)) {
                    iterator.remove();
                }
            }
            setValueTo(capturedMap);

            return backupMap;
        }

        private static void setValueTo(Map<TransmittableThreadLocal<?>, Object> capturedMap) {
            for (Map.Entry<TransmittableThreadLocal<?>,Object> entry:capturedMap.entrySet()) {
                @SuppressWarnings("unchecked")
                TransmittableThreadLocal<Object> threadLocal = (TransmittableThreadLocal<Object>) entry.getKey();
                threadLocal.set(entry.getValue());
            }
        }


        public static void restore(Object object){
            @SuppressWarnings("unchecked")
            Map<TransmittableThreadLocal<?>,Object> backupMap = (Map<TransmittableThreadLocal<?>, Object>) object;
            for (Iterator<? extends Map.Entry<TransmittableThreadLocal<?>,?>> iterator = holder.get().entrySet().iterator() ;
                    iterator.hasNext();) {
                Map.Entry<TransmittableThreadLocal<?>, ?> next = iterator.next();
                TransmittableThreadLocal<?> threadLocal =  next.getKey();
                if(! backupMap.containsKey(threadLocal)){
                    iterator.remove();
                }
            }
            setValueTo(backupMap);
        }
    }

}
