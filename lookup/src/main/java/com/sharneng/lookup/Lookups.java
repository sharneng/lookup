/*
 * Copyright (c) 2013 Original Authors
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

import java.util.Collection;
import java.util.Map;

import javax.annotation.CheckForNull;

/**
 * Class to hold factory methods to return the implementation of {@link Lookup}.
 * 
 * @author Kenneth Xu
 * 
 */
public final class Lookups {

    /**
     * Lookup API supports no more than 10 levels of lookups.
     */
    public static final int LEVEL_LIMIT = 10;

    private Lookups() {
    }

    /**
     * Creates a lookup based on given map.
     * 
     * @param map
     *            the map to create the lookup
     * 
     * @param <T>
     *            type of the reference object to be looked up
     * @return an implementation of {@link Lookup} that is backed by given map
     * 
     */
    public static <T> Lookup<T> create(@CheckForNull T defaultValue, final Map<? extends Object, ? extends T> map) {
        if (map == null) throw new IllegalArgumentException(Utils.notNull("map"));
        return new MapBasedLookup<T>(defaultValue, map);
    }

    /* values, string */

    /**
     * Create a lookup for objects in the given collection indexed by given property.
     * <p>
     * The class where the index property is searched for is determined by the first non-null element in the collection.
     * Other than that, this is equivalent to {@link #create(Class, Collection, String)}.
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
        if (property == null) throw new IllegalArgumentException(Utils.notNull("property"));
        return new MapBasedLookup<T>(null, values, new PropertyConverter<T>(getElementClass(values), property));
    }

    /**
     * Create a two level lookup for objects in the given collection indexed by given properties.
     * <p>
     * The class where the index properties are searched for is determined by the first non-null element in the
     * collection. Other than that, this is equivalent to {@link #create(Class, Collection, String, String)}.
     * 
     * @param values
     *            a collection of objects that can be looked up.
     * @param property1
     *            the first index property
     * @param property2
     *            the second index property
     * @param <T>
     *            type of the reference object to be looked up
     * @return an implementation of {@link Lookup} of {@code Lookup} indexed by the specified properties in order
     */
    public static <T> Lookup<Lookup<T>> create(final Collection<? extends T> values, final String property1,
            final String property2) {
        return Utils.toLookup2(create(values, new String[] { property1, property2 }));
    }

    /**
     * Create multilevel lookup for objects in the given collection indexed by given properties.
     * <p>
     * The class where the index properties are searched for is determined by the first non-null element in the
     * collection. Other than that, it is equivalent to {@link #create(Class, Collection, String...)}.
     * 
     * @param values
     *            a collection of objects that can be looked up.
     * @param properties
     *            the index properties
     * @param <T>
     *            type of the reference object to be looked up
     * @return an implementation of multilevel {@link Lookup} indexed by the specified properties
     */
    public static <T> Lookup<?> create(final Collection<? extends T> values, final String... properties) {
        checkValues(values);
        return new LookupBuilder<T>(null, getElementClass(values), properties).build(values);
    }

    /* default, values, string */

    /**
     * Create a lookup for objects in the given collection indexed by given property.
     * <p>
     * The class where the index property is searched for is determined by the first non-null element in the collection.
     * Other than that, this is equivalent to {@link #create(Class, Collection, String)}.
     * 
     * @param values
     *            a collection of objects that can be looked up.
     * @param property
     *            the index property
     * @param <T>
     *            type of the reference object to be looked up
     * @return an implementation of {@link Lookup} indexed by the specified property
     */
    public static <T> Lookup<T> create(@CheckForNull T defaultValue, final Collection<? extends T> values,
            final String property) {
        return defaultValue != null ? create(Utils.getClass(defaultValue), defaultValue, values, property) : create(
                values, property);
    }

    /**
     * Create a two level lookup for objects in the given collection indexed by given properties.
     * <p>
     * The class where the index properties are searched for is determined by the first non-null element in the
     * collection. Other than that, this is equivalent to {@link #create(Class, Collection, String, String)}.
     * 
     * @param values
     *            a collection of objects that can be looked up.
     * @param property1
     *            the first index property
     * @param property2
     *            the second index property
     * @param <T>
     *            type of the reference object to be looked up
     * @return an implementation of {@link Lookup} of {@code Lookup} indexed by the specified properties in order
     */
    public static <T> Lookup<Lookup<T>> create(@CheckForNull T defaultValue, final Collection<? extends T> values,
            final String property1, final String property2) {
        return defaultValue != null ? create(Utils.getClass(defaultValue), defaultValue, values, property1, property2)
                : create(values, property1, property2);
    }

    /**
     * Create multilevel lookup for objects in the given collection indexed by given properties defined on given class.
     * 
     * @param defaultValue
     *            TODO the class where the index property is searched for
     * @param values
     *            a collection of objects that can be looked up.
     * @param properties
     *            the index properties
     * 
     * @param <T>
     *            type of the reference object to be looked up
     * @return an implementation of multilevel {@link Lookup} indexed by the specified properties
     */
    public static <T> Lookup<?> create(T defaultValue, final Collection<? extends T> values, final String... properties) {
        return defaultValue != null ? create(Utils.getClass(defaultValue), defaultValue, values, properties) : create(
                values, properties);
    }

    /* class, values, string */

    /**
     * Create a {@link Lookup} for objects in the given collection indexed by given property defined on given class.
     * 
     * @param clazz
     *            the class where the index property is searched for
     * @param values
     *            a collection of objects that can be looked up
     * @param property
     *            the index property
     * 
     * @param <T>
     *            type of the reference object to be looked up
     * @return an implementation of {@link Lookup} indexed by the specified property
     */
    public static <T> Lookup<T> create(final Class<? extends T> clazz, final Collection<? extends T> values,
            final String property) {
        return create(clazz, null, values, property);
    }

    /**
     * Create a two level lookup for objects in the given collection indexed by given properties defined on given class.
     * 
     * @param clazz
     *            the class where the index property is searched for
     * @param values
     *            a collection of objects that can be looked up.
     * @param property1
     *            the first index property
     * @param property2
     *            the second index property
     * 
     * @param <T>
     *            type of the reference object to be looked up
     * @return an implementation of {@link Lookup} of {@code Lookup} indexed by the specified properties in order
     */
    public static <T> Lookup<Lookup<T>> create(final Class<? extends T> clazz, final Collection<? extends T> values,
            final String property1, final String property2) {
        return create(clazz, null, values, property1, property2);
    }

    /**
     * Create multilevel lookup for objects in the given collection indexed by given properties defined on given class.
     * 
     * @param clazz
     *            the class where the index property is searched for
     * @param values
     *            a collection of objects that can be looked up.
     * @param properties
     *            the index properties
     * 
     * @param <T>
     *            type of the reference object to be looked up
     * @return an implementation of multilevel {@link Lookup} indexed by the specified properties
     */
    public static <T> Lookup<?> create(final Class<? extends T> clazz, final Collection<? extends T> values,
            final String... properties) {
        return create(clazz, null, values, properties);
    }

    /* class, default, values, string */

    /**
     * Create a lookup for objects in the given collection indexed by given property.
     * <p>
     * The class where the index property is searched for is determined by the first non-null element in the collection.
     * Other than that, this is equivalent to {@link #create(Class, Collection, String)}.
     * 
     * @param values
     *            a collection of objects that can be looked up.
     * @param property
     *            the index property
     * @param <T>
     *            type of the reference object to be looked up
     * @return an implementation of {@link Lookup} indexed by the specified property
     */
    public static <T> Lookup<T> create(final Class<? extends T> clazz, @CheckForNull T defaultValue,
            final Collection<? extends T> values, final String property) {
        checkValues(values);
        if (property == null) throw new IllegalArgumentException(Utils.notNull("property"));
        return new MapBasedLookup<T>(defaultValue, values, new PropertyConverter<T>(clazz, property));
    }

    /**
     * Create a two level lookup for objects in the given collection indexed by given properties.
     * <p>
     * The class where the index properties are searched for is determined by the first non-null element in the
     * collection. Other than that, this is equivalent to {@link #create(Class, Collection, String, String)}.
     * 
     * @param values
     *            a collection of objects that can be looked up.
     * @param property1
     *            the first index property
     * @param property2
     *            the second index property
     * @param <T>
     *            type of the reference object to be looked up
     * @return an implementation of {@link Lookup} of {@code Lookup} indexed by the specified properties in order
     */
    public static <T> Lookup<Lookup<T>> create(final Class<? extends T> clazz, @CheckForNull T defaultValue,
            final Collection<? extends T> values, final String property1, final String property2) {
        return Utils.toLookup2(create(clazz, defaultValue, values, new String[] { property1, property2 }));
    }

    /**
     * Create multilevel lookup for objects in the given collection indexed by given properties defined on given class.
     * 
     * @param clazz
     *            the class where the index property is searched for
     * @param values
     *            a collection of objects that can be looked up.
     * @param properties
     *            the index properties
     * 
     * @param <T>
     *            type of the reference object to be looked up
     * @return an implementation of multilevel {@link Lookup} indexed by the specified properties
     */
    public static <T> Lookup<?> create(final Class<? extends T> clazz, @CheckForNull T defaultValue,
            final Collection<? extends T> values, final String... properties) {
        checkValues(values);
        return new LookupBuilder<T>(defaultValue, clazz, properties).build(values);
    }

    /* default, values, converter */

    static <T> Lookup<T> create(T defaultValue, final Collection<? extends T> values,
            final Converter<T, Object> converter) {
        checkValues(values);
        if (converter == null) throw new IllegalArgumentException(Utils.notNull("converter"));
        return new MapBasedLookup<T>(defaultValue, values, converter);
    }

    static <T> Lookup<Lookup<T>> create(T defaultValue, final Collection<? extends T> values,
            final Converter<T, Object> converter1, final Converter<T, Object> converter2) {
        final Lookup<?> create = create(defaultValue, values, Utils.toGeneric(converter1, converter2));
        return Utils.toLookup2(create);
    }

    static <T> Lookup<?> create(T defaultValue, final Collection<? extends T> values,
            final Converter<T, Object>... converters) {
        checkValues(values);
        checkConverters(converters);
        return new LookupBuilder<T>(defaultValue, converters).build(values);
    }

    private static <T> Class<T> getElementClass(final Collection<? extends T> values) {
        for (T value : values)
            if (value != null) return Utils.getClass(value);
        throw new IllegalArgumentException("Argument values collection must contain non-null element");
    }

    private static void checkValues(final Collection<?> values) {
        if (values == null) throw new IllegalArgumentException(Utils.notNull("values"));
        if (values.size() == 0) throw new IllegalArgumentException("Argument values collection must not be empty");
    }

    private static void checkConverters(final Converter<?, Object>[] converters) {
        if (converters == null) throw new IllegalArgumentException(Utils.notNull("converters"));
        if (converters.length == 0) throw new IllegalArgumentException("At least one converter must be supplied");
        for (int i = 0; i < converters.length; i++) {
            if (converters[i] == null) throw new IllegalArgumentException(Utils.notNull("converter" + (i + 1)));
        }
    }
}
