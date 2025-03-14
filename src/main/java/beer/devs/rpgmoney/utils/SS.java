package beer.devs.rpgmoney.utils;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * Utility class with shorthands to make code S-horter.
 */

public class SS
{
    public static <K> void counter(HashMap<K, Integer> map, K key)
    {
        map.merge(key, 1, Integer::sum);
    }

    public static <A, X, Y> Y mapData(HashMap<A, HashMap<X, Y>> map, A mainKey, X secondKey, Function<X, Y> sus)
    {
        return map.computeIfAbsent(mainKey, k -> new HashMap<>()).computeIfAbsent(secondKey, sus);
    }

    public static String[] arr(String ...str)
    {
        return str;
    }

    @SafeVarargs
    public static <T> List<T> finalList(T ...str)
    {
        return Arrays.asList(str);
    }

    @SafeVarargs
    public static <T> List<T> list(T ...str)
    {
        return new ArrayList<>(Arrays.asList(str));
    }

    public static <T> List<T> allocListByTrueBools(boolean... bools)
    {
        return new ArrayList<>(count(bools));
    }

    @SafeVarargs
    public static <T> List<T> list(boolean ignoreNull, T ...str)
    {
        List<T> list = new ArrayList<>();
        for (T t : str)
        {
            if (t == null)
                continue;
            list.add(t);
        }
        return list;
    }

    /**
     * Fast way to handle a list which can be null if no entries are put.
     * Useful to avoid allocating RAM if no element will be put into this list.
     */
    public static <T> List<T> add(List<T> list, T val)
    {
        if(list == null)
            list = new ArrayList<>();

        list.add(val);
        return list;
    }

    public static <T> boolean has(List<T> list, T val)
    {
        if(list == null)
            return false;

        return list.contains(val);
    }

    @Nullable
    public static <K,V> K findKey(Map<K,V> map, V value)
    {
        for (Map.Entry<K, V> entry : map.entrySet())
        {
            if(entry.getValue().equals(value))
                return entry.getKey();
        }
        return null;
    }

    public static <Q,T> boolean opt(Q qq, PropFunc<Boolean,Q> cons)
    {
        if(qq != null)
            return cons.call(qq);
        return false;
    }

    public static <Q,T> T opt(Q qq, PropFunc<T,Q> cons, T def)
    {
        if(qq != null)
            return cons.call(qq);
        return def;
    }

    public static int count(boolean... bools)
    {
        int count = 0;
        for (boolean bool : bools)
        {
            if (bool)
                count++;
        }
        return count;
    }

    @FunctionalInterface
    public interface PropFunc<V,Q>
    {
        V call(Q qq);
    }
}
