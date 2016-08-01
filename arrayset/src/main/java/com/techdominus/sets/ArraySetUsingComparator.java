/*
 * Modifications Copyright (C) 2016 Shaun Halder
 * Copyright (C) 2010, 2013 The Android Open Source Project
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
 */

package com.techdominus.sets;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

@SuppressWarnings({"unused", "WhileLoopReplaceableByForEach", "unchecked"})
final class ArraySetUsingComparator<E> extends ArraySet<E> {

    private Comparator mComparator;

    /**
     * Create a new empty ArraySet.  The default capacity of an array map is 0, and
     * will grow once items are added to it.
     */
    ArraySetUsingComparator(Comparator<? super E> comparator) {
        mArray = EMPTYARRAY_OBJECT;
        mSize = 0;

        mComparator = comparator;
    }

    /**
     * Create a new ArraySet with a given initial capacity.
     */
    ArraySetUsingComparator(int capacity, Comparator<? super E> comparator) {
        if (capacity == 0) {
            mArray = EMPTYARRAY_OBJECT;
        } else {
            allocArrays(capacity);
        }

        mSize = 0;
        mComparator = comparator;
    }

    /**
     * Allocates arrays for mArray[] based on parameter.  Method first attempts to
     * allocate arrays from the cache pool, failing it creates a new Array instances.
     *
     * @param newSize If newSize is exactly BaseSize or BaseSize*2 method will attempt to
     *                allocate from the cache pool. Otherwise allocation will be new array.
     */

    private void allocArrays(final int newSize) {

        switch(newSize){
            default:
                mArray = new Object[newSize];
                break;
            case BASE_SIZE:
                synchronized (ArraySet.class) {

                    Object[] oArr = mObjCache.peekFirst();

                    mArray = oArr != null && oArr.length == newSize ?
                            mObjCache.pollFirst() : new Object[newSize];
                }
                break;
            case BASE_SIZE * 2:
                synchronized (ArraySet.class) {

                    Object[] oArr = mObjCache.peekLast();

                    mArray = oArr != null && oArr.length == newSize ?
                            mObjCache.pollLast() : new Object[newSize];
                }
                break;
        }
    }

    /**
     * Returns arrays to the cache pool if they match size criteria.
     */

    private void freeArrays(final Object[] array, final int size) {

        switch(array.length){
            default:
                break;
            case BASE_SIZE:

                //Null to avoid mem leak by holding reference in cache
                Arrays.fill(array, 0, size, null);

                synchronized (ArraySet.class){
                    mObjCache.addFirst(array);
                }
                break;
            case BASE_SIZE * 2:

                //Null to avoid mem leak by holding reference in cache
                Arrays.fill(array, 0, size, null);

                synchronized (ArraySet.class){
                    mObjCache.addLast(array);
                }
                break;
        }
    }


    /**
     * All contents and the backing array itself are released.
     */

    public void clear() {
        if (mSize != 0) {
            freeArrays(mArray, mSize);
            mArray = EMPTYARRAY_OBJECT;
            mSize = 0;
        }
    }

    /**
     * Ensure the array map can hold at least <var>minimumCapacity</var>
     * items.
     */
    public void ensureCapacity(int minimumCapacity) {
        if (mArray.length < minimumCapacity) {

            final Object[] oArray = mArray;
            allocArrays(minimumCapacity);

            if (mSize > 0) {
                System.arraycopy(oArray, 0, mArray, 0, mSize);
            }
            freeArrays(oArray, mSize);
        }
    }


    /**
     * Returns the index of a value in the set.  If value not present, indexOf returns one less
     * than the would-be insertion point.  E.g. if would-be insertion is at 0, returns -1;
     *
     * @param key The value to search for.
     * @return Returns the index of the value if it exists, else a negative integer.
     */

    public int indexOf(Object key) {

        if(key == null)
            return indexOfNull();

        final int N = mSize;

        // Important fast case: if nothing is in here, nothing to look for.
        if (N == 0) {
            return ~0;   //return ~0 == -1
        }

        int index = Arrays.binarySearch(mArray, 0, N, key, mComparator);

        // If the hash code wasn't found, then we have no entry for this key.
        if (index < 0) {
            return index;
        }

        // If the key at the returned index matches, that's what we want.
        if (key.equals(mArray[index])) {
            return index;
        }

        // Search for a matching key after the index.
        int end;


        for (end = index + 1; end < N && mComparator.compare(mArray[end], key) == 0; end++) {
            if (key.equals(mArray[end])) return end;
        }

        // Search for a matching key before the index.
        for (int i = index - 1; i >= 0 && mComparator.compare(mArray[i], key) == 0; i--) {
            if (key.equals(mArray[i])) return i;
        }

        // Key not found -- return negative value indicating where a
        // new entry for this key should go.  We use the end of the
        // hash chain to reduce the number of array entries that will
        // need to be copied when inserting.
        return ~end;
    }


    /**
     * Adds the value to the set, and returns the index where the item was inserted.
     * If the item could not be added, a negative number is returned.
     *
     * @return Returns the index of the value inserted, else a negative integer.
     */

    public int addAndGetIndex(E value){

        final int hash;
        int index;

        //Bitwise unary not ~ --> Adds 1 and then flips sign, e.g. -1 --> 0, 0 --> -1;
        //A negative value returned from indexOf is indicative of insertion point, see description.

        index = ~indexOf(value);

        //Any X where X is >= 0 will be negative when ~X
        //Any X where X is < 0 will be positive when ~X

        if(index < 0)  //A negative index value here implies value already exists in set.
            return index;


        if (mSize >= mArray.length) {

            //NewSize equals next larger BaseSize, if size greater than BaseSize*2, grow
            //array at approximately 1.5x current size

            //Note: mSize>>1 is approx .5 mSize
            final int n = mSize >= (BASE_SIZE*2) ? (mSize+(mSize>>1))
                    : (mSize >= BASE_SIZE ? (BASE_SIZE*2) : BASE_SIZE);


            final Object[] oarray = mArray;
            allocArrays(n);

            if (mArray.length > 0)
                System.arraycopy(oarray, 0, mArray, 0, oarray.length);

            freeArrays(oarray, mSize);
        }

        if (index < mSize) {
            System.arraycopy(mArray, index, mArray, index + 1, mSize - index);
        }

        mArray[index] = value;
        mSize++;

        return index;
    }


    /**
     * Rewrite of indexOf for finding null values
     *
     * @return if >= 0, index of null, negative otherwise.
     */

    private int indexOfNull() {
        final int N = mSize;

        // Important fast case: if nothing is in here, nothing to look for.
        if (N == 0) {
            return ~0;
        }

        int index = Arrays.binarySearch(mArray, 0, N, null, mComparator);

        // If the hash code wasn't found, then we have no entry for this key.
        if (index < 0) {
            return index;
        }

        // If the key at the returned index matches, that's what we want.
        if (null == mArray[index]) {
            return index;
        }

        // Search for a matching key after the index.
        int end;
        for (end = index + 1; end < N && mComparator.compare(mArray[end], null) == 0; end++) {
            if (null == mArray[end]) return end;
        }

        // Search for a matching key before the index.
        for (int i = index - 1; i >= 0 && mComparator.compare(mArray[i], null) == 0; i--) {
            if (null == mArray[i]) return i;
        }

        // Key not found -- return negative value indicating where a
        // new entry for this key should go.  We use the end of the
        // hash chain to reduce the number of array entries that will
        // need to be copied when inserting.
        return ~end;
    }



    /**
     * Perform a {@link #add(Object)} of all values in <var>set</var>
     * @param set The array whose contents are to be retrieved.
     */

    public void addAll(Set<? extends E> set) {

        final int numToAdd = set.size();
        if(numToAdd == 0) return;

        ensureCapacity(mSize + numToAdd);

        for(E value : set){
            addAndGetIndex(value);
        }
    }


    /**
     * Remove the value at the given index.
     *
     * @param index The desired index, must be between 0 and {@link #size()}-1.
     * @return Returns the value that was stored at this index.
     */

    public E removeAt(int index) {
        final Object old = mArray[index];
        if (mSize <= 1) {
            // Now empty.
            //if (DEBUG) Log.d(TAG, "remove: shrink from " + mArray.length + " to 0");
            freeArrays(mArray, mSize);

            mArray = EMPTYARRAY_OBJECT;
            mSize = 0;
        } else {
            if (mArray.length > (BASE_SIZE*2) && mSize < mArray.length/3) {
                // Shrunk enough to reduce size of arrays.  We don't allow it to
                // shrink smaller than (BASE_SIZE*2) to avoid flapping between
                // that and BASE_SIZE.
                final int n = mSize > (BASE_SIZE*2) ? (mSize + (mSize>>1)) : (BASE_SIZE*2);

                //if (DEBUG) Log.d(TAG, "remove: shrink from " + mArray.length + " to " + n);

                final Object[] oarray = mArray;
                allocArrays(n);

                mSize--;
                if (index > 0) {
                    //if (DEBUG) Log.d(TAG, "remove: copy from 0-" + index + " to 0");
                    System.arraycopy(oarray, 0, mArray, 0, index);
                }
                if (index < mSize) {
                    //if (DEBUG) Log.d(TAG, "remove: copy from " + (index+1) + "-" + mSize
                    //      + " to " + index);
                    System.arraycopy(oarray, index + 1, mArray, index, mSize - index);
                }
            } else {
                mSize--;
                if (index < mSize) {
                    //if (DEBUG) Log.d(TAG, "remove: move " + (index+1) + "-" + mSize
                    //      + " to " + index);
                    System.arraycopy(mArray, index + 1, mArray, index, mSize - index);
                }
                mArray[mSize] = null;
            }
        }
        return (E)old;
    }
}
