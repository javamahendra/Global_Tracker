package com.nlw.global.api.advice;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import com.nlw.global.api.exception.NewsApiException;
import com.nlw.global.api.exception.WeatherException;

@ControllerAdvice
public class ExceptionMapper {

	@ExceptionHandler(WeatherException.class)
	public ModelAndView handelException(WeatherException exception) {
		ModelAndView mav = new ModelAndView("serviceError");
		return mav.addObject("errorMessage", exception.getMessage());
	}

	@ExceptionHandler(NewsApiException.class)
	public ModelAndView handleException(NewsApiException exception) {
		ModelAndView mav = new ModelAndView("serviceError");
		mav.addObject("errorMessage", exception.getMessage());
		return mav;
	}
}
