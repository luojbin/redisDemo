package Demo;

import org.junit.Test;

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
        // redis 的 list 是个双端链表, 使用 push 进队, pop 离队, 并结合 l/r 对 队首/队尾 操作

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
        System.out.println(jedis.ltrim("key_list", 1,2));
        System.out.println(jedis.lrange("key_list", 0, -1));



    }

    @Test
    public void testHash() {

    }


    @Test
    public void testSet() {

    }

    @Test
    public void testZset() {

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
        System.out.println(jedis.dump("str1"));

        // exist 检查某个 key 是否存在
        System.out.println(jedis.exists("str1"));
        System.out.println(jedis.del("str1"));
        System.out.println(jedis.exists("str1"));
    }


}






















