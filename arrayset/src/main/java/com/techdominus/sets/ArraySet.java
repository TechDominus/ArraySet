/*
 * Modifications Copyright (C) 2016 Shaun Halder
 * Copyright (C) 2013 The Android Open Source Project
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
 *
 * Adapted From:
 * - android.util.ArraySet (5.1.1_r1)
 * - libcore.util.EmptyArray (5.0.0_r2-robolectric-1)
 */

package com.techdominus.sets;

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

/**
 * A variation of the android.util.ArraySet that contains no API dependencies and can be
 * ordered using a Comparator.
 *
 * The Android implementation from which this is adapted indexed items by hashCode. This version
 * offers more flexibility by taking an optional Comparator parameter during instantiation
 * allowing the user to specify their own indexing.
 *
 * ArraySets instantiated with a Comparator are also more memory efficient; they eliminate the
 * default internal caching of hashCodes, but at the cost of having to rely on their Comparator
 * instead of cached data for order and search operations.
 *
 * Original Documentation:
 * ArraySet is a generic set data structure that is designed to be more memory efficient than a
 * traditional HashSet. The design is very similar to ArrayMap, with all of the caveats described
 * there. This implementation is separate from ArrayMap, however, so the Object array contains
 * only one item for each entry in the set (instead of a pair for a mapping).
 *
 * Note that this implementation is not intended to be appropriate for data structures that may
 * contain large numbers of items. It is generally slower than a traditional HashSet, since lookups
 * require a binary search and adds and removes require inserting and deleting entries in the array.
 * For containers holding up to hundreds of items, the performance difference is not significant,
 * less than 50%.
 *
 * Because this container is intended to better balance memory use, unlike most other standard Java
 * containers it will shrink its array as items are removed from it. Currently you have no
 * control over this shrinking -- if you set a capacity and then remove an item, it may reduce the
 * capacity to better match the current size. In the future an explicit call to set the capacity
 * should turn off this aggressive shrinking behavior.
 */

@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class ArraySet<E> implements Collection<E>, Set<E> {

    /**
     * The minimum amount by which the capacity of a ArraySet will increase.
     * This is tuned to be relatively space-efficient.
     */
    static final int BASE_SIZE = 4;

    /**
     * Maximum number of entries to have in array caches.
     */
    static final int CACHE_SIZE = 10;

    /**
     * Avoid spamming empty Arrays - point all EmptyArray
     * Refs to same array.
     */
    static final int[] EMPTYARRAY_INT = new int[0];
    static final Object[] EMPTYARRAY_OBJECT = new Object[0];

    /**
     * Holds Array Cache Pool -- recycles arrays with sizes equal
     * to x1 or x2 {@link #BASE_SIZE} to avoid spamming short-lived
     * Objects.
     */

    //Requires Android API level 9 or higher.
    final static ArrayDeque<Object[]> mObjCache = new ArrayDeque<>();
    final static ArrayDeque<int[]> mIntCache = new ArrayDeque<>();

    /**
     * Instance Fields
     */
    Object[] mArray;  //Holds Set Objects
    int mSize = 0;    //How much of mArray is filled


    /**
     * Constructor is package only, instead use static factory methods like
     * {@link ArraySet#newInstance()}.
     */

    ArraySet(){
        //Constructor is package only
    }

    /**
     *  Returns a default ArraySet which orders objects based on hashcode.
     */

    static public <E> ArraySet<E> newInstance(){
        return new ArraySetUsingHash<>();
    }

    /**
     *  Returns a default ArraySet which orders objects based on hashcode.
     *
     *  @param capacity sets the initial capacity of the internal array.
     */

    static public <E> ArraySet<E> newInstance(int capacity){
        return new ArraySetUsingHash<>(capacity);
    }

    /**
     *  Implements an ArraySet which orders objects based on the supplied Comparator.
     *
     *  <p> Comparator instances are more memory efficient because they don't internally cache
     *  ordinal data, relying exclusively on their Comparator for ordering and sorting, but they
     *  are also likely slower for the same reason.
     *
     *  @param cmp Comparator used to order objects in the ArraySet
     */

    static public <E> ArraySet<E> newInstance(Comparator<? super E> cmp){
        return new ArraySetUsingComparator<>(cmp);
    }

    /**
     *  Implements an ArraySet which orders objects based on the supplied Comparator.
     *
     *  <p> Comparator instances are more memory efficient because they don't internally cache
     *  ordinal data, relying exclusively on their Comparator for ordering and sorting, but they
     *  are also likely slower for the same reason.
     *
     *  @param capacity sets the initial capacity of the internal array.
     *  @param cmp Comparator used to order objects in the ArraySet
     */

    static public <E> ArraySet<E> newInstance(int capacity, Comparator<? super E> cmp){
        return new ArraySetUsingComparator<>(capacity, cmp);
    }

    /**
     * Check whether a value exists in the set.
     *
     * @param key The value to search for.
     * @return Returns true if the value exists, else false.
     */

    final public boolean contains(Object key) {
        return indexOf(key) >= 0;
    }

    /**
     * Return the value at the given index in the backing array.
     *
     * @param index The desired index, must be between 0 and {@link #size()}-1.
     * @return Returns the value stored at the given index.
     */

    @SuppressWarnings("unchecked")
    final public E valueAt(int index) {
        return (E) mArray[index];
    }

    /**
     * Removes the specified object from this set.
     *
     * @param object the object to remove.
     * @return {@code true} if this set was modified, {@code false} otherwise.
     */

    final public boolean remove(Object object) {
        final int index = indexOf(object);
        if (index >= 0) {
            removeAt(index);
            return true;
        }
        return false;
    }

    /**
     * Removes all objects from ArraySet not in collection
     *
     * @param collection objects to retain
     * @return {@code true} if an object was removed, {@code false} otherwise.
     */

    final public boolean retainAll(Collection<?> collection) {
        boolean removed = false;
        for (int i = mSize - 1; i >= 0; i--) {
            if (!collection.contains(mArray[i])) {
                removeAt(i);
                removed = true;
            }
        }
        return removed;
    }

    /**
     * Removes all objects in the ArraySet that are also contained in collection.
     *
     * @param collection objects to remove
     * @return {@code true} if any object was removed, {@code false} otherwise.
     */

    final public boolean removeAll(Collection<?> collection) {
        boolean removed = false;
        for (Object value : collection) {
            removed |= remove(value);
        }

        return removed;
    }

    /**
     * Add all objects to ArraySet also contained in collection
     *
     * @param collection objects to add
     * @return {@code true} if any object was added, {@code false} otherwise.
     */

    final public boolean addAll(Collection<? extends E> collection) {
        ensureCapacity(mSize + collection.size());

        boolean added = false;
        for (E value : collection) {
            added |= addAndGetIndex(value) >= 0;
        }
        return added;
    }

    /**
     * @return the number of items in this set.
     */

    final public int size() {
        return mSize;
    }

    /**
     *
     * @return A new array equal to {@link #size()} with the same contents
     * and indexing as the backing array.  Empty space in the backing
     * array is not copied.
     */

    final public Object[] toArray() {
        Object[] result = new Object[mSize];

        System.arraycopy(mArray, 0, result, 0, mSize);
        return result;
    }

    /**
     * Copies Array contents into the array param.
     *
     * @param array  The Array to be filled, if array.length g.t {@link #size()} a new Array will
     *               be created equal to size.
     *
     * @return The contents of the backing array;
     */

    @SuppressWarnings("SuspiciousSystemArraycopy")
    final public <T> T[] toArray(T[] array) {
        if (array.length < mSize) {
            @SuppressWarnings("unchecked") T[] newArray
                    = (T[]) Array.newInstance(array.getClass().getComponentType(), mSize);
            array = newArray;
        }

        System.arraycopy(mArray, 0, array, 0, mSize);

        if (array.length > mSize) {
            array[mSize] = null;
        }

        return array;
    }

    /**
     * Return true if the array map contains no items.
     */
    final public boolean isEmpty() {
        return mSize <= 0;
    }


    /**
     * {@inheritDoc}
     *
     * <p>This implementation returns false if the object is not a set, or
     * if the sets have different sizes.  Otherwise, for each value in this
     * set, it checks to make sure the value also exists in the other set.
     * If any value doesn't exist, the method returns false; otherwise, it
     * returns true.
     */

    @Override
    final public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof Set) {
            Set<?> set = (Set<?>) object;
            if (size() != set.size()) {
                return false;
            }

            try {
                for (int i=0; i<mSize; i++) {
                    E mine = valueAt(i);
                    if (!set.contains(mine)) {
                        return false;
                    }
                }
            } catch (NullPointerException ignored) {
                return false;
            } catch (ClassCastException ignored) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Returns an iterator that supports the removal operation.  The iterator
     * returns items in the same order they appear in the backing array.
     */

    final public Iterator<E> iterator() {

        //The original version wrapped the set in a MapCollection to acquire an iterator.
        //Not sure why, but removing it also conveniently removes significant private
        //android library dependencies.

        return new Iterator<E>() {

            int index = -1;

            @Override
            public boolean hasNext() {
                return (index+1) < mSize;
            }

            @Override
            public E next(){
                return valueAt(++index);
            }

            @Override
            public void remove() {
                removeAt(index--);
            }
        };
    }


    /**
     * @return {@code true} if all set contains all collection elementts, {@code false} otherwise.
     */

    final public boolean containsAll(Collection<?> collection) {
        for (Object aCollection : collection) {
            if (!contains(aCollection)) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation composes a string by iterating over its values. If
     * this set contains itself as a value, the string "(this Set)"
     * will appear in its place.
     */

    @Override
    final public String toString() {
        if (isEmpty()) {
            return "{}";
        }

        StringBuilder buffer = new StringBuilder(mSize * 14);
        buffer.append('{');
        for (int i=0; i<mSize; i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            Object value = valueAt(i);
            if (value != this) {
                buffer.append(value);
            } else {
                buffer.append("(this Set)");
            }
        }
        buffer.append('}');
        return buffer.toString();
    }

    final public boolean add(E value){
        return addAndGetIndex(value) >= 0;
    }

    abstract public int addAndGetIndex(E value);
    abstract public void ensureCapacity(int minimumCapacity);
    abstract public int indexOf(Object key);
    abstract public E removeAt(int index);
    abstract void addAll(Set<? extends E> set);
}
