
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
 *
 * Adapted From:
 *      - android.util.ArraySet (5.1.1_r1)
 */

import com.techdominus.sets.ArraySet;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;

public class MyTests {

    @Test
    public void testAddRemove() throws Exception {


        for(int i=0; i< TestCore.NUM_ARRAYSET_SETUPS; i++) {

            ArraySet<Integer> arraySet = TestCore.arrayset_setups(i);

            //Set is clean
            assert arraySet.size() == 0;

            //Does not add multiple of Object
            assert arraySet.add(5);
            assert !arraySet.add(5);
            assert arraySet.size() == 1;

            assert arraySet.add(6);
            assert arraySet.add(7);
            assert arraySet.add(8);
            assert arraySet.size() == 4;

            //Test remove item not in set fails, and doesn't affect arraySet
            assert !arraySet.remove(55);
            assert arraySet.size() ==4;

            //remove mixed up order
            assert arraySet.remove(7);
            assert arraySet.size() == 3;

            assert arraySet.remove(6);
            assert arraySet.size() == 2;

            assert arraySet.remove(5);
            assert arraySet.size() == 1;

            assert arraySet.remove(8);
            assert arraySet.size() == 0;
        }
    }

    @Test
    public void testAddAll() throws Exception {

        for(int i=0; i< TestCore.NUM_ARRAYSET_SETUPS ; i++){

            ArraySet<Integer> arraySet = TestCore.arrayset_setups(i);

            //Size 0 add, may use special arrayCopy functions
            List<Integer> integerList = Arrays.asList(1,2,3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13);
            assert arraySet.addAll(integerList);
            assert arraySet.size() == integerList.size();
            assert arraySet.containsAll(integerList);
            assert arraySet.removeAll(integerList);
            assert arraySet.size() == 0;

            //Add with Stuff already present
            arraySet.add(99);
            assert arraySet.addAll(integerList);
            assert arraySet.size() == integerList.size() + 1;
            assert arraySet.containsAll(integerList);
            assert arraySet.contains(99);
            assert arraySet.removeAll(integerList);
            assert arraySet.size() == 1;
        }
    }



    @Test
    public void testEqualHashcodeCase() throws Exception {

        //A test to see ArraySet works with different objects that still have the same hashcode.

        class FixedHash {

            @Override
            public int hashCode() {
                return 5;
            }
        }

        for(int i=0; i< TestCore.NUM_ARRAYSET_SETUPS ; i++){

            ArraySet<FixedHash> arraySet = TestCore.arrayset_setups(i);

            assert arraySet != null;
            assert arraySet.size() == 0;

            assert arraySet.add(new FixedHash());
            assert arraySet.add(new FixedHash());
            assert arraySet.add(new FixedHash());

            //Each item is a separate object, and should be added despite equal hashcode
            assert arraySet.size() == 3;
        }
   }


    @Test
    public void testNull() throws Exception {

        for(int i=0; i< TestCore.NUM_ARRAYSET_SETUPS ; i++){

            if(TestCore.mNotNullSafeSetups.contains(i))
                continue; //This setup is not meant to be nullsafe.

            ArraySet<Integer> arraySet = TestCore.arrayset_setups(i);

            assert arraySet != null;
            assert arraySet.size() == 0;

            assert arraySet.add(null);
            assert arraySet.size() == 1;

            //check if arraySet can find null
            assert arraySet.contains(null);

            //Null and 0 have same hashcode, check for false positive.
            assert !arraySet.contains(0);

            //Check if repeat attempt to add affects set.
            assert !arraySet.add(null);
            assert arraySet.size() == 1;

            //Add some junk, notably we added 0
            assert arraySet.addAll(Arrays.asList(0, 1, 2, 3, 4));

            //ArraySet can find both null and zero still.
            assert arraySet.contains(null);
            assert arraySet.contains(0);

            assert arraySet.remove(null);
            assert !arraySet.contains(null);
            assert arraySet.contains(0);
        }
    }
}