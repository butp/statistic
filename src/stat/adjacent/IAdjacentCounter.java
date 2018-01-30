package stat.adjacent;

/**
 * 连续发生次数计数器。
 * 应用场景：连续输错密码次数限制。指定时间段内短信下发次数限制。。。
 * Created by yangxuehua on 2017/8/18.
 */
public interface IAdjacentCounter {
    /**
     * 命中事件
     */
    void hit();

    /**
     * 未命中
     */
    void miss();

    /**
     * 连续命中记数
     *
     * @return
     */
    int adjacentHitCount();

    /**
     * 连续命中是否超出限制
     *
     * @return
     */
    boolean isHitOverLimit();
}
