package Demo;

import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

/**
 * @author luojbin
 * @version 1.0
 * @create 2018/7/31 14:49
 */
public class BasicTest {

    protected Jedis jedis;

    @Before
    public void init() {
        // 创建连接, 并清空所有库
        jedis = new Jedis("47.106.82.153",6379);
        jedis.auth("luojbin2Redis");
        jedis.flushAll();

        // // 添加预设数据
        // // string-string
        // jedis.set("str1", "str1-v");
        // jedis.set("str2", "str2-v");
        // jedis.set("str3", "str3-v");
        // jedis.set("str4", "str4-v");
        // jedis.set("str5", "str5-v");
        //
        // // string-hash
        // jedis.hset("hash1", "field1", "hash1-f1");
        // jedis.hset("hash1", "field2", "hash1-f2");
        // jedis.hset("hash2", "field3", "hash2-f3");
        // jedis.hset("hash2", "field4", "hash2-f4");
        // jedis.hset("hash3", "field5", "hash3-f5");
        // jedis.hset("hash3", "field6", "hash3-f6");
        //
        // // string-list
        // jedis.lpush("list1","e1", "e2", "e3");
        // jedis.lpush("list1","e4", "e5", "e6");
        //
        // // string-set
        // jedis.sadd("set1", "e1", "e2", "e3");
        // jedis.sadd("set1", "e4", "e5", "e6");
        //
        // // string-zset
        // jedis.zadd("zset", 1, "e1");
        // jedis.zadd("zset",2, "e2");
        // jedis.zadd("zset", 1.5, "e3");
    }

}
