package stat.adjacent;

import java.util.LinkedList;

/**
 * 指定时间的最近连续发生次数计数器（未分布式管理）
 * Created by yangxuehua on 2017/8/18.
 */
public class LatestAdjacentCounter implements IAdjacentCounter {
    private final int threshold;//连续发生次数最大阀值
    private final int seconds;//统计周期包含的分钟数。有效值[5,24*3600]
    private final int millsPerNode;
    private LinkedList<Entry<Long, Integer>> adjacentHitCountList = new LinkedList<>();

    /**
     * @param threshold 连续发生次数最大阀值
     * @param seconds   统计周期包含的秒数.有效值[5,24*3600],即不超过1天
     */
    public LatestAdjacentCounter(int seconds, int threshold) {
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

        Entry<Long, Integer> time2count = adjacentHitCountList.size() > 0 ? adjacentHitCountList.getLast() : null;
        long time = System.currentTimeMillis() / millsPerNode;
        if (time2count == null || time > time2count.k) {
            time2count = new Entry<>(time, 0);
            adjacentHitCountList.addLast(time2count);
        }
        time2count.v++;
    }

    @Override
    public synchronized void miss() {
        adjacentHitCountList.clear();
    }

    @Override
    public int adjacentHitCount() {
        clearInvalid();
        int count = 0;
        for (Entry<Long, Integer> time2count : adjacentHitCountList) {
            count += time2count.v;
        }
        return count;
    }

    @Override
    public boolean isHitOverLimit() {
        return adjacentHitCount() >= threshold;
    }

    private synchronized void clearInvalid() {
        long time = (System.currentTimeMillis() - seconds * 1000) / millsPerNode;
        while (adjacentHitCountList.size() > 0 && adjacentHitCountList.getFirst().k.compareTo(time) <= 0) {
            adjacentHitCountList.removeFirst();
        }
    }

    private static class Entry<K, V> {
        private K k;
        private V v;

        public Entry(K k, V v) {
            this.k = k;
            this.v = v;
        }
    }
}
