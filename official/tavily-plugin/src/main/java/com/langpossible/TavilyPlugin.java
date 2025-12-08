package com.langpossible.plugin;

import com.langpossible.core.PluginInput;
import com.langpossible.core.PluginOutput;

public class TavilyPlugin {

    public PluginOutput demo(PluginInput input) {
        return PluginOutput.builder()
                .text("hello world")
                .build();
    }

}