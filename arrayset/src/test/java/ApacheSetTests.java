/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Adapted From
 * - org.apache.pivot.collections.test.HashSetTest
 *
 */

import com.techdominus.sets.ArraySet;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.Iterator;
import static org.junit.Assert.assertEquals;

public class ApacheSetTests {

    @Test
    public void basicTest() {

        for(int i = 0; i< TestCore.NUM_ARRAYSET_SETUPS; i++) {

            ArraySet<String> set = TestCore.arrayset_setups(i);

            set.add("A");
            assert set.contains("A");

            set.add("B");
            assertTrue(set.contains("A"));
            assertTrue(set.contains("B"));

            assertEquals(set.size(), 2);

            int j = 0;
            for (String element : set) {
                assertTrue(element.equals("A") || element.equals("B"));
                j++;
            }

            assertEquals(j, 2);

            set.remove("B");
            assertFalse(set.contains("B"));

            set.remove("A");
            assertFalse(set.contains("A"));

            assertTrue(set.isEmpty());

            set.add("A");
            set.add("B");
            set.add("C");

            Iterator<String> iter = set.iterator();
            int count = 0;
            while (iter.hasNext()) {
                String s = iter.next();
                if (!set.contains(s)) {
                    fail("Unknown element in set " + s);
                }
                count++;
            }
            assertEquals(3, count);

            iter = set.iterator();
            while (iter.hasNext()) {
                iter.next();
                iter.remove();
            }
            assertEquals(0, set.size());
        }
    }

    @Test
    public void equalsTest() {

        for(int i=0; i< TestCore.NUM_ARRAYSET_SETUPS; i++) {

            ArraySet<String> set1 = TestCore.arrayset_setups(i);
            set1.add("one");
            set1.add("two");
            set1.add("three");

            ArraySet<String> set2 = TestCore.arrayset_setups(i);
            set2.add("one");
            set2.add("two");
            set2.add("three");

            // Same
            assertTrue(set1.equals(set2));

            // Different values
            set2.remove("three");
            set2.add("four");
            assertFalse(set1.equals(set2));

            // Different lengths
            set2.add("three");
            assertFalse(set1.equals(set2));
        }
   }
}