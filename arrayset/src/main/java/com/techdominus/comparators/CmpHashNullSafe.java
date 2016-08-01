/*
 * Copyright (C) 2016 Shaun Halder
 *
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
 */

package com.techdominus.comparators;
import java.util.Comparator;

/**
 * A nullsafe hashcode comparator.
 */

@SuppressWarnings("unused")
public enum CmpHashNullSafe implements Comparator<Object> {
    instance;

    @Override
    public int compare(Object o1, Object o2) {

        int hash1 = o1 == null ? 0 : o1.hashCode();
        int hash2 = o2 == null ? 0 : o2.hashCode();

        return hash1 < hash2 ? -1 : hash1 == hash2 ? 0 : 1;
    }
}
