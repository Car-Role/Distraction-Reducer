package com.distractionreducer;

import java.lang.reflect.Field;
import java.util.Set;
import net.runelite.api.AnimationID;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class MiningAnimationTest
{
    @Test
    public void includesTrailblazerDragonPickaxeAnimation() throws Exception
    {
        Field miningAnimationsField = DistractionReducerPlugin.class.getDeclaredField("MINING_ANIMATION_IDS");
        miningAnimationsField.setAccessible(true);

        @SuppressWarnings("unchecked")
        Set<Integer> miningAnimations = (Set<Integer>) miningAnimationsField.get(null);

        assertTrue(
                "Dragon pickaxe (or) with the Trailblazer ornament must trigger mining detection",
                miningAnimations.contains(AnimationID.MINING_DRAGON_PICKAXE_OR_TRAILBLAZER)
        );
    }
}
