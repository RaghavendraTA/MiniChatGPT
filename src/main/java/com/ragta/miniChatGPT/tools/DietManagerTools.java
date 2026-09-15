package com.ragta.miniChatGPT.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

public class DietManagerTools {

    @Tool("Helps the user to plan daily diet")
    String getDietPlan(@P("user query contains daily details for diet plan") String userQuery) {
        return "Do nothing";
    }
}
