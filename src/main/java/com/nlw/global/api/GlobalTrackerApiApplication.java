package com.nlw.global.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
public class GlobalTrackerApiApplication extends WebMvcConfigurerAdapter {

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		
		registry.addViewController("/").setViewName("home");
		registry.addViewController("global/api/location/home").setViewName("locationMapHome");
		registry.addViewController("global/api/getAddress").setViewName("getAddress");
		registry.addViewController("global/api/getDuration").setViewName("getDuration");
		registry.addViewController("global/api/getLatLang").setViewName("getLatLang");
		registry.addViewController("global/api/viewSearchPlace").setViewName("searchPlace");
		registry.addViewController("global/api/weather/home").setViewName("forecast");
		registry.addViewController("global/api/contactUs").setViewName("contactUs");
		registry.addViewController("global/api/viewFeedback").setViewName("feedback");

	}

	public static void main(String[] args) {
		
		SpringApplication.run(GlobalTrackerApiApplication.class, args);
	}
}
