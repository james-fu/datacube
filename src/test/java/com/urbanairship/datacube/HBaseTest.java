/*
Copyright 2012 Urban Airship and Contributors
*/

package com.urbanairship.datacube;

import com.urbanairship.datacube.DbHarness.CommitType;
import com.urbanairship.datacube.dbharnesses.HBaseDbHarness;
import com.urbanairship.datacube.dbharnesses.HbaseDbHarnessConfiguration;
import com.urbanairship.datacube.idservices.HBaseIdService;
import com.urbanairship.datacube.idservices.MapIdService;
import com.urbanairship.datacube.ops.LongOp;
import org.apache.hadoop.hbase.client.HTablePool;
import org.junit.BeforeClass;
import org.junit.Test;

public class HBaseTest extends EmbeddedClusterTestAbstract {
    public static final byte[] CUBE_DATA_TABLE = "cube_data".getBytes();
    public static final byte[] IDSERVICE_LOOKUP_TABLE = "idservice_data".getBytes();
    public static final byte[] IDSERVICE_COUNTER_TABLE = "idservice_counter".getBytes();

    public static final byte[] CF = "c".getBytes();

    @BeforeClass
    public static void setupCluster() throws Exception {
        getTestUtil().createTable(CUBE_DATA_TABLE, CF);
        getTestUtil().createTable(IDSERVICE_COUNTER_TABLE, CF);
        getTestUtil().createTable(IDSERVICE_LOOKUP_TABLE, CF);
    }

    @Test
    public void hbaseForCubeDataTest() throws Exception {
        IdService idService = new MapIdService();

        HTablePool pool = new HTablePool(getTestUtil().getConfiguration(), Integer.MAX_VALUE);
        DbHarness<LongOp> hbaseDbHarness = new HBaseDbHarness<LongOp>(pool,
                "hbaseForCubeDataTest".getBytes(), CUBE_DATA_TABLE, CF, LongOp.DESERIALIZER, idService,
                CommitType.INCREMENT, "scope");

        DbHarnessTests.basicTest(hbaseDbHarness);
    }

    @Test
    public void largeBatchSizeTest() throws Exception {
        IdService idService = new MapIdService();

        HTablePool pool = new HTablePool(getTestUtil().getConfiguration(), Integer.MAX_VALUE);

        HbaseDbHarnessConfiguration config = HbaseDbHarnessConfiguration.newBuilder()
                .setBatchSize(10)
                .setUniqueCubeName("hbaseForCubeDataTest".getBytes())
                .setTableName(CUBE_DATA_TABLE)
                .setCf(CF)
                .setCommitType(CommitType.INCREMENT)
                .build();

        DbHarness<LongOp> hbaseDbHarness = new HBaseDbHarness<LongOp>(config, pool, LongOp.DESERIALIZER, idService, (results) -> null);

        DbHarnessTests.asyncBatchWritesTest(hbaseDbHarness, 30);
    }

    @Test
    public void hbaseForCubeDataTestMulti() throws Exception {
        IdService idService = new MapIdService();

        HTablePool pool = new HTablePool(getTestUtil().getConfiguration(), Integer.MAX_VALUE);
        DbHarness<LongOp> hbaseDbHarness = new HBaseDbHarness<LongOp>(pool,
                "hbaseForCubeDataTest".getBytes(), CUBE_DATA_TABLE, CF, LongOp.DESERIALIZER, idService,
                CommitType.INCREMENT, "scope");

        DbHarnessTests.multiGetTest(hbaseDbHarness);
    }


    @Test
    public void basicIdServiceTest() throws Exception {
        IdService idService = new HBaseIdService(getTestUtil().getConfiguration(),
                IDSERVICE_LOOKUP_TABLE, IDSERVICE_COUNTER_TABLE, CF,
                "basicIdServiceTest".getBytes());

        IdServiceTests.basicTest(idService);
    }

    @Test
    public void exhaustionIdServiceTest() throws Exception {
        IdService idService = new HBaseIdService(getTestUtil().getConfiguration(),
                IDSERVICE_LOOKUP_TABLE, IDSERVICE_COUNTER_TABLE, CF,
                "exhaustionIdServiceTest".getBytes());
        IdServiceTests.testExhaustion(idService, 1, 1);
    }
}
