package Demo;

import org.junit.Test;
import redis.clients.jedis.Pipeline;

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
        System.out.println(jedis.set("key1", "value1"));
        System.out.println(jedis.set("key2", "value2"));
    }

    @Test
    public void testHash() {

    }

    @Test
    public void testList() {

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






















