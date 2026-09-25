package com.lucly.calgrader.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class UpgradeRulesTest {
    @Test
    void chanceIsProportionalAndCappedAtNinetyFivePercent() {
        assertEquals(2_500, UpgradeRules.chanceBasisPoints(25, 100));
        assertEquals(9_500, UpgradeRules.chanceBasisPoints(100, 100));
        assertEquals(0, UpgradeRules.chanceBasisPoints(0, 100));
        assertEquals(0, UpgradeRules.chanceBasisPoints(100, 0));
    }

    @Test
    void allUnstackableRewardsArePreserved() {
        List<Integer> counts = UpgradeRules.rewardStackCounts(64, 1);
        assertEquals(64, counts.size());
        assertEquals(64, counts.stream().mapToInt(Integer::intValue).sum());
        assertEquals(List.of(64), UpgradeRules.rewardStackCounts(64, 64));
    }
}
