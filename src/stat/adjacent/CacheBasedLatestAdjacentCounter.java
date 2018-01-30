package stat.adjacent;

import com.qlc.common.util.cache.ICacheDB;

import java.util.Map;

/**
 * 指定时间的最近连续发生次数计数器（分布式管理，基于cacheDB）
 * Created by yangxuehua on 2017/8/18.
 */
public class CacheBasedLatestAdjacentCounter implements IAdjacentCounter {
    private final static String CACHE_PREFIX = "DistLatestCounter_";
    private final int threshold;
    private final int seconds;//统计周期包含的分钟数。有效值[5,24*3600]
    private final ICacheDB cacheDB;
    private final int millsPerNode;
    private final String counterId;

    /**
     * @param cacheDB   缓存，用于存储计数
     * @param counterId 计数器唯一id
     * @param seconds   统计周期包含的秒数.有效值[5,24*3600],即不超过1天
     * @param threshold 每自然日连续发生次数最大阀值
     */
    public CacheBasedLatestAdjacentCounter(ICacheDB cacheDB, String counterId, int seconds, int threshold) {
        this.cacheDB = cacheDB;
        this.counterId = counterId;
        if (seconds < 5) {
            seconds = 5;
        } else if (seconds > 24 * 3600) {
            seconds = 24 * 3600;
        }
        this.threshold = threshold;
        this.seconds = seconds;
        if (seconds <= 2 * 60) {
            this.millsPerNode = 1000;
        } else if (seconds <= 10 * 60) {
            this.millsPerNode = 6000;
        } else {
            this.millsPerNode = 60000;
        }
    }

    @Override
    public synchronized void hit() {
        clearInvalid();

        String key = getKey();
        String field = String.valueOf(System.currentTimeMillis() / millsPerNode);
        Integer count = cacheDB.hGetObject(key, field, Integer.class);
        if (count == null) {
            cacheDB.hSetObject(key, field, 1);
            cacheDB.expire(key, seconds);
        } else {
            cacheDB.hSetObject(key, field, count + 1);
            cacheDB.expire(key, seconds);
        }
    }

    @Override
    public synchronized void miss() {
        cacheDB.delete(getKey());
    }

    @Override
    public int adjacentHitCount() {
        clearInvalid();
        int count = 0;
        Map<String, Integer> map = cacheDB.hGetAllObject(getKey(), Integer.class);
        if (map != null) {
            for (int i : map.values()) {
                count += i;
            }
        }
        return count;
    }

    @Override
    public boolean isHitOverLimit() {
        return adjacentHitCount() >= threshold;
    }

    private synchronized void clearInvalid() {
        String time = String.valueOf((System.currentTimeMillis() - seconds * 1000) / millsPerNode);
        String key = getKey();
        Map<String, Integer> map = cacheDB.hGetAllObject(key, Integer.class);
        if (map != null) {
            for (String field : map.keySet()) {
                if (field.compareTo(time) <= 0) {
                    cacheDB.hDelete(key, field);
                }
            }
        }
    }

    private String getKey() {
        return CACHE_PREFIX + counterId;
    }

}
