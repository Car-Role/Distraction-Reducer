package com.distractionreducer;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;
import com.distractionreducer.DistractionReducerPlugin;

public class DistractionReducerTest
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(DistractionReducerPlugin.class);
        RuneLite.main(args);
    }
}