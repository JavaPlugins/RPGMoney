package beer.devs.rpgmoney.utils;

import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UtilsTest
{
    @ParameterizedTest
    @CsvSource({"0, 7, false", "6, 7, false", "7, 7, true",
            "0, 3, false", "2, 3, false", "3, 3, true"})
    void onlyMatureCropsCanDropMoney(int age, int maximumAge, boolean mature)
    {
        Block block = mock(Block.class);
        Ageable crop = mock(Ageable.class);
        when(block.getBlockData()).thenReturn(crop);
        when(crop.getAge()).thenReturn(age);
        when(crop.getMaximumAge()).thenReturn(maximumAge);

        assertTrue(Utils.isGrowable(block));
        assertEquals(mature, Utils.isFullyGrown(block));
    }

    @Test
    void ordinaryBlocksRemainEligibleForLootAndPlacedBlockTracking()
    {
        Block block = mock(Block.class);
        when(block.getBlockData()).thenReturn(mock(BlockData.class));

        assertFalse(Utils.isGrowable(block));
        assertTrue(Utils.isFullyGrown(block));
    }
}
