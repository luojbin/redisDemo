package redisInAction.d1_SortArticles;

import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author luojbin
 * @version 1.0
 * @create 2018/8/8 14:26
 */
public class SortArticle {

    protected Jedis jedis;

    @Before
    public void init() {
        // 创建连接
        jedis = new Jedis("47.106.82.153", 6379);
        // 授权
        jedis.auth("luojbin2Redis");
    }

    /**
     * 公用有序表, 记录每篇文章的发布时间
     */
    private final String PUBLISH_TIME_ZSET = "time:";

    /**
     * 公用有序表, 记录文章当前分数
     */
    private final String ARTICLE_SCORE_ZSET = "score:";

    /**
     * 每篇文章有一个 hash, 记录文章内容, hash 名为 "article:文章ID", 内容为"title, body"
     * 每篇文章有一个 set, 记录为这篇文章投过票的用户, set 名为 "voted:文章ID", 记录为"user:用户ID"
     */
    private final String ARTICLE = "article:";
    private final String VOTED_USER = "voted:";
    private final Long SCORE_PER_VOTE = 432L;

    @Test
    public void testInit() {
        // jedis.flushDB();
        // 初始化数据
        jedis.set("articleId", "10000");
        System.out.println(jedis.keys("*"));
    }

    /**
     * 发布新文章
     */
    @Test
    public void testPublish() {

        String articleId = "" + jedis.incr("articleId");
        String title = "color:yellow";
        String author = "luojbin33";
        String body = "这是一篇文章, 关于: " + title;

        Map<String, String> article = new HashMap<>();
        article.put("title", title);
        article.put("author", author);
        article.put("body", body);
        article.put("votes", "0");

        // 创建文章的 内容hash
        jedis.hmset(ARTICLE + articleId, article);

        // 创建文章的 投票用户set
        jedis.sadd(VOTED_USER + articleId, "");

        long now = System.currentTimeMillis() / 1000;
        // 把文章添加到 发布时间 zset
        jedis.zadd(PUBLISH_TIME_ZSET, now, ARTICLE + articleId);

        // 把文章添加到 文章分数 zset
        jedis.zadd(ARTICLE_SCORE_ZSET, now, ARTICLE + articleId);

        System.out.println("发布成功!");
    }

    @Test
    public void testGetArticlesByCTimeDesc() {
        // 在指定的 zset 中, 获取排名在 [start, end] 范围内的成员 key (文章id)
        Set<Tuple> ids = jedis.zrevrangeWithScores(PUBLISH_TIME_ZSET, 0, -1);
        // 根据文章ids, 遍历查出文章 hash
        printArticles(ids);
    }

    @Test
    public void testGetArticlesByScoreDesc() {
        // 在指定的 zset 中, 获取排名在 [start, end] 范围内的成员 key (文章id)
        Set<Tuple> ids = jedis.zrevrangeWithScores(ARTICLE_SCORE_ZSET, 0, -1);
        // 根据文章ids, 遍历查出文章 hash
        printArticles(ids);
    }

    public void printArticles(Set<Tuple> ids){
        for (Tuple id : ids) {
            Map<String, String> articleData = jedis.hgetAll(id.getElement());
            System.out.println("这是一篇文章: " + id.getElement() + ", 分数为:" + (long)id.getScore());
            for (Map.Entry<String, String> entry : articleData.entrySet()) {
                System.out.println("\t" + entry.getKey() + ": " + entry.getValue());
            }
        }
    }
    /**
     * 为文章投票-赞
     */
    @Test
    public void testVoteUp() {
        String articleId = "10010";
        String userId = "luojbin7";
        // 需要在文章投票用户表中记录
        Long voted = jedis.sadd(VOTED_USER+articleId, userId);
        if(!new Long(1).equals(voted)){
            System.out.println("该用户已为当前文章投票, 不可重复投票!");
            return;
        }
        // 更新投票人数
        jedis.hincrBy(ARTICLE+articleId, "votes", 1);
        // 为文章加分
        long score = (long)(double)jedis.zincrby(ARTICLE_SCORE_ZSET, SCORE_PER_VOTE, ARTICLE+articleId );
        System.out.println("投票后, " + ARTICLE + articleId + "的分数为: " + score);
    }

    /**
     * 为文章投票-踩
     */
    @Test
    public void testVoteDown() {

    }

    /**
     * 获取文章列表
     */
    @Test
    public void testGetScores() {
        Set<Tuple> scoreSet = jedis.zrevrangeWithScores(ARTICLE_SCORE_ZSET, 0, -1);
        for(Tuple tuple : scoreSet){
            System.out.println(tuple.getElement() + " 的分数为: " + (long)tuple.getScore());
        }
    }

    /**
     * 将文章添加到分组
     */
    @Test
    public void testGroup() {
        // // animals 分组
        // jedis.sadd("group:animals", ARTICLE+"10004");
        // jedis.sadd("group:animals", ARTICLE+"10005");
        // jedis.sadd("group:animals", ARTICLE+"10006");
        // jedis.sadd("group:animals", ARTICLE+"10007");
        //
        // // fruit 分组
        // jedis.sadd("group:fruit", ARTICLE+"10008");
        // jedis.sadd("group:fruit", ARTICLE+"10009");
        // jedis.sadd("group:fruit", ARTICLE+"10010");
        // jedis.sadd("group:fruit", ARTICLE+"10011");
        //
        // // color 分组
        // jedis.sadd("group:color", ARTICLE+"10012");
        // jedis.sadd("group:color", ARTICLE+"10013");
        // jedis.sadd("group:color", ARTICLE+"10014");
        // jedis.sadd("group:color", ARTICLE+"10015");

        // fruit 分组


    }

    /**
     * 将文章移出分组
     */
    @Test
    public void testRemoveFromGroup() {

    }
    /**
     * 按分组获取文章
     */
    @Test
    public void testArticlesByGroup() {
        jedis.zinterstore("zgroup:fruit",ARTICLE_SCORE_ZSET, "group:fruit");
        Set<Tuple> set = jedis.zrevrangeWithScores("zgroup:fruit", 0, -1);
        printArticles(set);
    }

    /**
     * 命名空间测试, redis 惯例使用冒号分割命名空间,
     * 如 redis desktop manager 等工具, 在冒号分隔是会折叠相同明名空间的内容
     */
    @Test
    public void testNameSpace() {
        jedis.set("key-1", "1");
        jedis.set("key-2", "1");
        jedis.set("key-3", "1");
        jedis.set("key-4", "1");
        jedis.set("key-5", "1");

        jedis.set("key_1", "1");
        jedis.set("key_2", "1");
        jedis.set("key_3", "1");
        jedis.set("key_4", "1");
        jedis.set("key_5", "1");

        jedis.set("key,1", "1");
        jedis.set("key,2", "1");
        jedis.set("key,3", "1");
        jedis.set("key,4", "1");
        jedis.set("key,5", "1");

        jedis.set("key:1", "1");
        jedis.set("key:2", "1");
        jedis.set("key:3", "1");
        jedis.set("key:4", "1");
        jedis.set("key:5", "1");


    }
}
