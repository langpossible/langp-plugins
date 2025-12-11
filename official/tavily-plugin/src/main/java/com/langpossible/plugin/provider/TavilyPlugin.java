package com.langpossible.plugin.provider;

import com.langpossible.core.plugin.tool.ToolContract;
import com.langpossible.core.plugin.tool.ToolInput;
import com.langpossible.core.plugin.tool.ToolOutput;
import com.langpossible.plugin.tools.TavilySearchTool;
import org.osgi.framework.BundleContext;

public class TavilyPlugin implements ToolContract {

    @Override
    public void start(BundleContext context) {
        context.registerService(ToolContract.class.getName(), this, null);
    }

    @Override
    public void stop(BundleContext context) {
    }

    @Override
    public String getName() {
        return "TavilyPlugin";
    }

    @Override
    public ToolOutput invoke(String action, ToolInput input) {
        switch (action) {
            case "tavily_search" -> {
                return new TavilySearchTool().execute(input);
            }
            case "tavily_extract" -> {
                return ToolOutput.builder().build();
            }
            default -> {
                throw new IllegalArgumentException("Unknown action: " + action);
            }
        }
    }

}
