package com.loyofo.redis.redisInAction;

import com.google.gson.Gson;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class Chapter02 {
    public static final void main(String[] args) throws InterruptedException {
        new Chapter02().run();
    }

    public void run() throws InterruptedException {
        Jedis conn = new Jedis("47.106.82.153", 6379);
        conn.auth("luojbin2Redis");
        conn.select(2);

        testLoginCookies(conn);
        // testShopppingCartCookies(conn);
        // testCacheRows(conn);
        // testCacheRequest(conn);
    }

    public void testLoginCookies(Jedis conn) throws InterruptedException {
        System.out.println("\n----- testLoginCookies -----");
        String token = UUID.randomUUID().toString();

        updateToken(conn, token, "username", "itemX");
        System.out.println("我们刚刚登录/更新了token: " + token);
        System.out.println("当前用户为: 'username'");
        System.out.println();

        System.out.print("通过 token 找到用户: ");
        String r = checkToken(conn, token);
        System.out.println(r);
        System.out.println();
        assert r != null;

        System.out.println("准备删除所有用户cookie");
        System.out.println("开启一个线程, 晚点再关掉");
        System.out.println("让我们清除掉所有的登录信息");
        System.out.println("先启动一个清理线程, 待会再把这个进程结束掉");

        CleanSessionsThread thread = new CleanSessionsThread(0);
        System.out.println("启动线程");
        thread.start();
        System.out.println("暂停1秒");

        System.out.println("主线程暂停1秒");
        Thread.sleep(1000);

        System.out.println("退出清理线程");
        thread.quit();
        System.out.println("暂停2秒");
        System.out.println("主线程暂停1秒");
        Thread.sleep(2000);

        if (thread.isAlive()) {
            throw new RuntimeException("清理线程还没结束?");
        }

        long s = conn.hlen("login:");
        System.out.println("当前在线用户数量为: " + s);
        assert s == 0;
    }

    public void testShopppingCartCookies(Jedis conn) throws InterruptedException {
        System.out.println("\n----- testShopppingCartCookies -----");
        String token = UUID.randomUUID().toString();

        System.out.println("We'll refresh our session...");
        updateToken(conn, token, "username", "itemX");
        System.out.println("And add an item to the shopping cart");
        addToCart(conn, token, "itemY", 3);
        Map<String, String> r = conn.hgetAll("cart:" + token);
        System.out.println("Our shopping cart currently has:");
        for (Map.Entry<String, String> entry : r.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
        System.out.println();

        assert r.size() >= 1;

        System.out.println("Let's clean out our sessions and carts");
        CleanFullSessionsThread thread = new CleanFullSessionsThread(0);
        thread.start();
        Thread.sleep(1000);
        thread.quit();
        Thread.sleep(2000);
        if (thread.isAlive()) {
            throw new RuntimeException("The clean sessions thread is still alive?!?");
        }

        r = conn.hgetAll("cart:" + token);
        System.out.println("Our shopping cart now contains:");
        for (Map.Entry<String, String> entry : r.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
        assert r.size() == 0;
    }

    public void testCacheRows(Jedis conn) throws InterruptedException {
        System.out.println("\n----- testCacheRows -----");
        System.out.println("First, let's schedule caching of itemX every 5 seconds");
        scheduleRowCache(conn, "itemX", 5);
        System.out.println("Our schedule looks like:");
        Set<Tuple> s = conn.zrangeWithScores("schedule:", 0, -1);
        for (Tuple tuple : s) {
            System.out.println("  " + tuple.getElement() + ", " + tuple.getScore());
        }
        assert s.size() != 0;

        System.out.println("We'll start a caching thread that will cache the data...");

        CacheRowsThread thread = new CacheRowsThread();
        thread.start();

        Thread.sleep(1000);
        System.out.println("Our cached data looks like:");
        String r = conn.get("inv:itemX");
        System.out.println(r);
        assert r != null;
        System.out.println();

        System.out.println("We'll check again in 5 seconds...");
        Thread.sleep(5000);
        System.out.println("Notice that the data has changed...");
        String r2 = conn.get("inv:itemX");
        System.out.println(r2);
        System.out.println();
        assert r2 != null;
        assert !r.equals(r2);

        System.out.println("Let's force un-caching");
        scheduleRowCache(conn, "itemX", -1);
        Thread.sleep(1000);
        r = conn.get("inv:itemX");
        System.out.println("The cache was cleared? " + (r == null));
        assert r == null;

        thread.quit();
        Thread.sleep(2000);
        if (thread.isAlive()) {
            throw new RuntimeException("The database caching thread is still alive?!?");
        }
    }

    public void testCacheRequest(Jedis conn) {
        System.out.println("\n----- testCacheRequest -----");
        String token = UUID.randomUUID().toString();

        Callback callback = new Callback() {
            public String call(String request) {
                return "content for " + request;
            }
        };

        updateToken(conn, token, "username", "itemX");
        String url = "http://test.com/?item=itemX";
        System.out.println("We are going to cache a simple request against " + url);
        String result = cacheRequest(conn, url, callback);
        System.out.println("We got initial content:\n" + result);
        System.out.println();

        assert result != null;

        System.out.println("To test that we've cached the request, we'll pass a bad callback");
        String result2 = cacheRequest(conn, url, null);
        System.out.println("We ended up getting the same response!\n" + result2);

        assert result.equals(result2);

        assert !canCache(conn, "http://test.com/");
        assert !canCache(conn, "http://test.com/?item=itemX&_=1234536");
    }

    public String checkToken(Jedis conn, String token) {
        return conn.hget("login:", token);
    }

    public void updateToken(Jedis conn, String token, String user, String item) {
        long timestamp = System.currentTimeMillis() / 1000;
        conn.hset("login:", token, user);
        conn.zadd("recent:", timestamp, token);
        if (item != null) {
            conn.zadd("viewed:" + token, timestamp, item);
            conn.zremrangeByRank("viewed:" + token, 0, -26);
            conn.zincrby("viewed:", -1, item);
        }
    }

    public void addToCart(Jedis conn, String session, String item, int count) {
        if (count <= 0) {
            conn.hdel("cart:" + session, item);
        } else {
            conn.hset("cart:" + session, item, String.valueOf(count));
        }
    }

    public void scheduleRowCache(Jedis conn, String rowId, int delay) {
        conn.zadd("delay:", delay, rowId);
        conn.zadd("schedule:", System.currentTimeMillis() / 1000, rowId);
    }

    public String cacheRequest(Jedis conn, String request, Callback callback) {
        if (!canCache(conn, request)) {
            return callback != null ? callback.call(request) : null;
        }

        String pageKey = "cache:" + hashRequest(request);
        String content = conn.get(pageKey);

        if (content == null && callback != null) {
            content = callback.call(request);
            conn.setex(pageKey, 300, content);
        }

        return content;
    }

    public boolean canCache(Jedis conn, String request) {
        try {
            URL url = new URL(request);
            HashMap<String, String> params = new HashMap<String, String>();
            if (url.getQuery() != null) {
                for (String param : url.getQuery().split("&")) {
                    String[] pair = param.split("=", 2);
                    params.put(pair[0], pair.length == 2 ? pair[1] : null);
                }
            }

            String itemId = extractItemId(params);
            if (itemId == null || isDynamic(params)) {
                return false;
            }
            Long rank = conn.zrank("viewed:", itemId);
            return rank != null && rank < 10000;
        } catch (MalformedURLException mue) {
            return false;
        }
    }

    public boolean isDynamic(Map<String, String> params) {
        return params.containsKey("_");
    }

    public String extractItemId(Map<String, String> params) {
        return params.get("item");
    }

    public String hashRequest(String request) {
        return String.valueOf(request.hashCode());
    }

    public interface Callback {
        public String call(String request);
    }

    public class CleanSessionsThread extends Thread {
        private Jedis conn;
        private int limit;
        private boolean quit;

        public CleanSessionsThread(int limit) {
            this.conn = new Jedis("47.106.82.153", 6379);
            this.conn.auth("luojbin2Redis");
            this.conn.select(2);
            this.limit = limit;
        }

        public void quit() {
            quit = true;
        }

        public void run() {
            while (!quit) {
                long size = conn.zcard("recent:");
                if (size <= limit) {
                    try {
                        sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }

                long endIndex = Math.min(size - limit, 100);
                Set<String> tokenSet = conn.zrange("recent:", 0, endIndex - 1);
                String[] tokens = tokenSet.toArray(new String[tokenSet.size()]);

                ArrayList<String> sessionKeys = new ArrayList<String>();
                for (String token : tokens) {
                    sessionKeys.add("viewed:" + token);
                }

                conn.del(sessionKeys.toArray(new String[sessionKeys.size()]));
                conn.hdel("login:", tokens);
                conn.zrem("recent:", tokens);
            }
        }
    }

    public class CleanFullSessionsThread extends Thread {
        private Jedis conn;
        private int limit;
        private boolean quit;

        public CleanFullSessionsThread(int limit) {
            this.conn = new Jedis("localhost");
            this.conn.select(15);
            this.limit = limit;
        }

        public void quit() {
            quit = true;
        }

        public void run() {
            while (!quit) {
                long size = conn.zcard("recent:");
                if (size <= limit) {
                    try {
                        sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }

                long endIndex = Math.min(size - limit, 100);
                Set<String> sessionSet = conn.zrange("recent:", 0, endIndex - 1);
                String[] sessions = sessionSet.toArray(new String[sessionSet.size()]);

                ArrayList<String> sessionKeys = new ArrayList<String>();
                for (String sess : sessions) {
                    sessionKeys.add("viewed:" + sess);
                    sessionKeys.add("cart:" + sess);
                }

                conn.del(sessionKeys.toArray(new String[sessionKeys.size()]));
                conn.hdel("login:", sessions);
                conn.zrem("recent:", sessions);
            }
        }
    }

    public class CacheRowsThread extends Thread {
        private Jedis conn;
        private boolean quit;

        public CacheRowsThread() {
            this.conn = new Jedis("localhost");
            this.conn.select(15);
        }

        public void quit() {
            quit = true;
        }

        public void run() {
            Gson gson = new Gson();
            while (!quit) {
                Set<Tuple> range = conn.zrangeWithScores("schedule:", 0, 0);
                Tuple next = range.size() > 0 ? range.iterator().next() : null;
                long now = System.currentTimeMillis() / 1000;
                if (next == null || next.getScore() > now) {
                    try {
                        sleep(50);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }

                String rowId = next.getElement();
                double delay = conn.zscore("delay:", rowId);
                if (delay <= 0) {
                    conn.zrem("delay:", rowId);
                    conn.zrem("schedule:", rowId);
                    conn.del("inv:" + rowId);
                    continue;
                }

                Inventory row = Inventory.get(rowId);
                conn.zadd("schedule:", now + delay, rowId);
                conn.set("inv:" + rowId, gson.toJson(row));
            }
        }
    }

    public static class Inventory {
        private String id;
        private String data;
        private long time;

        private Inventory(String id) {
            this.id = id;
            this.data = "data to cache...";
            this.time = System.currentTimeMillis() / 1000;
        }

        public static Inventory get(String id) {
            return new Inventory(id);
        }
    }
}
