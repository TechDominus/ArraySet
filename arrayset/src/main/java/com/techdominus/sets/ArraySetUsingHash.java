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
 *      - android.util.ArraySet (5.1.1_r1)
 */


package com.techdominus.sets;

import java.util.Arrays;
import java.util.Set;

final class ArraySetUsingHash<E> extends ArraySet<E> {

    private int[] mHashes;

    /**
     * Create a new empty ArraySet.  The default capacity of an array map is 0, and
     * will grow once items are added to it.
     */

    ArraySetUsingHash(){
        mHashes = EMPTYARRAY_INT;
        mArray = EMPTYARRAY_OBJECT;
    }

    /**
     * Create a new ArraySet with a given initial capacity.
     */

    ArraySetUsingHash(int capacity) {

        if (capacity == 0) {
            mHashes = EMPTYARRAY_INT;
            mArray = EMPTYARRAY_OBJECT;
        } else {
            //mHashes and mArray will be set by AllocArrays.
            allocArrays(capacity);
        }
    }

    /**
     * Returns the index of a value in the set.  If value not present, indexOf returns one less
     * than the would-be insertion point.  E.g. if would-be insertion is at 0, returns -1;
     *
     * @param key The value to search for.
     * @param hash The hashCode of the key.
     *
     * @return Returns the index of the value if it exists, else a negative integer.
     */


    private int indexOf(Object key, int hash) {

        final int N = mSize;

        // Important fast case: if nothing is in here, nothing to look for.
        if (N == 0) {
            return ~0;
        }

        int index = Arrays.binarySearch(mHashes, 0, N, hash);

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
        for (end = index + 1; end < N && mHashes[end] == hash; end++) {
            if (key.equals(mArray[end])) return end;
        }

        // Search for a matching key before the index.
        for (int i = index - 1; i >= 0 && mHashes[i] == hash; i--) {
            if (key.equals(mArray[i])) return i;
        }

        // Key not found -- return negative value indicating where a
        // new entry for this key should go.  We use the end of the
        // hash chain to reduce the number of array entries that will
        // need to be copied when inserting.
        return ~end;
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

        int index = Arrays.binarySearch(mHashes, 0, N, 0);

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
        for (end = index + 1; end < N && mHashes[end] == 0; end++) {
            if (null == mArray[end]) return end;
        }

        // Search for a matching key before the index.
        for (int i = index - 1; i >= 0 && mHashes[i] == 0; i--) {
            if (null == mArray[i]) return i;
        }

        // Key not found -- return negative value indicating where a
        // new entry for this key should go.  We use the end of the
        // hash chain to reduce the number of array entries that will
        // need to be copied when inserting.
        return ~end;
    }

    /**
     * Allocates arrays for mArray[] and mHash[] based on parameter.  Method first attempts to
     * allocate arrays from the cache pool, failing it creates a new Array instances.
     *
     * @param newSize If newSize is exactly BaseSize or BaseSize*2 method will attempt to
     *                allocate from the cache pool. Otherwise allocation will be new array.
     */

    private void allocArrays(final int newSize) {

        switch(newSize){
            default:
                mHashes = new int[newSize];
                mArray = new Object[newSize];
                break;
            case BASE_SIZE:
                synchronized (ArraySet.class) {

                    Object[] oArr = mObjCache.peekFirst();
                    int[] iArr = mIntCache.peekFirst();

                    mArray = oArr != null && oArr.length == newSize ?
                            mObjCache.pollFirst() : new Object[newSize];

                    mHashes= iArr != null && iArr.length == newSize ?
                            mIntCache.pollFirst() : new int[newSize];
                }
                break;
            case BASE_SIZE * 2:
                synchronized (ArraySet.class) {

                    Object[] oArr = mObjCache.pollLast();
                    int[] intArr = mIntCache.pollLast();

                    mArray = oArr != null && oArr.length == newSize ?
                            mObjCache.pollLast() : new Object[newSize];

                    mHashes= intArr != null && intArr.length == newSize ?
                            mIntCache.pollLast() : new int[newSize];

                }
                break;
        }
    }


    /**
     * Returns arrays to the cache pool if they match size criteria.
     */

    private void freeArrays(final int[] hashes, final Object[] array, final int size) {

        switch(hashes.length){
            default:
                break;
            case BASE_SIZE:

                //Null to avoid mem leak by holding reference in cache
                Arrays.fill(array, 0, size, null);

                synchronized (ArraySet.class){
                    mObjCache.addFirst(array);
                    mIntCache.addFirst(hashes);
                }
                break;
            case BASE_SIZE * 2:

                //Null to avoid mem leak by holding reference in cache
                Arrays.fill(array, 0, size, null);

                synchronized (ArraySet.class){
                    mObjCache.addLast(array);
                    mIntCache.addLast(hashes);
                }
                break;
        }
    }

    /**
     * Make the array map empty.  All storage is released.
     */
    public void clear() {
        if (mSize != 0) {
            freeArrays(mHashes, mArray, mSize);
            mHashes = EMPTYARRAY_INT;
            mArray = EMPTYARRAY_OBJECT;
            mSize = 0;
        }
    }

    /**
     * Ensure the array map can hold at least <var>minimumCapacity</var>
     * items.
     */
    public void ensureCapacity(int minimumCapacity) {
        if (mHashes.length < minimumCapacity) {
            final int[] ohashes = mHashes;
            final Object[] oarray = mArray;

            allocArrays(minimumCapacity);

            if (mSize > 0) {
                System.arraycopy(ohashes, 0, mHashes, 0, mSize);
                System.arraycopy(oarray, 0, mArray, 0, mSize);
            }
            freeArrays(ohashes, oarray, mSize);
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
        return key == null ? indexOfNull() : indexOf(key, key.hashCode());
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
        //A negative value returned from indexOf / indexOfNull is indicative of insertion point,
        //see descriptions.

        if (value == null) {
            hash = 0;
            index = ~indexOfNull();
        } else {
            hash = value.hashCode();
            index = ~indexOf(value, hash);
        }

        //Any X where X is >= 0 will be negative when ~X
        //Any X where X is < 0 will be positive when ~X

        if(index < 0)  //A negative index value here implies value already exists in set.
            return index;

        //If condition, can't add value until array resized.
        if (mSize >= mHashes.length) {

            //NewSize equals next larger BaseSize, if size greater than BaseSize*2, grow
            //array at approximately 1.5x current size

            //Note: mSize>>1 is approx .5 mSize
            final int n = mSize >= (BASE_SIZE*2) ? (mSize+(mSize>>1))
                    : (mSize >= BASE_SIZE ? (BASE_SIZE*2) : BASE_SIZE);


            final int[] ohashes = mHashes;
            final Object[] oarray = mArray;

            allocArrays(n);

            if (mHashes.length > 0) {
                System.arraycopy(ohashes, 0, mHashes, 0, ohashes.length);
                System.arraycopy(oarray, 0, mArray, 0, oarray.length);
            }

            freeArrays(ohashes, oarray, mSize);
        }

        if (index < mSize) {
            //if (DEBUG) Log.d(TAG, "add: move " + index + "-" + (mSize-index)
            //        + " to " + (index+1));
            System.arraycopy(mHashes, index, mHashes, index + 1, mSize - index);
            System.arraycopy(mArray, index, mArray, index + 1, mSize - index);
        }

        mHashes[index] = hash;
        mArray[index] = value;
        mSize++;
        return index;
    }



    /**
     * Perform a {@link #/add(Object)} of all values in <var>array</var>
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
     * Remove the key/value mapping at the given index.
     * @param index The desired index, must be between 0 and {@link #size()}-1.
     * @return Returns the value that was stored at this index.
     */

    @SuppressWarnings("unchecked")
    public E removeAt(int index) {
        final Object old = mArray[index];
        if (mSize <= 1) {
            // Now empty.
            //if (DEBUG) Log.d(TAG, "remove: shrink from " + mHashes.length + " to 0");
            freeArrays(mHashes, mArray, mSize);
            mHashes = EMPTYARRAY_INT;
            mArray = EMPTYARRAY_OBJECT;
            mSize = 0;
        } else {
            if (mHashes.length > (BASE_SIZE*2) && mSize < mHashes.length/3) {
                // Shrunk enough to reduce size of arrays.  We don't allow it to
                // shrink smaller than (BASE_SIZE*2) to avoid flapping between
                // that and BASE_SIZE.
                final int n = mSize > (BASE_SIZE*2) ? (mSize + (mSize>>1)) : (BASE_SIZE*2);

                //if (DEBUG) Log.d(TAG, "remove: shrink from " + mHashes.length + " to " + n);

                final int[] ohashes = mHashes;
                final Object[] oarray = mArray;
                allocArrays(n);

                mSize--;
                if (index > 0) {
                    //if (DEBUG) Log.d(TAG, "remove: copy from 0-" + index + " to 0");
                    System.arraycopy(ohashes, 0, mHashes, 0, index);
                    System.arraycopy(oarray, 0, mArray, 0, index);
                }
                if (index < mSize) {
                    //if (DEBUG) Log.d(TAG, "remove: copy from " + (index+1) + "-" + mSize
                    //        + " to " + index);
                    System.arraycopy(ohashes, index + 1, mHashes, index, mSize - index);
                    System.arraycopy(oarray, index + 1, mArray, index, mSize - index);
                }
            } else {
                mSize--;
                if (index < mSize) {
                    //if (DEBUG) Log.d(TAG, "remove: move " + (index+1) + "-" + mSize
                    //        + " to " + index);
                    System.arraycopy(mHashes, index + 1, mHashes, index, mSize - index);
                    System.arraycopy(mArray, index + 1, mArray, index, mSize - index);
                }
                mArray[mSize] = null;
            }
        }
        return (E)old;
    }


    /**
     * {@inheritDoc}
     */

    @Override
    public int hashCode() {
        final int[] hashes = mHashes;
        int result = 0;
        for (int i = 0, s = mSize; i < s; i++) {
            result += hashes[i];
        }
        return result;
    }
}
