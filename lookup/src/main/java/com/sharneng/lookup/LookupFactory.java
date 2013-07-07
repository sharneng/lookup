/*
 * Copyright (c) 2011 Original Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sharneng.lookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to hold factory methods to return the implementation of {@link Lookup}.
 * 
 * @author Kenneth Xu
 * 
 */
public final class LookupFactory {

    private LookupFactory() {
    }

    /**
     * Creates a lookup based on given map.
     * 
     * @param map
     *            the map to create the lookup
     * @param <T>
     *            type of the reference object to be looked up
     * @return an implementation of {@link Lookup} that is backed by given map
     * 
     */
    public static <T> Lookup<T> create(final Map<? extends Object, ? extends T> map) {
        if (map == null) throw new IllegalArgumentException(notNull("map"));
        return new MapBasedLookup<T>(map);
    }

    /**
     * Create a lookup for objects in the given collection indexed by given property.
     * <p>
     * The class where the index property is searched for is determined by the first element in the collection.
     * 
     * @param values
     *            a collection of objects that can be looked up.
     * @param property
     *            the index property
     * @param <T>
     *            type of the reference object to be looked up
     * @return an implementation of {@link Lookup} indexed by the specified property
     */
    public static <T> Lookup<T> create(final Collection<? extends T> values, final String property) {
        checkValues(values);
        if (property == null) throw new IllegalArgumentException(notNull("property"));
        return create(values, getElementClass(values), property);
    }

    public static <T> Lookup<Lookup<T>> create(final Collection<? extends T> values, final String property1,
            final String property2) {
        final Lookup<?> lookup = create(values, new String[] { property1, property2 });
        @SuppressWarnings("unchecked")
        final Lookup<Lookup<T>> result = (Lookup<Lookup<T>>) lookup;
        return result;
    }

    public static <T> Lookup<?> create(final Collection<? extends T> values, final String... properties) {
        checkValues(values);
        return create(values, getElementClass(values), properties);
    }

    /**
     * Create a {@link Lookup} for objects in the given collection indexed by given property defined on given class.
     * 
     * @param values
     *            a collection of objects that can be looked up
     * @param clazz
     *            the class where the index property is searched for
     * @param property
     *            the index property
     * @param <T>
     *            type of the reference object to be looked up
     * @return an implementation of {@link Lookup} indexed by the specified property
     */
    public static <T> Lookup<T> create(final Collection<? extends T> values, final Class<T> clazz, final String property) {
        checkValues(values);
        if (clazz == null) throw new IllegalArgumentException(notNull("clazz"));
        if (property == null) throw new IllegalArgumentException(notNull("property"));
        return create(values, new PropertyConverter<T>(clazz, property));
    }

    public static <T> Lookup<Lookup<T>> create(final Collection<? extends T> values, final Class<T> clazz,
            final String property1, final String property2) {
        final Lookup<?> lookup = create(values, clazz, new String[] { property1, property2 });
        @SuppressWarnings("unchecked")
        final Lookup<Lookup<T>> result = (Lookup<Lookup<T>>) lookup;
        return result;
    }

    public static <T> Lookup<?> create(final Collection<? extends T> values, final Class<T> clazz,
            final String... properties) {
        checkValues(values);
        if (clazz == null) throw new IllegalArgumentException(notNull("clazz"));
        if (properties == null) throw new IllegalArgumentException(notNull("properties"));
        if (properties.length == 0) throw new IllegalArgumentException("At least one property must be supplied");
        @SuppressWarnings("unchecked")
        Converter<T, Object>[] converters = new Converter[properties.length];
        for (int i = 0; i < properties.length; i++) {
            if (properties[i] == null) throw new IllegalArgumentException(notNull("property" + (i + 1)));
            converters[i++] = new PropertyConverter<T>(clazz, properties[i]);
        }
        return create(values, converters);
    }

    public static <T> Lookup<T> create(final Collection<? extends T> values, final Converter<T, Object> converter) {
        checkValues(values);
        if (converter == null) throw new IllegalArgumentException(notNull("converter"));
        return createPrivate(values, converter);
    }

    public static <T> Lookup<Lookup<T>> create(final Collection<? extends T> values,
            final Converter<T, Object> converter1, final Converter<T, Object> converter2) {
        @SuppressWarnings("unchecked")
        Lookup<Lookup<T>> result = (Lookup<Lookup<T>>) create(values, new Converter[] { converter1, converter2 });
        return result;
    }

    public static <T> Lookup<?> create(final Collection<? extends T> values, final Converter<T, Object>... converters) {
        checkValues(values);
        checkConverters(converters);
        return createMultiLookup(values, 0, converters);
    }

    private static <T> Class<T> getElementClass(final Collection<? extends T> values) {
        for (T value : values) {
            if (value != null) {
                @SuppressWarnings("unchecked")
                final Class<T> clazz = (Class<T>) value.getClass();
                return clazz;
            }
        }
        throw new IllegalArgumentException("Argument values collection must contain non-null element");
    }

    private static <T> Lookup<T> createPrivate(final Collection<? extends T> values,
            final Converter<T, Object> converter) {
        return new MapBasedLookup<T>(values, converter);
    }

    private static <T> Lookup<?> createMultiLookup(final Collection<? extends T> values, int index,
            final Converter<T, Object>[] converters) {

        Converter<T, Object> converter = converters[index];
        if (++index == converters.length) return createPrivate(values, converter); // last one

        Map<Object, Collection<T>> map = new HashMap<Object, Collection<T>>();
        for (T value : values) {
            Object key = converter.convert(value);
            Collection<T> c = map.get(key);
            if (c == null) {
                c = new ArrayList<T>();
                map.put(key, c);
            }
            c.add(value);
        }

        Map<Object, Lookup<?>> lookupMap = new HashMap<Object, Lookup<?>>();
        for (Map.Entry<Object, Collection<T>> entry : map.entrySet()) {
            lookupMap.put(entry.getKey(), createMultiLookup(entry.getValue(), index, converters));
        }

        return new MapBasedLookup<Lookup<?>>(lookupMap);
    }

    private static void checkValues(final Collection<?> values) {
        if (values == null) throw new IllegalArgumentException(notNull("values"));
        if (values.size() == 0) throw new IllegalArgumentException("Argument values collection must not be empty");
    }

    private static void checkConverters(final Converter<?, Object>[] converters) {
        if (converters == null) throw new IllegalArgumentException(notNull("converters"));
        if (converters.length == 0) throw new IllegalArgumentException("At least one converter must be supplied");
        for (int i = 0; i < converters.length; i++) {
            if (converters[i] == null) throw new IllegalArgumentException(notNull("converter" + (i + 1)));
        }
    }

    private static String notNull(final String argumentName) {
        return "Argument " + argumentName + " must not be null.";
    }
}
