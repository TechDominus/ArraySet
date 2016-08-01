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


import com.techdominus.comparators.CmpHash;
import com.techdominus.comparators.CmpHashNullSafe;
import com.techdominus.sets.ArraySet;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Shaun Halder on 8/3/2016.  All rights reserved.
 * shaun.halder@gmail.com
 */

@SuppressWarnings("WeakerAccess")
class TestCore {

    final static int ARRAYSET_DEFAULT = 0;
    final static int ARRAYSET_COMPARATOR_HASH = 1;
    final static int ARRAYSET_COMPARATOR_HASHNULLSAFE = 2;
    final static int ARRAYSET_SMALLCAP_DEFAULT = 3;
    final static int ARRAYSET_SMALLCAP_COMPARATOR = 4;
    final static int ARRAYSET_ZEROCAP_DEFAULT = 5;


    final static int NUM_ARRAYSET_SETUPS = 5;

    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    final static List<Integer> mNotNullSafeSetups = Arrays.asList(
            ARRAYSET_COMPARATOR_HASH
    );

    static <T> ArraySet<T> arrayset_setups(int i){

        switch(i){
            case ARRAYSET_DEFAULT:
                return ArraySet.newInstance();
            case ARRAYSET_COMPARATOR_HASH:
                return ArraySet.newInstance(CmpHash.instance);
            case ARRAYSET_COMPARATOR_HASHNULLSAFE:
                return ArraySet.newInstance(CmpHashNullSafe.instance);
            case ARRAYSET_SMALLCAP_DEFAULT:
                return ArraySet.newInstance(2);
            case ARRAYSET_SMALLCAP_COMPARATOR:
                return ArraySet.newInstance(2, CmpHashNullSafe.instance);
            case ARRAYSET_ZEROCAP_DEFAULT:
                return ArraySet.newInstance(0);
            default:
                throw new RuntimeException();
        }
    }
}
