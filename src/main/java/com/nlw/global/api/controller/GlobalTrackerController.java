package com.nlw.global.api.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlw.global.api.constant.Constants;
import com.nlw.global.api.dto.ApiResponse;
import com.nlw.global.api.dto.CurrentObservation;
import com.nlw.global.api.dto.DisplayLocation;
import com.nlw.global.api.dto.DurationTimeWrapper;
import com.nlw.global.api.dto.Location;
import com.nlw.global.api.dto.NewsResponse;
import com.nlw.global.api.dto.Respone;
import com.nlw.global.api.dto.Source;
import com.nlw.global.api.dto.WeatherInfo;
import com.nlw.global.api.dto.WeatherResponse;
import com.nlw.global.api.exception.NewsApiException;
import com.nlw.global.api.exception.WeatherException;
import com.nlw.global.api.search.dto.NearestSearchResponse;
import com.nlw.global.api.service.GlobalTrackerService;
import com.nlw.global.api.util.MailUtil;
import com.nlw.global.api.util.PdfGenaratorUtil;
import com.nlw.global.api.util.RuleUtil;
import com.nlw.global.api.util.ServiceUtils;

@Controller
@RequestMapping(value = "global/api")
public class GlobalTrackerController {
	@Autowired
	private RuleUtil ruleUtil;
	@Autowired
	private MailUtil mailUtil;
	@Autowired
	private PdfGenaratorUtil util;

	@Autowired
	private GlobalTrackerService service;

	DisplayLocation userLocation = null;
	WeatherInfo weatherInfo = null;
	CurrentObservation observation = null;

	private Logger logger = LoggerFactory.getLogger(GlobalTrackerController.class);

	private static List<NearestSearchResponse> searchResponses = null;

	private Map<String, String> mediaMap = new HashMap<>();
	List<ApiResponse> mediaIds = null;
	List<Respone> currentResponse = null;
	String mediaSource = null;
	String newsId = null;
	private String sortByNews = "";

	/* START LOCATION API */
	@RequestMapping(value = "/getLocation")
	public String getAddress(@RequestParam("lattitude") double lattitude, @RequestParam("longitude") double longitude,
			Model model) {
		logger.debug("getAddress() method called with lattitude {} and longitude {}", lattitude, longitude);
		String address = "";
		try {
			address = ruleUtil.getAddressDetails(lattitude, longitude);
			logger.info("Response in Controller for getAddress API: {}",
					new ObjectMapper().writeValueAsString(address));
			model.addAttribute("address", address);
		} catch (Exception e) {
			model.addAttribute("errorMessage", Constants.ERROR_MESSAGE);
			logger.error("Error in Controller : {}", e.getMessage());
		}
		return "getAddress";
	}

	@RequestMapping(value = "/getDistanceTime")
	public String getDistanceTime(String source, String destination, Model model) {
		DurationTimeWrapper response = null;
		logger.debug("getDistanceTime() method called with source {} and destination {}", source, destination);
		try {
			response = ruleUtil.getDuration(source, destination);
			logger.info("Response in Controller for getDistanceTime API: {}",
					new ObjectMapper().writeValueAsString(response));
			model.addAttribute("response", response);
		} catch (Exception e) {
			model.addAttribute("errorMessage", Constants.ERROR_MESSAGE);
			logger.error("Error in Controller : {}", e.getMessage());
		}
		return "getDuration";

	}

	@RequestMapping(value = "/getLatitudeLongitude")
	public String getLatitudeLongitude(String address, Model model) {
		Location location = null;
		logger.debug("getLatitudeLongitude() method called with address {} ", address);
		try {
			location = ruleUtil.getLatitudeLongitude(address);
			logger.info("Response in Controller for getLatitudeLongitude API: {}",
					new ObjectMapper().writeValueAsString(location));
			model.addAttribute("location", location);
		} catch (Exception e) {
			model.addAttribute("errorMessage", Constants.ERROR_MESSAGE);
			logger.error("Error in Controller : {}", e.getMessage());
		}
		return "getLatLang";
	}

	@RequestMapping(value = "/searchNearestPlace")
	public String getNearestPlace(@RequestParam("searchType") String searchType,
			@RequestParam("location") String location, Model model) {
		int matchesCount = 0;
		logger.debug("getNearestPlace() method called with searchType {}  and location {} ", searchType, location);
		try {
			searchResponses = ruleUtil.getNearestPlace(searchType, location);
			matchesCount = searchResponses.size();
			logger.info("Response in Controller for getNearestPlace API: {}",
					new ObjectMapper().writeValueAsString(searchResponses));
			model.addAttribute("matchesCount", matchesCount);
			model.addAttribute("searchResponses", searchResponses);
			model.addAttribute("searchType", searchType);
		} catch (Exception e) {
			model.addAttribute("errorMessage", Constants.ERROR_MESSAGE);
			logger.error("Error in Controller : {}", e.getMessage());
		}
		return "searchResault";

	}

	@RequestMapping("/generatePDF")
	public String generatePDF(Model model) {
		int matchesCount = 0;
		Map<Object, Object> data = new HashMap<>();
		data.put("searchResponses", searchResponses);
		try {
			matchesCount = searchResponses.size();
			model.addAttribute("matchesCount", matchesCount);
			util.createPdf("report", data);
			model.addAttribute("message", "Downloaded successfully..");
		} catch (Exception e) {
			model.addAttribute("errorMessage", Constants.ERROR_MESSAGE);
			logger.error(e.getLocalizedMessage());
		}
		return "searchResault";
	}

	@RequestMapping("/generateGraph")
	public String generateGraph(Model model) {
		Map<String, Integer> rateMap = ServiceUtils.mapGraphData(searchResponses);
		model.addAttribute("rateMap", rateMap);
		model.addAttribute("searchType", searchResponses.get(0).getTypes());
		return "searchRatingGraph";
	}

	@RequestMapping("/sendEmail")
	public String sendEmail(@RequestParam("email") String email, Model model) throws Exception {
		List<String> msg = new ArrayList<>();
		String subject = searchResponses.get(0).getTypes();
		String mailStatus = mailUtil.sendEmail(email, subject + " info", searchResponses);
		msg.add("Dummy list to hide popup");
		model.addAttribute("msg", msg);
		model.addAttribute("mailStatus", mailStatus);
		return "searchPlace";
	}
	/* END LOCATION API */

	/* START WEATHER API */
	@RequestMapping(value = "/getWeather")
	public String getWeatherStatus(@RequestParam("location") String location, Model model) throws WeatherException {
		WeatherResponse response = null;
		response = service.getCurrentWeather(location);
		if (response.getCurrentObservation() == null) {
			throw new WeatherException(" There is some issue for your search Location " + "( " + location + " )"
					+ " Please Retry again !!");
		}
		List<DisplayLocation> locations = new ArrayList<>();
		DisplayLocation displayLocation = response.getCurrentObservation().getDisplayLocation();
		userLocation = displayLocation;
		locations.add(displayLocation);
		model.addAttribute("locations", locations);
		model.addAttribute("responses", ServiceUtils.getTableHeaders());
		model.addAttribute("weathers", mapWeatherInfo(response));
		model.addAttribute("image", "/images/growth-small.jpg");
		return "forecast";
	}

	@RequestMapping("/sendWeatherEmail")
	public String notification(@RequestParam("email") String email, Model model) throws WeatherException {
		String message = "";
		List<String> msg = new ArrayList<>();
		try {
			message = service.sendMail(email, userLocation, weatherInfo);
			msg.add("Dummy list to hide popup");
			model.addAttribute("msg", msg);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw new WeatherException("Weather Notification Failure");
		}
		model.addAttribute("message", message);
		return "forecast";
	}

	@RequestMapping("/generateWeatherGraph")
	public String generateWeatherGraph(Model model) {
		Map<String, Double> graphMap = new ConcurrentHashMap<>();
		graphMap.put("Temp in C", Double.valueOf(weatherInfo.getTemp_c()));
		graphMap.put("Temp in F", Double.valueOf(weatherInfo.getTemp_f()));
		graphMap.put("Dew Point C", Double.valueOf(observation.getDewpointC()));
		graphMap.put("Dew Point F", Double.valueOf(observation.getDewpointF()));
		graphMap.put("Humidity", Double.valueOf(weatherInfo.getHumidity().replace("%", "")));
		model.addAttribute("graphMap", graphMap);
		model.addAttribute("place", userLocation.getCity());
		System.out.println(graphMap);
		return "graphReport";
	}

	private List<WeatherInfo> mapWeatherInfo(WeatherResponse response) {
		List<WeatherInfo> weatherInfos = new ArrayList<>();
		observation = response.getCurrentObservation();
		String date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss a").format(new Date());
		WeatherInfo info = new WeatherInfo(observation.getLocalTzLong(), observation.getWeather(),
				observation.getTemperatureString(), String.valueOf(observation.getTempF()),
				String.valueOf(observation.getTempC()), observation.getRelativeHumidity(), observation.getWindString(),
				observation.getDewpointString(), observation.getHistoryUrl(), date.split(" ")[0], date.split(" ")[1]);
		weatherInfo = info;
		weatherInfos.add(info);
		return weatherInfos;
	}
	/* END WEATHER API */

	/* START NEWS API */
	@RequestMapping("/newsHome")
	public String viewHome(Model model) throws NewsApiException {
		List<ApiResponse> idLists = service.getNewsId();
		mediaIds = idLists;
		model.addAttribute("idLists", idLists);
		return "newsHome";
	}

	@RequestMapping("/getSource")
	public String getSource(@RequestParam("id") String id, Model model) throws NewsApiException {
		List<ApiResponse> sortBys = new LinkedList<>();
		Source source = service.getSourceById(id);
		// source.getSortBysAvailable().forEach(s -> sortBys.add(new ApiResponse(null,
		// s)));
		// added manually value for drop down(select)
		sortBys.add(new ApiResponse(null, "Select"));
		Collections.reverse(sortBys);
		newsId = source.getName();
		mediaMap.put(newsId, id);
		model.addAttribute("newsId", newsId);
		model.addAttribute("sortBys", sortBys);
		model.addAttribute("idLists", mediaIds);
		return "newsHome";
	}

	@RequestMapping("/getNews")
	public String getNews(@RequestParam("source") String source, @RequestParam("sortBy") String sortBy, Model model)
			throws NewsApiException {
		String mediaSourceLink = mediaMap.get(source);
		NewsResponse response = service.getNewsApiResponse(mediaMap.get(source), sortBy);
		mediaSource = source;
		sortByNews = sortBy;
		List<Respone> respones = response.getResponse();
		currentResponse = respones;
		model.addAttribute("respones", respones);
		model.addAttribute("mediaSource", mediaSource);
		model.addAttribute("mediaSourceLink", mediaSourceLink);
		model.addAttribute("idLists", mediaIds);
		return "news";
	}

	@RequestMapping("/sendNewsEmail")
	public String sendEmail(Model model, @RequestParam("email") String emailId) throws NewsApiException {
		List<String> msg = new ArrayList<>();
		String mailMessage = service.sendEmail(emailId, newsId + " Current Update", currentResponse, mediaSource,
				sortByNews);
		msg.add("Dummy list to hide popup");
		model.addAttribute("msg", msg);
		model.addAttribute("mailMessage", mailMessage);
		model.addAttribute("idLists", mediaIds);
		return "newsHome";
	}

	/* END NEWS API */
	@RequestMapping("/sendFeedback")
	public String sendFeedback(Model model, @RequestParam("name") String name, @RequestParam("email") String email,
			@RequestParam("message") String message) throws WeatherException {
		List<String> msg = new ArrayList<>();
		try {
			msg.add("Dummy list to hide popup");
			model.addAttribute("msg", msg);
			model.addAttribute("resp", mailUtil.sendFeedback(email, name, message));
		} catch (MessagingException e) {
			throw new WeatherException("Mailing support is disable as of now please retry latter.");
		}
		return "feedback";
	}
}
