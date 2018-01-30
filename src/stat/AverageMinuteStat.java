package stat;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by yangxuehua on 2017/8/18.
 * 按分钟计平均数
 */
public class AverageMinuteStat extends AbstractMinuteStat {
    private int periods = 10;

    private AtomicLong numAmount;
    private AtomicLong numCount;
    private LinkedList<Status> stackStatus;

    public AverageMinuteStat(int maxMinutes) {
        super();
        if (maxMinutes <= 1) {
            maxMinutes = 1;
        }
        this.periods = maxMinutes + 1;
        this.numAmount = new AtomicLong(0L);
        this.numCount = new AtomicLong(0L);
        this.stackStatus = new LinkedList<Status>();
    }

    public static void main(String... avgs) {
        LinkedList<Integer> list = new LinkedList<Integer>();
        list.offerFirst(1);
        list.offerFirst(2);
        list.offerFirst(3);
        list.offer(4);
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
        }
    }

    public void destroy() {
        super.destroy();
    }

    public void addNum(long num) {
        this.numAmount.addAndGet(num);
        this.numCount.incrementAndGet();
    }

    @Override
    protected void pollCheck() {
        Status status = new Status();
        status.amount = this.numAmount.get();
        status.count = this.numCount.get();
        stackStatus.offerFirst(status);
        if (stackStatus.size() > periods) {
            stackStatus.removeLast();
        }
    }

    /**
     * @param minutes 最近minutes分钟内的平均数
     * @return
     */
    public Average getAverage(int minutes) {
        if (minutes < 1) {
            minutes = 1;
        }
        if (stackStatus.size() > minutes) {
            Status end = stackStatus.get(0);
            Status begin = stackStatus.get(minutes);
            long count = end.count - begin.count;
            long amount = end.amount - begin.amount;
            long ave = count > 0 ? amount / count : -1;

            Average average = new Average();
            average.average = ave;
            average.count = count;
            average.seconds = minutes * secoundsPerPeriod;
            return average;
        } else {
            if (periods <= minutes) {
                throw new IllegalArgumentException("AverageMinuteStat(maxMinutes),getAverage(intminutes): please make" +
                        " sure minutes <= maxMinutes");
            }
            return null;
        }
    }

    private static class Status {
        private long count;
        private long amount;
    }

    public static class Average {
        private long count;
        private long average;
        private int seconds;

        public long getCount() {
            return count;
        }

        public long getAverage() {
            return average;
        }

        public int getSeconds() {
            return seconds;
        }

        @Override
        public String toString() {
            return "Average{" +
                    "count=" + count +
                    ", average=" + average +
                    ", seconds=" + seconds +
                    '}';
        }
    }

}
