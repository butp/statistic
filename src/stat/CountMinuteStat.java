package stat;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by yangxuehua on 2017/8/18.
 * 按分钟计数
 */
public class CountMinuteStat extends AbstractMinuteStat {
    private int periods = 10;

    private AtomicLong amount;
    private LinkedList<Long> stackCount;

    public CountMinuteStat(int maxMinutes) {
        super();
        if (maxMinutes <= 1)
            maxMinutes = 1;
        this.periods = maxMinutes + 1;
        if (periods < 2)
            periods = 2;
        this.amount = new AtomicLong(0L);
        this.stackCount = new LinkedList<Long>();
    }

    public void destroy() {
        super.destroy();
    }

    public long inc() {
        return amount.incrementAndGet();
    }

    public long incBy(int step) {
        return amount.addAndGet(step);
    }

    public long desc() {
        return amount.decrementAndGet();
    }

    public long descBy(int step) {
        return amount.addAndGet(step * -1);
    }

    public long getAndSet(long newValue) {
        return amount.getAndSet(newValue);
    }

    public long getNowValue() {
        return amount.longValue();
    }

    @Override
    protected void pollCheck() {
        stackCount.offerFirst(amount.longValue());
        if (stackCount.size() > periods)
            stackCount.removeLast();
    }

    /**
     * @param minutes 最近minutes分钟内的增长值
     * @return
     */
    public Increase getIncrease(int minutes) {
        if (minutes < 1) {
            minutes = 1;
        }
        if (stackCount.size() > minutes) {
            Increase increase = new Increase();
            increase.setIncrease(stackCount.getFirst().longValue() - stackCount.get(minutes).longValue());
            increase.setSeconds(secoundsPerPeriod * minutes);
            return increase;
        } else {
            if (periods <= minutes) {
                throw new IllegalArgumentException("CountMinuteStat(maxMinutes),getIncrease(intminutes):  please make" +
                        " sure minutes <= maxMinutes");
            }
            return null;
        }
    }

    public static class Increase {
        private long increase;//增长值
        private int seconds;//秒数

        public long getIncrease() {
            return increase;
        }

        public void setIncrease(long increase) {
            this.increase = increase;
        }

        public int getSeconds() {
            return seconds;
        }

        public void setSeconds(int seconds) {
            this.seconds = seconds;
        }

        @Override
        public String toString() {
            return "Increase{" +
                    "increase=" + increase +
                    ", seconds=" + seconds +
                    '}';
        }
    }

}
