package com.yangdb.logging.log4j2.spi;

/*-
 * #%L
 * logging
 * %%
 * Copyright (C) 2016 - 2022 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.logging.log4j.spi.CopyOnWrite;
import org.apache.logging.log4j.util.StringMap;

import java.util.Map;

public interface ReadOnlyExtendedThreadContextMap {
    /**
     * Clears the context.
     */
    void clear();

    /**
     * Determines if the key is in the context.
     * @param key The key to locate.
     * @return True if the key is in the context, false otherwise.
     */
    boolean containsKey(final String key);

    /**
     * Gets the context identified by the <code>key</code> parameter.
     *
     * <p>This method has no side effects.</p>
     * @param key The key to locate.
     * @return The value associated with the key or null.
     */
    <V> V get(final String key);

    /**
     * Gets a non-{@code null} mutable copy of current thread's context Map.
     * @return a mutable copy of the context.
     */
    Map<String, Object> getCopy();

    /**
     * Returns an immutable view on the context Map or {@code null} if the context map is empty.
     * @return an immutable context Map or {@code null}.
     */
    Map<String, Object> getImmutableMapOrNull();

    /**
     * Returns the context data for reading. Note that regardless of whether the returned context data has been
     * {@linkplain StringMap#freeze() frozen} (made read-only) or not, callers should not attempt to modify
     * the returned data structure.
     * <p>
     * <b>Thread safety note:</b>
     * </p>
     * <p>
     * If this {@code ReadOnlyThreadContextMap} implements {@link CopyOnWrite}, then the returned {@code StringMap} can
     * safely be passed to another thread: future changes in the underlying context data will not be reflected in the
     * returned {@code StringMap}.
     * </p><p>
     * Otherwise, if this {@code ReadOnlyThreadContextMap} does <em>not</em> implement {@link CopyOnWrite}, then it is
     * not safe to pass the returned {@code StringMap} to another thread because changes in the underlying context may
     * be reflected in the returned object. It is the responsibility of the caller to make a copy to pass to another
     * thread.
     * </p>
     *
     * @return a {@code StringMap} containing context data key-value pairs
     */
    ExtendedStringMap getReadOnlyContextData();

    /**
     * Returns true if the Map is empty.
     * @return true if the Map is empty, false otherwise.
     */
    boolean isEmpty();
}
