package main.java.tukano.impl.storage.cache;

import main.java.tukano.api.Result;
import main.java.tukano.api.Short;
import main.java.utils.JSON;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import static main.java.tukano.api.Result.error;
import static main.java.tukano.api.Result.ok;

public class RedisCache {
    private static final String RedisHostname = System.getenv("REDIS_URL");
    private static final String RedisKey = System.getenv("REDIS_KEY");
    private static final int REDIS_PORT = 6380;
    private static final int REDIS_TIMEOUT = 1000;
    private static final boolean Redis_USE_TLS = true;

    private static JedisPool instance;

    private synchronized static JedisPool getCachePool() {
        if( instance != null)
            return instance;

        var poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(128);
        poolConfig.setMinIdle(16);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
        instance = new JedisPool(poolConfig, RedisHostname, REDIS_PORT, REDIS_TIMEOUT, RedisKey, Redis_USE_TLS);
        return instance;
    }

    public static class Obtain {
        public <T> Obtain(String iid, Function<String, T> decodee) {
            id=iid;
            decode=decodee;
        }
        protected String id;
        protected Function<String, ?> decode;
    }

    public static class Insert <T> {
        public Insert(String iid, T oobject, Function<T, String> encodee) {
            id=iid;
            object=oobject;
            encode=encodee;
        }
        protected String id;
        protected T object;
        protected Function<T, String> encode;
    }


    public synchronized static <T> Result<T> getRedis(Jedis jedis, String id, Function<String, T> decode) {
        String res = jedis.get(id);
        T result = decode.apply(res);
        if (result != null) {
            return Result.ok(result);
        } else {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }
    }

    public synchronized static <T> Result<List<T>> getRedis(Jedis jedis, List<Obtain> ids) {
        List<T> results = new java.util.ArrayList<T>(List.of());
        ids.forEach((i) -> {
            String res = jedis.get(i.id);
            results.add((T) i.decode.apply(res));
        });
        if (results.stream().allMatch(n -> n!= null)) {
            return Result.ok(results);
        } else {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }
    }

    public synchronized static <T> Result<String> setRedis(Jedis jedis, String id, T object, Function<T, String> encode) {
        String res = jedis.set(id, encode.apply(object));
        if (Objects.equals(res, "OK")) {
            return Result.ok(res);
        } else {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }
    public synchronized static <T> Result<Void> setRedis(Jedis jedis, List<Insert<T>> ids) {
        List<String> results = new java.util.ArrayList<>(List.of());
        ids.forEach((i) -> {
            String res = jedis.set(i.id, i.encode.apply(i.object));
            results.add(res);
        });
        if (results.stream().allMatch(n -> Objects.equals(n, "OK"))) {
            return Result.ok();
        } else {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    public synchronized static <T> Result<T> doRedis(Function<Jedis, Result<T>> f) {
        if (System.getenv("REDIS_AVAILABLE").equalsIgnoreCase("TRUE")) {
            try (var jedis = RedisCache.getCachePool().getResource()) {
                return f.apply(jedis);
            }
            catch (Exception e){
                System.err.println(e);
                return error(Result.ErrorCode.INTERNAL_ERROR);
            }
        }
        return error(Result.ErrorCode.UNAVAILABLE);
    }

}