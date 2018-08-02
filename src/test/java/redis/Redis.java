package redis;


import java.util.HashMap;

/**
 * @author luojbin
 * @version 1.0
 * @create 2018/8/1 11:03
 */
public class Redis {

    HashMap<String, Object>[] databases;

    public Redis(int n){
        databases = new HashMap[n];
    }
}
