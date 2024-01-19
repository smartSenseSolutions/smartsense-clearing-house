package com.smartsense.dummy.ch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class ClearingHouseApplication implements WebMvcConfigurer {

	public static void main(String[] args) {
		SpringApplication.run(ClearingHouseApplication.class, args);
	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
		messageConverters.add(new MappingJackson2HttpMessageConverter());
	}



	@Bean
	public RestTemplate getRestTemplate(){
		return new RestTemplate();
	}
}
