/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.prestosql.operator.index;

import com.facebook.presto.block.BlockEncodingManager;
import com.facebook.presto.metadata.FunctionRegistry;
import com.facebook.presto.spi.InMemoryRecordSet;
import com.facebook.presto.spi.RecordCursor;
import com.facebook.presto.spi.type.ArrayType;
import com.facebook.presto.spi.type.TypeManager;
import com.facebook.presto.sql.analyzer.FeaturesConfig;
import com.facebook.presto.type.TypeRegistry;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.testng.annotations.Test;

import static com.facebook.presto.spi.type.BigintType.BIGINT;
import static com.facebook.presto.spi.type.DateTimeEncoding.packDateTimeWithZone;
import static com.facebook.presto.spi.type.TimeZoneKey.getTimeZoneKeyForOffset;
import static com.facebook.presto.spi.type.TimestampWithTimeZoneType.TIMESTAMP_WITH_TIME_ZONE;
import static com.facebook.presto.util.StructuralTestUtil.arrayBlockOf;
import static org.testng.Assert.assertTrue;

public class TestFieldSetFilteringRecordSet
{
    @Test
    public void test()
    {
        TypeManager typeManager = new TypeRegistry();
        FunctionRegistry functionRegistry = new FunctionRegistry(typeManager, new BlockEncodingManager(typeManager), new FeaturesConfig());

        ArrayType arrayOfBigintType = new ArrayType(BIGINT);
        FieldSetFilteringRecordSet fieldSetFilteringRecordSet = new FieldSetFilteringRecordSet(
                functionRegistry,
                new InMemoryRecordSet(
                        ImmutableList.of(BIGINT, BIGINT, TIMESTAMP_WITH_TIME_ZONE, TIMESTAMP_WITH_TIME_ZONE, arrayOfBigintType, arrayOfBigintType),
                        ImmutableList.of(
                                ImmutableList.of(
                                        100L,
                                        100L,
                                        // test same time in different time zone to make sure equal check was done properly
                                        packDateTimeWithZone(100, getTimeZoneKeyForOffset(123)),
                                        packDateTimeWithZone(100, getTimeZoneKeyForOffset(234)),
                                        // test structural type
                                        arrayBlockOf(BIGINT, 12, 34, 56),
                                        arrayBlockOf(BIGINT, 12, 34, 56)))),
                ImmutableList.of(ImmutableSet.of(0, 1), ImmutableSet.of(2, 3), ImmutableSet.of(4, 5)));
        RecordCursor recordCursor = fieldSetFilteringRecordSet.cursor();
        assertTrue(recordCursor.advanceNextPosition());
    }
}