package com.nlw.global.api.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nlw.global.api.dto.ApiResponse;
import com.nlw.global.api.dto.DisplayLocation;
import com.nlw.global.api.dto.NewsResponse;
import com.nlw.global.api.dto.NewsSourceResponse;
import com.nlw.global.api.dto.Respone;
import com.nlw.global.api.dto.Source;
import com.nlw.global.api.dto.WeatherInfo;
import com.nlw.global.api.dto.WeatherResponse;
import com.nlw.global.api.exception.NewsApiException;
import com.nlw.global.api.exception.WeatherException;
import com.nlw.global.api.util.MailUtil;
import com.nlw.global.api.util.RuleUtil;

@Service
@PropertySource("classpath:application.properties")
public class GlobalTrackerService {
	
	@Autowired
	private RuleUtil ruleUtil;
	@Autowired
	private MailUtil mailUtil;
	@Value("${inlineImage}")
	String templateMailBodyImageVal;
	InputStreamSource imageSource = null;
	private NewsSourceResponse response;

	/* START WEATHER API */
	public WeatherResponse getCurrentWeather(String location) throws WeatherException {
		return ruleUtil.getWeatherInfo(location);
	}

	public String sendMail(String toMail, DisplayLocation userLocation, WeatherInfo weather) throws Exception {
		MultipartFile image = getImageContent();
		return mailUtil.sendEmail(toMail, userLocation.getCity() + " Weather Report", image, imageSource, userLocation,
				weather);
	}

	private MultipartFile getImageContent() throws Exception {
		
		InputStream imageIs = null;
		byte[] imageByteArray = null;
		MultipartFile multipartFile = null;
		imageIs = this.getClass().getClassLoader().getResourceAsStream("templates/" + templateMailBodyImageVal);
		imageByteArray = IOUtils.toByteArray(imageIs);
		multipartFile = new MockMultipartFile(imageIs.getClass().getName(), imageIs.getClass().getName(), "image/jpeg",imageByteArray);
		imageSource = new ByteArrayResource(imageByteArray);
		return multipartFile;
	}
	/* END WEATHER API */

	/* START NEWS API */
	public NewsResponse getNewsApiResponse(String source, String sortBy) throws NewsApiException {
		NewsResponse response = ruleUtil.getNews(source, sortBy);
		/*response.getResponse().stream().filter(obj -> obj.getDescription().isEmpty())
				.forEach(obj -> obj.setDescription("Content Not Available Click on Above Link"));*/
		return response;
	}

	@PostConstruct
	public NewsSourceResponse getSources() throws NewsApiException {
		if (response == null) {
			return response = ruleUtil.getNewsSources();
		}
		return this.response;
	}

	public List<ApiResponse> getNewsId() throws NewsApiException {
		response = getSources();
		List<ApiResponse> idList = new ArrayList<>();
		//response.getSources().stream().forEach(s -> idList.add(new ApiResponse(s.getId(), null)));
		return idList;
	}

	public Source getSourceById(String id) throws NewsApiException {
		response = getSources();
		Source source = (Source) response.getSources();//.stream().filter(s -> id.equalsIgnoreCase(s.getId())).findAny().get();
		return source;
	}

	public String sendEmail(String to, String subject, List<Respone> respones, String mediaSource, String sortBy)
			throws NewsApiException {
		try {
			return mailUtil.sendEmail(to, subject, respones, mediaSource, sortBy);
		} catch (Exception e) {
			throw new NewsApiException("Unable to send Notification ,Server might have issue please retry !!");
		}
	}
	
	/* END NEWS API */
}
