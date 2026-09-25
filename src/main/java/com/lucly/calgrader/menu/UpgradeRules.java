package com.lucly.calgrader.menu;

import java.util.ArrayList;
import java.util.List;

final class UpgradeRules {
    private UpgradeRules() {
    }

    static int chanceBasisPoints(int offerValue, int rewardValue) {
        if (offerValue <= 0 || rewardValue <= 0) {
            return 0;
        }
        long chance = (long) offerValue * 10_000L / rewardValue;
        return (int) Math.clamp(chance, 0L, 9_500L);
    }

    static List<Integer> rewardStackCounts(int amount, int maxStackSize) {
        List<Integer> counts = new ArrayList<>();
        if (amount <= 0 || maxStackSize <= 0) {
            return counts;
        }
        for (int remaining = amount; remaining > 0; ) {
            int count = Math.min(remaining, maxStackSize);
            counts.add(count);
            remaining -= count;
        }
        return counts;
    }
}
