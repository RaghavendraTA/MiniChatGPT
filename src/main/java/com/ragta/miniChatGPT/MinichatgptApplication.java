package com.ragta.miniChatGPT;

import com.ragta.miniChatGPT.configurations.OllamaConfig;
import com.ragta.miniChatGPT.configurations.OpenAIConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({OllamaConfig.class, OpenAIConfig.class})
@SpringBootApplication
public class MinichatgptApplication {

	public static void main(String[] args) {
		SpringApplication.run(MinichatgptApplication.class, args);
	}

}
