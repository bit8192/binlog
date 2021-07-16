package cn.bincker.web.blog.base.service.impl;

import cn.bincker.web.blog.base.exception.SystemException;
import cn.bincker.web.blog.base.service.ISystemCacheService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class MemSystemCacheServiceImpl implements ISystemCacheService, DisposableBean {
    private static final long CLEAR_TIMEOUT = 5 * 60 * 1000L;//定时清理时间(5分钟)
    private final Map<String, Object> cacheMap;//所有数据将存在这里
    private final PriorityQueue<ExpireInfo> expireInfoQueue;//存储过期信息，定时清理
    @SuppressWarnings("FieldCanBeLocal")
    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    public MemSystemCacheServiceImpl() {
        cacheMap = new ConcurrentHashMap<>();
        expireInfoQueue = new PriorityQueue<>((a, b)-> (int) (a.expire - b.expire));
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        scheduledThreadPoolExecutor.scheduleAtFixedRate(getClearRunnable(), CLEAR_TIMEOUT, CLEAR_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    private Runnable getClearRunnable() {
        return ()->{
            var currentTimeMillis = System.currentTimeMillis();
            ExpireInfo expireInfo;
            synchronized (expireInfoQueue){
                while ((expireInfo = expireInfoQueue.peek()) != null) {
                    if (currentTimeMillis - expireInfo.expire > 0) {
                        expireInfoQueue.poll();
                        cacheMap.remove(expireInfo.key);
                    } else {//否则跳过，不再进行检查，因为后面的时间都更大
                        break;
                    }
                }
            }
        };
    }

    @Override
    public void put(String key, Object obj) {
        this.cacheMap.put(key, obj);
    }

    @Override
    public void put(String key, Object obj, Long aliveTime) {
        synchronized (this.expireInfoQueue) {
            expireInfoQueue.removeIf(e->{//删掉先前的过期信息，不删的话会被常驻定时任务清除掉
                var result = e.key.equals(key);
                if(result && !e.scheduledFuture.isCancelled() && !e.scheduledFuture.isDone()){
                    e.scheduledFuture.cancel(false); //为了安全，还是不打断吧
                }
                return result;
            });
            var expireInfo = new ExpireInfo(key, System.currentTimeMillis() + aliveTime, null);
            expireInfo.scheduledFuture = scheduledThreadPoolExecutor.schedule(()->{
                synchronized (expireInfoQueue){
                    expireInfoQueue.remove(expireInfo);
                }
                cacheMap.remove(key);
            }, aliveTime, TimeUnit.MILLISECONDS);

            expireInfoQueue.add(expireInfo);
        }
        //放在后面推入，防止在等待时被删掉
        this.cacheMap.put(key, obj);
    }

    @Override
    public void put(String key, Object obj, Duration duration) {
        this.put(key, obj, duration.toMillis());
    }

    @Override
    public Optional<String> getStringValue(String key) {
        return this.getValue(key, String.class);
    }

    @Override
    public void remove(String key) {
        this.cacheMap.remove(key);
        synchronized (expireInfoQueue){
            expireInfoQueue.removeIf(expireInfo -> {
                if(!expireInfo.key.equals(key)) return false;
//                取消定时清理
                if(!expireInfo.scheduledFuture.isDone() && expireInfo.scheduledFuture.isCancelled()) expireInfo.scheduledFuture.cancel(false);
                return true;
            });
        }
    }

    @Override
    public boolean containsKey(String key) {
        return cacheMap.containsKey(key);
    }

    @Override
    public <T> Optional<T> getValue(String key, Class<T> clazz) {
        Object result = cacheMap.get(key);
        if(result == null) return Optional.empty();
        if(!clazz.isAssignableFrom(result.getClass())){
            throw new SystemException("无法将[" + result.getClass() + "]转换为[" + clazz + "]");
        }
        //noinspection unchecked
        return Optional.of((T) result);
    }

    @Override
    public void destroy() {
        scheduledThreadPoolExecutor.shutdownNow();
    }

    private static class ExpireInfo{
        private final String key;
        private final long expire;
        private ScheduledFuture<?> scheduledFuture;

        private ExpireInfo(String key, long expire, ScheduledFuture<?> scheduledFuture) {
            this.key = key;
            this.expire = expire;
            this.scheduledFuture = scheduledFuture;
        }
    }
}
