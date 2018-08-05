package com.luojbin.redis.dao;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.beans.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
public class JdbcDao<T, R> {

    public static final String INSERT = "insert";
    public static final String DELETE = "delete";
    public static final String UPDATE = "update";
    public static final String SELECT = "select";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) throws SQLException {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> queryForMap(String sql) throws SQLException {
        try {
            return jdbcTemplate.queryForMap(sql);
        } catch (Exception e) {
            if ((e instanceof IncorrectResultSizeDataAccessException) && (((IncorrectResultSizeDataAccessException) e).getActualSize() != 1)) {
                return null;
            }
        }
        return null;
    }

    public Map<String, Object> queryForMap(String sql, Object[] args) throws SQLException {
        try {
            return jdbcTemplate.queryForMap(sql, args);
        } catch (Exception e) {
            if ((e instanceof IncorrectResultSizeDataAccessException) && (((IncorrectResultSizeDataAccessException) e).getActualSize() != 1)) {
                return null;
            }
        }
        return null;
    }

    public List<Map<String, Object>> queryForList(String sql) throws SQLException {
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> queryForList(String sql, Object[] args) throws SQLException {
        return jdbcTemplate.queryForList(sql, args);
    }

    public int update(String sql) throws SQLException {
        return jdbcTemplate.update(sql);
    }

    public int update(String sql, Object[] args) throws SQLException {
        return jdbcTemplate.update(sql, args);
    }

    public int count(String sql, Object[] o) throws SQLException {
        String newSql = "SELECT COUNT(1) FROM ( " + sql + " ) t";
        try {
            if (o == null) {
                return ((Integer) jdbcTemplate.queryForObject(newSql, Integer.class)).intValue();
            }
            return ((Integer) jdbcTemplate.queryForObject(newSql, o, Integer.class)).intValue();
        } catch (EmptyResultDataAccessException e) {
        }
        return 0;
    }

    public int count(String sql) throws SQLException {
        return count(sql, null);
    }

    // 查单个对象
    public T queryForObject(String sql, RowMapper<T> rowMapper) throws SQLException {

        try {
            return jdbcTemplate.queryForObject(sql, rowMapper);
        } catch (Exception e) {
            if ((e instanceof IncorrectResultSizeDataAccessException) && (((IncorrectResultSizeDataAccessException) e).getActualSize() != 1)) {
                return null;
            }
        }
        return null;
    }

    // 查单个对象
    public T queryForObject(String sql, Object[] args, RowMapper<T> rowMapper) throws SQLException {
        try {
            return jdbcTemplate.queryForObject(sql, rowMapper, args);
        } catch (Exception e) {
            if ((e instanceof IncorrectResultSizeDataAccessException) && (((IncorrectResultSizeDataAccessException) e).getActualSize() != 1)) {
                return null;
            }
        }
        return null;
    }

    // 查对象列表
    public List<T> query(String sql, RowMapper<T> rowMapper) throws SQLException {
        return jdbcTemplate.query(sql, rowMapper);
    }

    // 查对象列表
    public List<T> query(String sql, Object[] args, RowMapper<T> rowMapper) throws SQLException {
        return jdbcTemplate.query(sql, rowMapper, args);
    }

    // 查单个字段
    public R querySingleObject(String sql, Class<R> requiredType, Object[] args) throws SQLException {
        try {
            return jdbcTemplate.queryForObject(sql, requiredType, args);
        } catch (Exception e) {
            if ((e instanceof IncorrectResultSizeDataAccessException) && (((IncorrectResultSizeDataAccessException) e).getActualSize() != 1)) {
                return null;
            }
        }
        return null;
    }

    // 查单个字段
    public R querySingleObject(String sql, Class<R> requiredType) throws SQLException {
        try {
            return jdbcTemplate.queryForObject(sql, requiredType);
        } catch (Exception e) {
            if ((e instanceof IncorrectResultSizeDataAccessException) && (((IncorrectResultSizeDataAccessException) e).getActualSize() != 1)) {
                return null;
            }
        }
        return null;
    }

    // 查单个字段集合
    public List<R> queryForList(String sql, Class<R> elementType, Object[] args) throws SQLException {
        return jdbcTemplate.queryForList(sql, elementType, args);
    }

    // 查单个字段集合
    public List<R> queryForList(String sql, Class<R> elementType) throws SQLException {
        return jdbcTemplate.queryForList(sql, elementType);
    }

    // 查询单个对象(带参),数据库字段与model字段对应
    public T querySingleBean(String sql, Object[] args, Class<T> requiredType) throws SQLException {
        try {
            return (T) jdbcTemplate.queryForObject(sql, args, new BeanPropertyRowMapper(requiredType));
        } catch (Exception e) {
            if ((e instanceof IncorrectResultSizeDataAccessException) && (((IncorrectResultSizeDataAccessException) e).getActualSize() != 1)) {
                return null;
            }
        }
        return null;
    }

    // 查询单个对象(不带参)
    public T querySingleBean(String sql, Class<T> requiredType) throws SQLException {
        try {
            return (T) jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper(requiredType));
        } catch (Exception e) {
            if ((e instanceof IncorrectResultSizeDataAccessException) && (((IncorrectResultSizeDataAccessException) e).getActualSize() != 1)) {
                return null;
            }
        }
        return null;
    }

    public T querySingleBeanById(String id) throws Exception {
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        Class<T> entityClass = (Class<T>) type.getActualTypeArguments()[0];
        String sql = this.appendSql(entityClass, null, SELECT);
        return querySingleBean(sql, new String[]{id}, entityClass);
    }

    // 查询对象列表(不带参)
    public List<T> queryListBean(String sql, Class<T> requiredType) throws SQLException {
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper(requiredType));
    }

    // 查询对象列表(带参)
    public List<T> queryListBean(String sql, Object[] args, Class<T> requiredType) throws SQLException {
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper(requiredType), args);
    }

    public int save(T entity) throws Exception {
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        Class<T> entityClass = (Class<T>) type.getActualTypeArguments()[0];
        String sql = this.appendSql(entityClass, entity, INSERT);
        Object[] args = this.setInsertArgs(entityClass, entity);
        return jdbcTemplate.update(sql, args);
    }

    public int update(T entity) throws Exception {
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        Class<T> entityClass = (Class<T>) type.getActualTypeArguments()[0];
        String sql = this.appendSql(entityClass, entity, UPDATE);
        Object[] args = this.setUpdateArgs(entityClass, entity);
        return jdbcTemplate.update(sql, args);
    }

    public int delete(T entity) throws Exception {
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        Class<T> entityClass = (Class<T>) type.getActualTypeArguments()[0];
        String sql = this.appendSql(entityClass, entity, DELETE);
        Object[] args = this.setDeleteArgs(entityClass, entity);
        return jdbcTemplate.update(sql, args);
    }

    private String appendSql(Class<T> entityClass, T entity, String type) throws IncorrectTableInfoException, IllegalArgumentException, IllegalAccessException {
        String tableName = fieldtoColumn(entityClass.getSimpleName());
        if (tableName == null) {
            throw new IncorrectTableInfoException("not found table name " + tableName);
        }

        Field[] fields = entityClass.getDeclaredFields();
        StringBuffer sb = new StringBuffer();

        switch (type) {
            case SELECT:
                String id = null;
                int c = 0;
                sb.append(" SELECT ");
                for (int i = 0; i < fields.length; i++) {
                    Field field = fields[i];
                    field.setAccessible(true);

                    if (field.isAnnotationPresent(Transient.class)) {
                        continue;
                    }

                    String column = fieldtoColumn(field.getName());
                    sb.append("`").append(column).append("`,");

                    // 注解是ID字段
                    if (field.isAnnotationPresent(Id.class)) {
                        id = column;
                        c++;
                        continue;
                    }
                }
                sb = sb.deleteCharAt(sb.length() - 1);
                sb.append(" FROM `").append(tableName).append("` WHERE ").append(id).append(" = ?");
                break;
            case INSERT:
                sb.append(" INSERT INTO `" + tableName).append("` (");
                int d = 0;
                for (int i = 0; i < fields.length; i++) {
                    Field field = fields[i];
                    field.setAccessible(true);

                    Object v = fields[i].get(entity);

                    if (field.isAnnotationPresent(IgnoreFieldAnnotation.class)) {
                        continue;
                    }

                    // 空值跳过
                    if (v == null || StringUtil.isEmpty(v.toString())) {
                        continue;
                    }

                    String column = fieldtoColumn(field.getName());
                    sb.append("`").append(column).append("`,");
                    d++;
                }
                sb = sb.deleteCharAt(sb.length() - 1);
                sb.append(") VALUES (");
                for (int i = 0; i < d; i++) {
                    sb.append("?,");
                }
                sb = sb.deleteCharAt(sb.length() - 1);
                sb.append(")");
                break;

            case UPDATE:
                sb.append(" UPDATE  `" + tableName).append("` SET ");

                id = null;
                c = 0;
                for (int i = 0; i < fields.length; i++) {
                    Field field = fields[i];
                    field.setAccessible(true);
                    Object v = fields[i].get(entity);

                    if (field.isAnnotationPresent(IgnoreFieldAnnotation.class)) {
                        continue;
                    }

                    // 空值跳过
                    if (v == null) {
                        continue;
                    }

                    String column = fieldtoColumn(field.getName());
                    // 注解是ID字段
                    if (field.isAnnotationPresent(Id.class)) {
                        id = column;
                        c++;
                        continue;
                    }
                    sb.append("`").append(column).append("`").append("=").append("?,");
                }
                sb = sb.deleteCharAt(sb.length() - 1);
                sb.append(" WHERE ").append("`").append(id).append("` = ?");
                if (c != 1) {
                    throw new IncorrectTableInfoException("the count of unique id is not 1, count:" + c);
                }
                break;
            case DELETE:
                sb.append(" DELETE FROM " + tableName + " WHERE ");
                String id1 = null;
                int c1 = 0;
                for (int i = 0; i < fields.length; i++) {
                    fields[i].setAccessible(true);

                    String column = fieldtoColumn(fields[i].getName());
                    // 注解是ID字段
                    if (fields[i].isAnnotationPresent(Id.class)) {
                        id1 = column;
                        c1++;
                        continue;
                    }
                }

                if (c1 != 1) {
                    throw new IncorrectTableInfoException("the count of unique id is not 1, count:" + c1);
                }

                sb.append(id1).append(" = ?");
                break;
        }

        return sb.toString();
    }

    private Object[] setInsertArgs(Class<T> entityClass, T entity) throws IncorrectTableInfoException, IllegalArgumentException, IllegalAccessException {
        Field[] fields = entityClass.getDeclaredFields();
        List<Object> l = new ArrayList<>();

        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isAnnotationPresent(IgnoreFieldAnnotation.class)) {
                continue;
            }

            Field field = fields[i];
            field.setAccessible(true);
            Object v = field.get(entity);

            // 跳过null空值
            if (v == null || "".equals(v)) {
                continue;
            }

            l.add(v);
        }

        return l.toArray();
    }

    private Object[] setUpdateArgs(Class<T> entityClass, T entity) throws IncorrectTableInfoException, IllegalArgumentException, IllegalAccessException {
        Field[] fields = entityClass.getDeclaredFields();
        List<Object> l = new ArrayList<>();

        Object idValue = null;
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isAnnotationPresent(IgnoreFieldAnnotation.class)) {
                continue;
            }

            Field field = fields[i];
            field.setAccessible(true);
            Object v = field.get(entity);

            // 跳过ID
            if (field.isAnnotationPresent(Id.class)) {
                idValue = v;
                continue;
            }

            // 跳过null空值
            if (v == null) {
                continue;
            }

            l.add(v);
        }

        l.add(idValue);
        return l.toArray();
    }

    private Object[] setDeleteArgs(Class<T> entityClass, T entity) throws IllegalArgumentException, IllegalAccessException {
        Field[] fields = entityClass.getDeclaredFields();
        Object[] args = new Object[1];
        for (int i = 0; args != null && i < args.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            if (field.isAnnotationPresent(Id.class)) {
                args[i] = fields[i].get(entity);
                break;
            }
        }
        return args;
    }

    public static String fieldtoColumn(String field) {
        if (StringUtil.isEmpty(field)) {
            return null;
        }

        int len = field.length();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len; i++) {
            char c = field.charAt(i);
            if (i == 0) {
                sb.append(c);
                continue;
            }

            if (c >= 'A' && c <= 'Z') {
                sb.append("_" + Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }

        return sb.toString().toUpperCase();
    }

    public int[] batchInsert(List<T> entitys) throws Exception {
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        Class<T> entityClass = (Class<T>) type.getActualTypeArguments()[0];
        String sql = this.appendSql(entityClass, entitys.get(0), INSERT);
        List<Object[]> batchArgs = new ArrayList<>();
        for (T entity : entitys) {
            Object[] args = this.setInsertArgs(entityClass, entity);
            batchArgs.add(args);
        }
        return jdbcTemplate.batchUpdate(sql, batchArgs);
    }

}