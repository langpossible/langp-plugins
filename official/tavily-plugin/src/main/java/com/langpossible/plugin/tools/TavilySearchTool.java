package com.langpossible.plugin.tools;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.langpossible.core.components.okhttp.OkHttpManager;
import com.langpossible.core.plugin.tool.ToolInput;
import com.langpossible.core.plugin.tool.ToolOutput;

import java.util.HashMap;
import java.util.Map;

public class TavilySearchTool {

    public ToolOutput execute(ToolInput input) {
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put("Authorization", "Bearer " + input.getCredentials().get("tavily_api_key"));
        headerParams.put("Content-Type", "application/json");
        Map<String, Object> bodyParams = this.buildBodyParams(input);
        String responseStr = OkHttpManager.getInstance().post("https://api.tavily.com/search", headerParams, JSONUtil.toJsonStr(bodyParams));
        if (responseStr == null) {
            return null;
        }
        JSONObject jo = JSONUtil.parseObj(responseStr);

        return ToolOutput.builder()
                .text(responseToText(input, jo))
                .json(jo)
                .build();
    }

    private String responseToText(ToolInput input, JSONObject jo) {
        StringBuilder output = new StringBuilder();

        // include_answer
        if (Boolean.TRUE.equals(input.getParameters().get("include_answer")) && jo.containsKey("answer")) {
            output.append("**Answer:** ").append(jo.getStr("answer")).append("\n\n");
        }

        // results
        if (jo.containsKey("results")) {
            JSONArray results = jo.getJSONArray("results");
            for (int i = 0; i < results.size(); i++) {
                JSONObject result = results.getJSONObject(i);
                String title = result.getStr("title", "No Title");
                String url = result.getStr("url", "");
                String content = result.getStr("content", "");
                String publishedDate = result.getStr("published_date", "");
                String score = result.getStr("score", "");

                output.append("# Result ").append(i + 1).append(": [")
                        .append(title).append("](").append(url).append(")\n");

                if ("news".equals(input.getParameters().get("topic")) && publishedDate != null && !publishedDate.isEmpty()) {
                    output.append("**Published Date:** ").append(publishedDate).append("\n");
                }

                output.append("**URL:** ").append(url).append("\n");

                if (score != null && !score.isEmpty()) {
                    output.append("**Relevance Score:** ").append(score).append("\n");
                }

                // favicon
                if (Boolean.TRUE.equals(input.getParameters().get("include_favicon")) && result.containsKey("favicon")) {
                    output.append("**Favicon:** ![Favicon for ")
                            .append(title).append("](").append(result.getStr("favicon")).append(")\n");
                }

                if (content != null && !content.isEmpty()) {
                    output.append("**Content:**\n").append(content).append("\n");
                }

                if (Boolean.TRUE.equals(input.getParameters().get("include_raw_content")) && result.containsKey("raw_content")) {
                    output.append("**Raw Content:**\n").append(result.getStr("raw_content")).append("\n");
                }

                output.append("---\n");
            }
        }

        // images
        if (Boolean.TRUE.equals(input.getParameters().get("include_images")) && jo.containsKey("images")) {
            output.append("**Images:**\n");
            JSONArray images = jo.getJSONArray("images");
            for (Object imageObj : images) {
                String imageUrl = null;
                String description = "Tavily search result image";

                if (imageObj instanceof JSONObject imageJson) {
                    imageUrl = imageJson.getStr("url");
                    description = imageJson.getStr("description", description);
                } else {
                    imageUrl = String.valueOf(imageObj);
                }

                if (imageUrl != null && !imageUrl.isEmpty()) {
                    output.append("![").append(description).append("](").append(imageUrl).append(")\n");
                }
            }
            output.append("\n");
        }

        return output.toString();
    }


    /**
     * 构建请求体参数
     * @param input 输入参数 {@link ToolInput}
     * @return 请求体参数 {@link Map}
     */
    private Map<String, Object> buildBodyParams(ToolInput input) {
        HashMap<String, Object> bodyParams = new HashMap<>();

        // 添加查询参数
        if (input.getParameters().get("query") != null) {
            bodyParams.put("query", input.getParameters().get("query"));
        }

        /* 添加其他可选参数 */
        if (input.getParameters().get("search_depth") != null) {
            bodyParams.put("search_depth", input.getParameters().get("search_depth"));
        } else if (input.getParameters().get("options") != null) {
            // 根据options参数设置search_depth
            bodyParams.put("search_depth", input.getParameters().get("options"));
        } else {
            bodyParams.put("search_depth", "basic");
        }
        if (input.getParameters().get("topic") != null) {
            bodyParams.put("topic", input.getParameters().get("topic"));
        } else {
            bodyParams.put("topic", "general");
        }
        if (input.getParameters().get("max_results") != null) {
            bodyParams.put("max_results", Integer.parseInt((String) input.getParameters().get("max_results")));
        } else {
            bodyParams.put("max_results", 5);
        }
        if (input.getParameters().get("auto_parameters") != null) {
            bodyParams.put("auto_parameters", Boolean.parseBoolean((String) input.getParameters().get("auto_parameters")));
        } else {
            bodyParams.put("auto_parameters", false);
        }
        if (input.getParameters().get("chunks_per_source") != null) {
            bodyParams.put("chunks_per_source", Integer.parseInt((String) input.getParameters().get("chunks_per_source")));
        } else {
            bodyParams.put("chunks_per_source", 3);
        }
        if (input.getParameters().get("time_range") != null) {
            bodyParams.put("time_range", input.getParameters().get("time_range"));
        }
        if (input.getParameters().get("start_date") != null) {
            bodyParams.put("start_date", input.getParameters().get("start_date"));
        }
        if (input.getParameters().get("end_date") != null) {
            bodyParams.put("end_date", input.getParameters().get("end_date"));
        }
        if (input.getParameters().get("include_answer") != null) {
            bodyParams.put("include_answer", Boolean.parseBoolean((String) input.getParameters().get("include_answer")));
        } else {
            bodyParams.put("include_answer", false);
        }
        if (input.getParameters().get("include_raw_content") != null) {
            bodyParams.put("include_raw_content", Boolean.parseBoolean((String) input.getParameters().get("include_raw_content")));
        } else {
            bodyParams.put("include_raw_content", false);
        }
        if (input.getParameters().get("include_images") != null) {
            bodyParams.put("include_images", Boolean.parseBoolean((String) input.getParameters().get("include_images")));
        } else {
            bodyParams.put("include_images", false);
        }
        if (input.getParameters().get("include_image_descriptions") != null) {
            bodyParams.put("include_image_descriptions", Boolean.parseBoolean((String) input.getParameters().get("include_image_descriptions")));
        } else {
            bodyParams.put("include_image_descriptions", false);
        }
        if (input.getParameters().get("include_favicon") != null) {
            bodyParams.put("include_favicon", Boolean.parseBoolean((String) input.getParameters().get("include_favicon")));
        } else {
            bodyParams.put("include_favicon", false);
        }
        if (input.getParameters().get("country") != null) {
            bodyParams.put("country", input.getParameters().get("country"));
        }
        return bodyParams;
    }

}
