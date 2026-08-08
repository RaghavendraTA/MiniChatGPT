package com.ragta.miniChatGPT;

import com.ragta.miniChatGPT.configurations.OllamaConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(OllamaConfig.class)
@SpringBootApplication
public class MinichatgptApplication {

	public static void main(String[] args) {
		SpringApplication.run(MinichatgptApplication.class, args);
	}

}
