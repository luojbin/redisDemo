package redisInAction.d1_SortArticles;

import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

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
    private final String SCORE = "200";

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
        String author = "luojbin";
        String body = "这是一篇文章, 关于: " + articleId;

        Map<String, String> article = new HashMap<>();
        article.put("title", articleId);
        article.put("author", author);
        article.put("body", body);
        article.put("votes", "0");

        // 创建文章的 内容hash
        jedis.hmset(ARTICLE + articleId, article);

        // 创建文章的 投票用户set
        jedis.sadd(VOTED_USER + articleId, "");

        // 把文章添加到 发布时间 zset
        jedis.zadd(PUBLISH_TIME_ZSET, System.currentTimeMillis() / 1000, ARTICLE + articleId);

        // 把文章添加到 文章分数 zset
        jedis.zadd(ARTICLE_SCORE_ZSET, System.currentTimeMillis() / 1000, ARTICLE + articleId);
    }

    @Test
    public void testGetArticles() {
        // 在指定的 zset 中, 获取排名在 [start, end] 范围内的成员 key (文章id)
        Set<String> ids = jedis.zrevrange(PUBLISH_TIME_ZSET, 0, -1);
        // 根据文章ids, 遍历查出文章 hash
        for (String id : ids) {
            Map<String, String> articleData = jedis.hgetAll(id);
            articleData.put("id", id);
            System.out.println("这是一篇文章: " + id);
            for (Map.Entry<String, String> entry : articleData.entrySet()) {
                if (entry.getKey().equals("id")) {
                    continue;
                }
                System.out.println("    " + entry.getKey() + ": " + entry.getValue());
            }
        }
    }

    /**
     * 为文章投票-赞
     */
    @Test
    public void testVoteUp() {
        String articleId = "10003";

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
    public void testGetListByScore() {
        Set<String> articleSet = jedis.zrevrange(ARTICLE_SCORE_ZSET, 0, -1);
        for (String article : articleSet) {
            System.out.print(article + ": ");
            System.out.println(jedis.zscore(ARTICLE_SCORE_ZSET, article));
        }
    }

    /**
     * 分组查询
     */
    @Test
    public void testGroup() {

    }
}
