package Demo;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author luojbin
 * @version 1.0
 * @create 2018/7/31 14:53
 */
public class D1_BasicTypes extends BasicTest {

    @Test
    public void testConection() {
        // 通过 ping 方法测试是否连接上 redis 服务器
        System.out.println(jedis.ping());
    }

    @Test
    public void testString() {
        // 使用 set 添加一个 string-string
        System.out.println(jedis.set("key_str_1", "val_str_1"));
        System.out.println(jedis.set("key_str_2", "val_str_2"));

        // 使用 mset 添加多个 string-string, 注意参数成对出现
        System.out.println(jedis.mset("key_str_3", "val_str_3", "key_str_4", "val_str_4", "key_str_5", "val_str_5"));
        System.out.println(jedis.mset("key_str_6", "val_str_6"));
    }

    @Test
    public void testExpire() throws Exception {
        System.out.println(jedis.set("key_str_ex", "val_str_ex"));

        // 使用 expire 为key 设置超时时间, 超过后自动删除
        System.out.println(jedis.expire("key_str_ex", 5));
        System.out.println("先获取一次: " + jedis.get("key_str_ex"));
        Thread.sleep(6000);
        System.out.println("6秒后再次获取: " + jedis.get("key_str_ex"));

        // 可以在设置 key 的时候同时设置超时时间
        System.out.println(jedis.setex("key_str_ex2", 5, "val_str_ex2"));
        System.out.println("先获取一次: " + jedis.get("key_str_ex2"));
        Thread.sleep(6000);
        System.out.println("6秒后再次获取: " + jedis.get("key_str_ex2"));
    }

    @Test
    public void testIncr() {
        System.out.println(jedis.set("key_str_incr", "1"));
        System.out.println(jedis.get("key_str_incr"));

        // incr 会对数值 +1, 并返回结果
        System.out.println(jedis.incr("key_str_incr"));

        // incrBy 可以指定增量, 并返回结果
        System.out.println(jedis.incrBy("key_str_incr", 5));
    }

    @Test
    public void testList() {
        /*  redis 的 list 是个双端链表, 使用 push 进队, pop 离队, 并结合 l/r 对 队首/队尾 操作
         *  这里的 l 是 left, 与 right 对应, 表示队首和队尾, 并不是 list 的意思
         *
         *  list 的其他方法 l+命令, 这里的 l 表示 list
         *       index:  获取某个下标的元素
         *       range:  获取指定下标范围的元素, 闭区间 [a, b]
         *       trim:   根据下标范围裁剪 list, 只保留闭区间 [a, b]
         */

        // 左进左出
        System.out.println("lpush & lpop");
        System.out.println(jedis.lpush("key_list", "val_lpush_1", "val_lpush_2", "val_lpush_3"));
        System.out.println(jedis.lrange("key_list", 0, -1));
        System.out.println(jedis.lpop("key_list"));
        System.out.println(jedis.lrange("key_list", 0, -1));

        // 右进右出
        System.out.println("rpush & rpop");
        System.out.println(jedis.rpush("key_list", "val_rpush_1", "val_rpush_2", "val_rpush_3"));
        System.out.println(jedis.lrange("key_list", 0, -1));
        System.out.println(jedis.rpop("key_list"));
        System.out.println(jedis.lrange("key_list", 0, -1));

        // lindex 根据下标获取元素, 从 0 开始
        System.out.println("lindex");
        System.out.println(jedis.lindex("key_list", 1));

        // ltrim 对list 进行截取, [a, b], 保留闭区间内的元素, 负数表示倒数第几个元素
        System.out.println(jedis.ltrim("key_list", 1, 2));
        System.out.println(jedis.lrange("key_list", 0, -1));
    }

    @Test
    public void testHash() {
        // hset : hash + set
        System.out.println(jedis.hset("key_hash", "field_1", "1"));
        System.out.println(jedis.hset("key_hash", "field_2", "2"));
        System.out.println(jedis.hset("key_hash", "field_3", "3"));

        // hget : hash + get, 获取 hash 中某个 field 的 value
        System.out.println(jedis.hget("key_hash", "field_1"));

        // hmset : hash + multi + set, 同时设置多个 field, jedis 中直接使用 Map 作为参数, 以保证 key-value 成对出现
        Map<String, String> map = new HashMap<>();
        map.put("hmset_field1", "hmset_val1");
        map.put("hmset_field2", "hmset_val2");
        System.out.println(jedis.hmset("key_hmset", map));

        // hgetall : hash + getAll, 返回一个 Map
        System.out.println(jedis.hgetAll("key_hash"));

        // hlen : hash + length
        System.out.println(jedis.hlen("key_hash"));
        System.out.println(jedis.hlen("key_hmset"));

        // 与 string 类似, 可以使用 incrBy 或 incrByFloat 对 value 进行增减, 没有 incr
        System.out.println(jedis.hincrBy("key_hash", "field_1", 3));
        System.out.println(jedis.hincrByFloat("key_hash", "field_2", 3.5));
        System.out.println(jedis.hgetAll("key_hash"));
    }


    @Test
    public void testSet() {
        // sadd : set + add , 往集合中添加元素, jedis 中使用变长参数列表, 允许一次添加多个
        System.out.println(jedis.sadd("key_set", "val1", "val2", "val3"));

        // smembers : set + members, 获取集合中所有元素
        System.out.println(jedis.smembers("key_set"));

        // sismember : set + isMember, 检查某个元素是否在集合内
        System.out.println(jedis.sismember("key_set", "val1"));
        System.out.println(jedis.sismember("key_set", "val4"));

        // scard: set + cardinality, 获取 set 内的元素个数
        System.out.println(jedis.scard("key_set"));

        // spop: set + pop, 随机弹出 set 中的一个元素
        System.out.println(jedis.smembers("key_set"));
        System.out.println(jedis.spop("key_set"));
        System.out.println(jedis.smembers("key_set"));

        // srandmember : set random member, 随机获取一个元素, 但不弹出
        System.out.println(jedis.srandmember("key_set"));
        System.out.println(jedis.smembers("key_set"));
    }

    @Test
    public void testZset() {
        // zadd : zset + add, 在 zset 中添加元素, 需要指定分值
        System.out.println(jedis.zadd("key_zset", 1, "val1"));
        System.out.println(jedis.zadd("key_zset", 2, "val2"));
        System.out.println(jedis.zadd("key_zset", 3, "val3"));
        System.out.println(jedis.zadd("key_zset", 2.1, "val2.1"));
        System.out.println(jedis.zadd("key_zset", 1.8, "val1.8"));

        // zrange : zset + range, 根据分值升序排列, 获取序号在闭区间 [a, b] 内的元素, 且 a 必须在 b 前面, 否则为空
        System.out.println(jedis.zrange("key_zset", 0, -1));
        System.out.println(jedis.zrange("key_zset", -1, 0));

        // zrevrange : zset reverse range, 根据分值降序排列, 获取 [a, b] 内的元素
        System.out.println(jedis.zrevrange("key_zset", 0, -1));
    }

    @Test
    public void testKeys() {
        // 通过 keys 获取当前所有 key, * 表示全部
        System.out.println(jedis.keys("*"));

        // 可以进行筛选, 筛选条件不是正则表达式
        System.out.println(jedis.keys("str[1-9]"));

        // del 命令可以删除某个 key
        System.out.println(jedis.del("str3"));
        System.out.println(jedis.keys("str[1-9]"));
        // System.out.println(jedis.dump("str1"));

        // exist 检查某个 key 是否存在
        System.out.println(jedis.exists("str1"));
        System.out.println(jedis.del("str1"));
        System.out.println(jedis.exists("str1"));
    }


}






















