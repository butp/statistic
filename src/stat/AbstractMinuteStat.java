package stat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by yangxuehua on 2017/8/18.
 * 按分钟统计容器抽象类
 */
public abstract class AbstractMinuteStat {
    protected final static int secoundsPerPeriod = 60;
    private final static LinkedList<AbstractMinuteStat> _instances = new LinkedList<AbstractMinuteStat>();
    private static ScheduledExecutorService scheduExec = null;

    static {
        if (scheduExec == null) {
            //从下分钟的01秒开始计算
            int initialDelay = (61 - Calendar.getInstance().get(Calendar.SECOND)) % 60;
            scheduExec = Executors.newScheduledThreadPool(1);
            scheduExec.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        // System.out.println("poll:"+_instances.size());
                        if (_instances.size() > 0) {
                            List<AbstractMinuteStat> all = new ArrayList<AbstractMinuteStat>();
                            all.addAll(_instances);
                            for (AbstractMinuteStat _instance : all) {
                                _instance.pollCheck();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, initialDelay, secoundsPerPeriod, TimeUnit.SECONDS);
        }
    }

    protected AbstractMinuteStat() {
        synchronized (_instances) {
            _instances.addFirst(this);
        }
    }

    public void destroy() {
        synchronized (_instances) {
            _instances.remove(this);
        }
    }

    protected abstract void pollCheck();

}
