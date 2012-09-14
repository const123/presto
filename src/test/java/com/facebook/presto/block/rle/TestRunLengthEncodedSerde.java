package com.facebook.presto.block.rle;

import com.facebook.presto.TupleInfo;
import com.facebook.presto.block.BlockStream;
import com.facebook.presto.block.Blocks;
import com.facebook.presto.block.uncompressed.UncompressedBlockStream;
import com.facebook.presto.slice.DynamicSliceOutput;
import com.facebook.presto.slice.SliceOutput;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.facebook.presto.TupleInfo.Type.VARIABLE_BINARY;

public class TestRunLengthEncodedSerde
{
    private SliceOutput sliceOutput;
    private RunLengthEncodedSerde rleSerde;

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception
    {
        sliceOutput = new DynamicSliceOutput(1024);
        rleSerde = new RunLengthEncodedSerde();
    }

    @Test
    public void testSanity() throws Exception
    {
        BlockStream blockStream = Blocks.createBlockStream(0, "a", "b", "b", "cde", "fuu", "a", "fuu");
        rleSerde.serialize(blockStream, sliceOutput);
        Blocks.assertBlockStreamEquals(blockStream, rleSerde.deserialize(sliceOutput.slice()));
    }

    @Test
    public void testAllSame() throws Exception
    {
        BlockStream blockStream = Blocks.createBlockStream(0, "a", "a", "a", "a", "a", "a", "a");
        rleSerde.serialize(blockStream, sliceOutput);
        Blocks.assertBlockStreamEquals(blockStream, rleSerde.deserialize(sliceOutput.slice()));
    }

    @Test
    public void testAllUnique() throws Exception
    {
        BlockStream blockStream = Blocks.createBlockStream(0, "a", "b", "c", "d", "e", "f", "g");
        rleSerde.serialize(blockStream, sliceOutput);
        Blocks.assertBlockStreamEquals(blockStream, rleSerde.deserialize(sliceOutput.slice()));
    }

    @Test
    public void testPositionGaps() throws Exception
    {
        BlockStream blockStream = new UncompressedBlockStream(
                new TupleInfo(VARIABLE_BINARY),
                Blocks.createBlock(1, "a", "a", "b", "a", "c"),
                Blocks.createBlock(6, "c", "a", "b", "b", "b"),
                Blocks.createBlock(100, "y", "y", "a", "y", "b"),
                Blocks.createBlock(200, "b")
        );
        rleSerde.serialize(blockStream, sliceOutput);
        Blocks.assertBlockStreamEquals(blockStream, rleSerde.deserialize(sliceOutput.slice()));
    }
}