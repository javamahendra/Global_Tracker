package com.nlw.global.api.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.nlw.global.api.dto.HeaderResponse;
import com.nlw.global.api.search.dto.NearestSearchResponse;
import com.nlw.global.api.search.dto.Result;
import com.nlw.global.api.search.dto.SearchResponse;

public class ServiceUtils {

	public static List<NearestSearchResponse> getSearchInfo(SearchResponse response, String searchType) {
		List<NearestSearchResponse> searchList = new ArrayList<>();
		NearestSearchResponse searchResponse = null;
		List<Result> results = response.getResults();
		for (Result result : results) {
			searchResponse = new NearestSearchResponse();
			searchResponse.setName(result.getName());
			searchResponse.setLocation(result.getFormattedAddress());
			String type = searchType.split(" ")[0];
			if (result.getTypes().get(0).contains(type.toLowerCase())) {
				searchResponse.setTypes(result.getTypes().get(0));
			} else {
				searchResponse.setTypes("NA");
			}
			searchResponse.setRating(result.getRating());
			if (result.getPhotos() != null) {
				searchResponse.setMoreInfoLink(getInfoLink(result.getPhotos().get(0).getHtmlAttributions().get(0)));
			} else {
				searchResponse.setMoreInfoLink("");
			}
			searchList.add(searchResponse);
		}
		return orderSearchListByRating(searchList);

	}

	private static List<NearestSearchResponse> orderSearchListByRating(List<NearestSearchResponse> searchList) {
		//return searchList.stream().sorted(Comparator.comparing(NearestSearchResponse::getRating).reversed())
			//	.collect(Collectors.toList());

		return null;
	}

	public static Map<String, Integer> mapGraphData(List<NearestSearchResponse> searchList) {
		/*return searchList.stream().filter(obj -> !(obj.getTypes().equalsIgnoreCase("NA")))
				.collect(Collectors.toMap(obj -> obj.getName(), obj -> (int) obj.getRating() * 20));*/
		
		return null;
	}

	public static String getInfoLink(String text) {
		return text.substring(text.indexOf("https"), text.indexOf(">") - 1);
	}

	public static List<HeaderResponse> getTableHeaders() {
		List<HeaderResponse> responses = new ArrayList<>();
		HeaderResponse response = new HeaderResponse("City", "State", "Country", "CCD", "Latitude", "Longitude",
				"Elevation", "TimeZone", "Weather", "Temprature", "Temp(F)", "Temp(C)", "Humidity", "Wind", "DewPoint",
				"Update Date", "Time", "View-Web", "/images/gr.jpg");
		responses.add(response);
		return responses;
	}
}


