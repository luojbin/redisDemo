package com.loyofo.redis.service;

import com.loyofo.redis.dao.ArticleDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author luojbin
 * @version 1.0
 * @create 2018/8/8 10:28
 */
@Service
public class HelloService {

    @Autowired
    private ArticleDao articleDao;

    public Map<String, Object> getOneArticle(){
        Map<String, Object> map = articleDao.quereyArticle();
        System.out.println(map);
        return map;
    }

}
