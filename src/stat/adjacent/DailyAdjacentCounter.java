package stat.adjacent;

import com.qlc.common.util.DateUtil;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 按自然日的连续发生次数计数器（未分布式管理）
 * Created by yangxuehua on 2017/8/18.
 */
public class DailyAdjacentCounter implements IAdjacentCounter {
    private final int threshold;//每自然日连续发生次数最大阀值
    private AtomicInteger adjacentHitCount = new AtomicInteger(0);
    private String yyyymmddCount;

    /**
     * @param threshold 每自然日连续发生次数最大阀值
     */
    public DailyAdjacentCounter(int threshold) {
        this.threshold = threshold;
        ensureRightPeriod();
    }

    @Override
    public void hit() {
        ensureRightPeriod();
        adjacentHitCount.incrementAndGet();
    }

    @Override
    public void miss() {
        adjacentHitCount.set(0);
    }

    @Override
    public int adjacentHitCount() {
        ensureRightPeriod();
        return adjacentHitCount.get();
    }

    @Override
    public boolean isHitOverLimit() {
        ensureRightPeriod();
        return adjacentHitCount() >= threshold;
    }

    private void ensureRightPeriod() {
        String yyyymmdd = DateUtil.format2.format(new Date());
        if (!yyyymmdd.equals(yyyymmddCount)) {
            adjacentHitCount.set(0);
            yyyymmddCount = yyyymmdd;
        }
    }
}

