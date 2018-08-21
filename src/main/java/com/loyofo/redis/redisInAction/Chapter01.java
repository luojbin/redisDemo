package com.loyofo.redis.redisInAction;

/**
 * @author luojbin
 * @version 1.0
 * @create 2018/8/8 11:19
 */
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ZParams;

import java.util.*;

public class Chapter01 {
    /**
     * 每周的秒数
     */
    private static final int ONE_WEEK_IN_SECONDS = 7 * 86400;
    /**
     * 每张投票的分值
     */
    private static final int VOTE_SCORE = 432;
    /**
     * 每页显示文章数
     */
    private static final int ARTICLES_PER_PAGE = 25;

    public static final void main(String[] args) {
        new Chapter01().run();
    }

    public void run() {
        Jedis conn = new Jedis("47.106.82.153",6379);
        conn.auth("luojbin2Redis");
        conn.select(15);

        String articleId = postArticle(
                conn, "username", "A title", "http://www.google.com");
        System.out.println("发布了一篇文章, id: " + articleId);
        System.out.println("文章 hash 如下:");

        // hgetAll 获取指定 hash 的所有内容
        Map<String,String> articleData = conn.hgetAll("article:" + articleId);
        // 遍历 hash, 输出文章的具体内容
        for (Map.Entry<String,String> entry : articleData.entrySet()){
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }

        System.out.println();

        // 给文章投票
        articleVote(conn, "other_user", "article:" + articleId);
        // 获取指定文章的当前票数
        String votes = conn.hget("article:" + articleId, "votes");
        System.out.println("给文章投票, 当前票数为: " + votes);
        assert Integer.parseInt(votes) > 1;

        // 获取高分文章列表
        System.out.println("现在最高分的文章列表是:");
        List<Map<String,String>> articles = getArticles(conn, 1);
        printArticles(articles);
        assert articles.size() >= 1;

        // 给文章添加分组标签
        addGroups(conn, articleId, new String[]{"new-group"});
        System.out.println("把文章添加到new-group 分组中, 组内其他文章有:");
        // ghgh
        articles = getGroupArticles(conn, "new-group", 1);
        printArticles(articles);
    }

    /**
     * 发表文章
     * @param conn
     * @param user
     * @param title
     * @param link
     * @return
     */
    public String postArticle(Jedis conn, String user, String title, String link) {
        // 获取文章id, 用 "article:" 为 key, 记录当前文章id
        // incr 会将对应 key 的值加一并返回, 如果不存在则初始化为0 再加一
        String articleId = String.valueOf(conn.incr("article:"));

        // 初始化 文章已投票用户, 将发布者记为第一个投票用户
        String voted = "voted:" + articleId;
        conn.sadd(voted, user);
        // 设置有效期一周, 一周后删除投票统计 set, 该文章不能再投票
        conn.expire(voted, ONE_WEEK_IN_SECONDS);

        // 发布文章, 创建文章的 hash
        long now = System.currentTimeMillis() / 1000;
        String article = "article:" + articleId;
        HashMap<String,String> articleData = new HashMap<String,String>();
        articleData.put("title", title);
        articleData.put("link", link);
        articleData.put("user", user);
        articleData.put("now", String.valueOf(now));
        articleData.put("votes", "1");
        conn.hmset(article, articleData);

        // 将文章添加到分数表
        conn.zadd("score:", now + VOTE_SCORE, article);
        // 将文章添加到时间表
        conn.zadd("time:", now, article);

        return articleId;
    }

    /**
     * 给文章投票
     * @param conn
     * @param user
     * @param article
     */
    public void articleVote(Jedis conn, String user, String article) {
        // 文章是否已过期, 不让投票
        long cutoff = (System.currentTimeMillis() / 1000) - ONE_WEEK_IN_SECONDS;
        if (conn.zscore("time:", article) < cutoff){
            return;
        }

        // 投票操作
        String articleId = article.substring(article.indexOf(':') + 1);
        // 记录给文章投票的用户
        if (conn.sadd("voted:" + articleId, user) == 1) {
            // 更新分数表里的文章分数
            conn.zincrby("score:", VOTE_SCORE, article);
            // 更新文章 hash 的投票人数
            conn.hincrBy(article, "votes", 1);
        }
    }


    /**
     * 获取文章列表
     * @param conn
     * @param page
     * @return
     */
    public List<Map<String,String>> getArticles(Jedis conn, int page) {
        return getArticles(conn, page, "score:");
    }

    /**
     * 获取文章列表
     * @param conn
     * @param page
     * @param order
     * @return
     */
    public List<Map<String,String>> getArticles(Jedis conn, int page, String order) {
        // 计算要查询的文章 起始序号
        int start = (page - 1) * ARTICLES_PER_PAGE;
        // 计算要查询的文章 结束序号
        int end = start + ARTICLES_PER_PAGE - 1;

        // 在指定的 zset 中, 获取排名在 [start, end] 范围内的成员 key (文章id)
        Set<String> ids = conn.zrevrange(order, start, end);
        // 根据文章ids, 遍历查出文章 hash
        List<Map<String,String>> articles = new ArrayList<Map<String,String>>();
        for (String id : ids){
            Map<String,String> articleData = conn.hgetAll(id);
            articleData.put("id", id);
            articles.add(articleData);
        }

        return articles;
    }

    /**
     * 增加分组
     * @param conn
     * @param articleId
     * @param toAdd
     */
    public void addGroups(Jedis conn, String articleId, String[] toAdd) {
        // 每个分组有一个 set, 将文章id 加入到该组的 set 即可
        String article = "article:" + articleId;
        for (String group : toAdd) {
            conn.sadd("group:" + group, article);
        }
    }

    /**
     * 获取组内文章
     * @param conn
     * @param group
     * @param page
     * @return
     */
    public List<Map<String,String>> getGroupArticles(Jedis conn, String group, int page) {
        return getGroupArticles(conn, group, page, "score:");
    }

    /**
     * 获取组内文章
     * @param conn
     * @param group
     * @param page
     * @param order
     * @return
     */
    public List<Map<String,String>> getGroupArticles(Jedis conn, String group, int page, String order) {
        String key = order + group;
        if (!conn.exists(key)) {
            ZParams params = new ZParams().aggregate(ZParams.Aggregate.MAX);
            conn.zinterstore(key, params, "group:" + group, order);
            conn.expire(key, 60);
        }
        return getArticles(conn, page, key);
    }

    /**
     * 打印文章
     * @param articles
     */
    private void printArticles(List<Map<String,String>> articles){
        for (Map<String,String> article : articles){
            System.out.println("  id: " + article.get("id"));
            for (Map.Entry<String,String> entry : article.entrySet()){
                if (entry.getKey().equals("id")){
                    continue;
                }
                System.out.println("    " + entry.getKey() + ": " + entry.getValue());
            }
        }
    }
}