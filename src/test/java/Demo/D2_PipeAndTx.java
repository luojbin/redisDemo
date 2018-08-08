package Demo;

import org.junit.Test;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Transaction;

/**
 * @author luojbin
 * @version 1.0
 * @create 2018/7/31 14:53
 */
public class D2_PipeAndTx extends BasicTest {

    @Test
    public void testNormal() {
        jedis.flushDB();
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            jedis.set("p" + i, i + "");
        }
        long t2 = System.currentTimeMillis();
        System.out.print("同步操作: ");
        System.out.println(t2 - t1);
    }
    /**
     * redis 提供了管道技术, 允许在一次网络开销中执行多个操作指令
     * 使用 pipelined 开启管道
     * 使用 sync 方法执行管道中所有命令
     */
    @Test
    public void testPipeline() {
        jedis.flushDB();
        Pipeline p = jedis.pipelined();
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            p.set("p" + i, i + "");
        }
        p.sync();
        long t2 = System.currentTimeMillis();
        System.out.print("管道操作: ");
        System.out.println(t2 - t1);
    }

    /**
     * redis 的事务只保证了操作的原子性, 但是并不保证同时成功或同时失败
     * 使用 multi 开始事务块
     * 使用 exec 执行事务块
     */
    @Test
    public void testTx() {
        jedis.flushDB();
        Transaction mutil = jedis.multi();
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            mutil.set("p" + i, i + "");
        }
        mutil.exec();
        long t2 = System.currentTimeMillis();
        System.out.print("事务操作: ");
        System.out.println(t2 - t1);
    }

    @Test
    public void testPipeAndTx() {
        jedis.flushDB();

    }

    @Test
    public void test() {

    }
    @Test
    public void testPipeVsTx() {
        testNormal();

        // 使用管道命令执行1000次
        testPipeline();

        // 使用事务执行1000次
        testTx();

        // 使用管道+事务执行1000次
    }


}






















