/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.core.util;

import com.jsonex.core.type.Identifiable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class ListUtil {
  public static <C extends Collection<TDest>, TSrc, TDest> C map(Collection<? extends TSrc> source, Function<? super TSrc, ? extends TDest> func, C dest) {
    if (source == null)
      return null;
    for (TSrc src : source)
      dest.add(func.apply(src));
    return dest;
  }

  public static <TSrc, TDest> List<TDest> map(Collection<? extends TSrc> source, Function<? super TSrc, ? extends TDest> func) {
    return map(source, func, new ArrayList<>());
  }

  public static <TId> List<TId> getIds(Collection<? extends Identifiable<TId>> identifiables) {
    return map(identifiables, param -> param.getId());
  }

  public static <K, E> Map<K, List<E>> groupBy(Collection<? extends E> source, Function<? super E, ? extends K> classifier) {
    Map<K, List<E>> result = new HashMap<>();
    for (E e : source) {
      K k = classifier.apply(e);
      List<E> v = result.get(k);
      if (v == null) {
        v = new ArrayList<>();
        result.put(k, v);
      }
      v.add(e);
    }
    return result;
  }

  public static <K, V, T> Map<K, T> mapValues(Map<? extends K, ? extends V> source, Function<? super V, ? extends T> func) {
    Map<K, T> result = new HashMap<>();
    for (K k : source.keySet())
      result.put(k, func.apply(source.get(k)));
    return result;
  }

  /**
   * The list should contain Long values. Otherwise ClassCastException will be thrown.
   */
  public static long[] toLongArray(Collection<Object> list) {
    long[] result = new long[list.size()];
    int i = 0;
    for (Object e : list) {
      long l;
      if (e == null)
        l = 0;
      else if (e instanceof String)
        l = Long.parseLong((String)e);
      else if (e instanceof Number)
        l = ((Number)e).longValue();
      else
        throw new IllegalArgumentException("Expect list of String or Numbers, actual got: " + e.getClass() + ", e=" + e);
      result[i++] = l;
    }
    return result;
  }

  public static <K, V> Map<K, V> toMap(Collection<V> source, Function<? super V, ? extends K> keyFunc) {
    Map<K, V> map = new HashMap<>();
    for (V s : source)
      map.put(keyFunc.apply(s), s);
    return map;
  }

  public static <S, K, V> Map<K, V> toMap(Collection<S> source, Function<? super S, ? extends K> keyFunc, Function<? super S, ? extends V> valFunc) {
    Map<K, V> map = new HashMap<>();
    for (S s : source)
      map.put(keyFunc.apply(s), valFunc.apply(s));
    return map;
  }

  public static <V, C extends Collection<V>> C filter(C source, Predicate<? super V> pred, C dest) {
    for (V s : source) {
      if (pred.test(s))
        dest.add(s);
    }
    return dest;
  }

  public static <V, C extends Collection<V>> List<V> filter(C source, Predicate<? super V> pred) {
    List<V> dest = new ArrayList<>();
    filter(source, pred, dest);
    return dest;
  }

  public static <V, C extends Collection<V>> List<V> orderBy(C source, final Function<? super V, ? extends Comparable> by) {
    return orderBy(source, by, false);
  }

  public static <V, C extends Collection<V>, K extends Comparable<K>> List<V> orderBy(C src, final Function<? super V, K> by, final boolean desc) {
    List<V> dest = new ArrayList<>(src.size());
    dest.addAll(src);  // clone
    Collections.sort(dest, (o1, o2) -> {
      K v1 = by.apply(o1);
      K v2 = by.apply(o2);
      int result;
      if (v1 == null)
        result = v2 == null ? 0 : -1;
      else {
        result = v2 == null ? 1 : v1.compareTo(v2);
      }
      return desc ? - result : result;
    });
    return dest;
  }

  public static boolean contains(long[] longs, long match) {
    for (long l : longs)
      if (match == l)
        return true;
    return false;
  }

  public static <T> String join(T[] list, String delimiter) { return join(Arrays.asList(list), delimiter); }
  public static <T> String join(Collection<T> list, String delimiter) {
    StringBuilder sb = new StringBuilder();
    for(Object obj : list) {
      if(sb.length() > 0)
        sb.append(delimiter);
      sb.append(obj);
    }
    return sb.toString();
  }

  public static <V, C extends Collection<V>> boolean exists(C source, Predicate<? super V> pred) {
    for (V s : source) {
      if (pred.test(s))
        return true;
    }
    return false;
  }

  public static <V, C extends Collection<V>> V first(C source, Predicate<? super V> pred) {
    for (V s : source) {
      if (pred.test(s))
        return s;
    }
    return null;
  }

  public static <T> T last(List<T> list) { return list == null ? null : list.get(list.size() - 1); }
  public static <T> T first(Collection<T> list) { return list == null || list.size() == 0 ? null : list.iterator().next(); }

  public static <T> boolean containsAny(Collection<T> list, T... elements) {
    for (T e : elements) {
      if (list.contains(e))
        return true;
    }
    return false;
  }

  public static void removeLast(List<?> list) { list.remove(list.size() - 1); }

  public static <T> Set<T> setOf(T... e) { return new HashSet<>(Arrays.asList(e)); }
}
