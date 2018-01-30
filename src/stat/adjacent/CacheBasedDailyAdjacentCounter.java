package stat.adjacent;

import com.qlc.common.util.DateUtil;
import com.qlc.common.util.cache.ICacheDB;

import java.util.Date;

/**
 * 按自然日的连续发生次数分布式计数器（基于分布式Cache）
 * Created by yangxuehua on 2017/8/18.
 */
public class CacheBasedDailyAdjacentCounter implements IAdjacentCounter {
    private final static String CACHE_PREFIX = "DistDailyCounter_";
    private final static int CACHE_EXPIRE = 24 * 3600;
    private final int threshold;
    private final ICacheDB cacheDB;
    private final String counterId;

    /**
     * @param cacheDB   缓存，用于存储计数
     * @param counterId 计数器唯一id
     * @param threshold 每自然日连续发生次数最大阀值
     */
    public CacheBasedDailyAdjacentCounter(ICacheDB cacheDB, String counterId, int threshold) {
        this.threshold = threshold;
        this.cacheDB = cacheDB;
        this.counterId = counterId;
    }

    @Override
    public void hit() {
        cacheDB.incrBy(getKey(), 1);
        cacheDB.expire(getKey(), CACHE_EXPIRE);
    }

    @Override
    public void miss() {
        cacheDB.delete(getKey());
    }

    @Override
    public int adjacentHitCount() {
        return (int) cacheDB.incrDecrGet(getKey());
    }

    @Override
    public boolean isHitOverLimit() {
        return adjacentHitCount() >= threshold;
    }

    private String getKey() {
        return CACHE_PREFIX + counterId + "_" + DateUtil.format2.format(new Date());
    }
}

