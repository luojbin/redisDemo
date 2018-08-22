package redisInAction.d2_cookie;

import org.junit.Before;
import org.junit.Test;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.util.Random;
import java.util.UUID;

/**
 * @author luojbin
 * @version 1.0
 * @create 2018-08-21
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
    
    /**
     * 用户登陆, 需要存token, 并设置超时时间, 只能单独设置一个 String 类型, key形如 "token:xxxxx", value 为 "user:0001"
     * 每个用户有一个购物车, 需要存储商品id, 商品数量, 需要用一个 zset, 以数量为 score, 形如"cart:0001"
     * 每个用户有一个浏览记录, 需要存储商品id, 访问时间, 可以用一个 zset, 以访问时间为 score, 形如 "history:0001"
     *
     * 所有商品要统计浏览次数, 需要存储商品id, 访问次数, 可以用一个 zset, 以点击次数为 score, 形如 "count:readItem"
     * 所有商品要统计加购次数, 需要存储商品id, 加购次数, 可以用一个 zset, 以加购次数为 score, 形如 "count:cartItem"
     *
     *
     */

    /**
     * 用户登录
     */
    @Test
    public void testLogin() {
        String userId = "0001";
        String token = UUID.randomUUID().toString().replaceAll("-","");
        String userKey = "user:" + userId;

        // 创建token
        jedis.setex(token, 60, userKey);
        // 创建购物车
        // 创建浏览历史
        System.out.println(token);
    }

    public String checkToken(String token) {
        String user = jedis.get(token);
        System.out.println(token + "," + user);
        if(StringUtils.isEmpty(user)){
            System.out.println("用户未登录, 请登录");
            return "";
        } else {
            String userId = user.substring(user.indexOf(':') + 1);
            System.out.println("验证成功, 用户已登录:" + userId);
            return userId;
        }
    }

    /**
     * 把商品添加到购物车
     */
    @Test
    public void addIntoCart(){
        String token = "22b48f1e789b45d0bd76414bf90be941";
        String userId = checkToken(token);
        Random random = new Random();

        String itemId = "item:" + String.format("%03d", random.nextInt(1000));
        int itemCount = random.nextInt(10);
        if (!StringUtils.isEmpty(userId)){
            // 加入用户的购物车
            jedis.sadd("cart:"+userId, itemId+"_"+itemCount);
            // 统计加购的商品
            jedis.zincrby("count:cartItem", 1, itemId);
        }

        System.out.println("已将商品 " + itemId + "加入购物车, 数量为: " + itemCount);

    }

    /**
     * 查看当前购物车的内容
     */
    @Test
    public void getCart(){}

    /**
     * 浏览货物, 将货物id加入当前用户的浏览记录
     */
    @Test
    public void readItem(){
        String token = "22b48f1e789b45d0bd76414bf90be941";
        String userId = checkToken(token);
        Random random = new Random();

        String itemId = "item:" + String.format("%03d", random.nextInt(1000));
        int itemCount = random.nextInt(10);
        if (!StringUtils.isEmpty(userId)){
            // 加入用户的购物车
            // jedis.zadd("cart:"+userId, itemId);
            // 统计加购的商品
            jedis.zincrby("count:readItem", 1, itemId);
        }

        System.out.println("添加了");
    }

    /**
     * 创建 999 个商品, 加入 readItem 和 cartItem
     */
    @Test
    public void testCreateItem() {
        Pipeline pl = jedis.pipelined();
        for (int i = 1; i <1000 ; i++){
            pl.zadd("count:readItem", 0, "item:"+String.format("%03d", i));
            pl.zadd("count:cartItem", 0, "item:"+String.format("%03d", i));
        }
        pl.sync();
    }
}