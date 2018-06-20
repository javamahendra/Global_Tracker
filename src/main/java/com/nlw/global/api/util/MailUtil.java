package com.nlw.global.api.util;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.nlw.global.api.dto.DisplayLocation;
import com.nlw.global.api.dto.Respone;
import com.nlw.global.api.dto.WeatherInfo;
import com.nlw.global.api.search.dto.NearestSearchResponse;

@Component
public class MailUtil {
	@Autowired
	private JavaMailSender sender;
	@Autowired
	private TemplateEngine templateEngine;

	public String sendEmail(String to, String subject, List<NearestSearchResponse> searchResponses) throws Exception {
		String templateName = "email/gMapEmailTemplate";
		Context context = new Context();
		String searchType = searchResponses.get(0).getTypes();
		context.setVariable("searchResponses", searchResponses);
		context.setVariable("searchType", searchType);

		String body = templateEngine.process(templateName, context);

		MimeMessage mail = sender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mail, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
				StandardCharsets.UTF_8.name());
		helper.setTo(to);
		helper.setSubject(subject);
		helper.setText(body, true);
		helper.setFrom("javabymahendra@gmail.com");
		sender.send(mail);
		return "mail send successfully";
	}

	public String sendEmail(String to, String subject, MultipartFile image,
			InputStreamSource imageSource, DisplayLocation userLocation, WeatherInfo weather) throws Exception {
		String templateName = "email/weatherEmailTemplate";

		Context context = new Context();
		context.setVariable("weather", weather);
		context.setVariable("location", userLocation);
		// add for image
		// context.setVariable("imageResourceName", image.getName());

		InputStream in = getClass().getResourceAsStream("download.jpg");

		context.setVariable("imageResourceName", in);
		String body = templateEngine.process(templateName, context);

		MimeMessage mail = sender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mail, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
				StandardCharsets.UTF_8.name());
		helper.setTo(to);
		helper.setSubject(subject);
		helper.setText(body, true);
		helper.addInline(image.getName(), imageSource, image.getContentType());
		helper.setFrom("javabymahendra@gmail.com");
		sender.send(mail);
		return "mail send successfully to (" + to + ")";
	}

	public String sendEmail(String to, String subject, List<Respone> respones, String mediaSource, String sortBy)
			throws Exception {
		String templateName = "email/newsEmailTemplate";
		Context context = new Context();
		context.setVariable("respones", respones);
		context.setVariable("mediaSource", mediaSource);
		context.setVariable("sortBy", sortBy);
		String body = templateEngine.process(templateName, context);
		MimeMessage mail = sender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mail, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
				StandardCharsets.UTF_8.name());
		helper.setTo(to);
		helper.setSubject(subject);
		helper.setText(body, true);
		helper.setFrom("javabymahendra@gmail.com");
		sender.send(mail);
		return "mail send successfully to : (" + to + ")";
	}

	public String sendFeedback(String from, String name, String message) throws MessagingException {
		MimeMessage mail = sender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mail, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
				StandardCharsets.UTF_8.name());
		helper.setTo("javabymahendra@gmail.com");
		helper.setSubject("Feedback");
		helper.setText(message + "(" + name + ")", true);
		helper.setFrom(from);
		sender.send(mail);
		return "Hi " + name + " Thanks for your feedback we will look on it .please keep in touch with us ";
	}
}
