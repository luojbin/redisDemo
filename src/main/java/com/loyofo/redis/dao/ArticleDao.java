package com.loyofo.redis.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * @author luojbin
 * @version 1.0
 * @create 2018/8/8 10:23
 */
@Repository
public class ArticleDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Map<String, Object> quereyArticle(){
        String sql = "select * from article limit 1";
        return jdbcTemplate.queryForMap(sql);
    }
}
