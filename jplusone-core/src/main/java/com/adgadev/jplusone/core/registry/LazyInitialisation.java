/*
 * Copyright (c) 2020 Adam Gaj
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adgadev.jplusone.core.registry;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LazyInitialisation {

    public enum LazyInitialisationType {ENTITY, COLLECTION}

    private final String entityClassName;

    private final String fieldName;

    private final LazyInitialisationType type;

    public static LazyInitialisation entityLazyInitialisation(String entityClassName) {
        return new LazyInitialisation(entityClassName, null, LazyInitialisationType.ENTITY);
    }

    public static LazyInitialisation collectionLazyInitialisation(String entityClassName, String fieldName) {
        return new LazyInitialisation(entityClassName, fieldName, LazyInitialisationType.COLLECTION);
    }

    @Override
    public String toString() {
        return type == LazyInitialisationType.COLLECTION
                ? entityClassName + '.' + fieldName + " [FETCHING COLLECTION]"
                : entityClassName + " [FETCHING ENTITY]";
    }

}
