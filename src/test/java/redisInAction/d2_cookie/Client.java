package redisInAction.d2_cookie;

import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

/**
 * @author luojbin
 * @version 1.0
 * @time 2018-08-21
 */
public class Client {
    protected Jedis jedis;

    @Before
    public void init() {
        // 创建连接
        jedis = new Jedis("47.106.82.153", 6379);
        // 授权
        jedis.auth("luojbin2Redis");
        // 选择数据库
        jedis.select(2);
    }

}