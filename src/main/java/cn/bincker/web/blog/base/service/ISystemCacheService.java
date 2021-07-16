package cn.bincker.web.blog.base.service;

import java.time.Duration;
import java.util.Optional;

/**
 * 系统缓存服务，用于缓存需要跨session或需要定时清除的数据
 */
public interface ISystemCacheService {
    void put(String key, Object obj);

    /**
     * 设置缓存, 并指定存活时长
     * @param key key
     * @param obj obj
     * @param expire 存活时长（毫秒）, 并非时间
     */
    void put(String key, Object obj, Long expire);

    /**
     * 设置缓存，并指定存活时长
     */
    void put(String key, Object obj, Duration duration);
    Optional<String> getStringValue(String key);
    <T> Optional<T> getValue(String key, Class<T> clazz);
    boolean containsKey(String key);

    void remove(String key);
}
