# ArraySet

## Summary
An ordered set largely derived from 'android.util.ArraySet' re-written as a POJO and can be ordered using a Comparator.  It requires Java 7+ or API 9+ in Android (due to Java Language requirements).

The Android implementation from which this is adapted indexed items by hashCode. This version offers more flexibility by taking an optional Comparator during instantiation allowing the user to specify their own indexing.

ArraySets instantiated with a Comparator are also more memory efficient; they eliminate the default internal caching of hashCodes, but at the cost of relying on their Comparator instead of cached data for order and search operations.

<a href="http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/5.1.1_r1/android/util/ArraySet.java#ArraySet">Original Documentation:</a>

ArraySet is a generic set data structure that is designed to be more memory efficient than a traditional HashSet. The design is very similar to ArrayMap, with all of the caveats described there. This implementation is separate from ArrayMap, however, so the Object array contains only one item for each entry in the set (instead of a pair for a mapping).

Note that this implementation is not intended to be appropriate for data structures that may contain large numbers of items. It is generally slower than a traditional HashSet, since lookups require a binary search and adds and removes require inserting and deleting entries in the array. For containers holding up to hundreds of items, the performance difference is not significant, less than 50%.

Because this container is intended to better balance memory use, unlike most other standard Java containers it will shrink its array as items are removed from it. Currently you have no control over this shrinking -- if you set a capacity and then remove an item, it may reduce the capacity to better match the current size. In the future an explicit call to set the capacity should turn off this aggressive shrinking behavior.


## LICENSE
-------

    Copyright (C) 2016 Shaun Halder

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.