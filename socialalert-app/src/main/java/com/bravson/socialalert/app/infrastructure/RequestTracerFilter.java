package com.bravson.socialalert.app.infrastructure;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.springframework.mock.web.DelegatingServletInputStream;
import org.springframework.mock.web.DelegatingServletOutputStream;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

public class RequestTracerFilter extends OncePerRequestFilter {

	private Logger logger = Logger.getLogger(getClass());
	
	private String[] customTextContentTypes = {"application/x-www-form-urlencoded"};

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			MDC.put("user", (String) authentication.getName());
		} else {
			MDC.put("user", request.getRemoteAddr());
		}

		ByteArrayOutputStream requestOutputStream = new ByteArrayOutputStream(2000);
		ByteArrayOutputStream responseOutputStream = new ByteArrayOutputStream(2000);
		request.setAttribute("requestOutputStream", requestOutputStream);
		try {
			filterChain.doFilter(wrapRequest(request, requestOutputStream), wrapResponse(response, responseOutputStream));
		} finally {
			String requestPayload = createPayload(requestOutputStream.toByteArray(), request.getCharacterEncoding(), request.getContentType());
			String responsePayload = createPayload(responseOutputStream.toByteArray(), response.getCharacterEncoding(), response.getContentType());
			logger.info(createMessage(request, response, requestPayload, responsePayload));
			MDC.remove("user");
		}
	}

	private String createMessage(HttpServletRequest request, HttpServletResponse response, String requestPayload, String responsePayload) {
		String queryString = request.getQueryString();
		String requestUri = request.getRequestURI();

		if (queryString == null) {
			queryString = "";
		}
		StringBuilder builder = new StringBuilder(1000);
		builder.append("uri: ").append(requestUri);
		if (queryString.length() > 0) {
			builder.append('?');
			builder.append(queryString);
		}
		builder.append("\n\t");
		builder.append("head in  :");
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			builder.append(" {");
			builder.append(headerName);
			builder.append(": ");
			builder.append(request.getHeader(headerName));
			builder.append("}");
		}
		builder.append("\n\t");
		builder.append("request  : ");
		builder.append(requestPayload);
		builder.append("\n\t");
		builder.append("response : ");
		builder.append(responsePayload);
		builder.append("\n\t");
		builder.append("status   : ");
		builder.append(response.getStatus());
		builder.append("\n\t");
		builder.append("head out :");
		for (String headerName : response.getHeaderNames()) {
			builder.append(" {");
			builder.append(headerName);
			builder.append(": ");
			builder.append(response.getHeader(headerName));
			builder.append("}");
		}
		builder.append("\n");
		return builder.toString();
	}

	private String createPayload(byte[] buffer, String characterEncoding, String contentType) {
		if (StringUtils.startsWith(contentType, "image/")) {
			return "[image]";
		} else if (StringUtils.startsWith(contentType, "video/")) {
			return "[video]";
		} else if (StringUtils.startsWith(contentType, "text/")) {
			return toStringContent(buffer, characterEncoding);
		} else if (StringUtils.startsWith(contentType, "application/") && (StringUtils.contains(contentType, "xml") || StringUtils.contains(contentType, "json"))) {
			return toStringContent(buffer, characterEncoding);
		} else if (ArrayUtils.contains(customTextContentTypes, contentType)) {
			return toStringContent(buffer, characterEncoding);
		} else {
			return "[unknown]";
		}
	}

	private static String toStringContent(byte[] buffer, String characterEncoding) {
		if (characterEncoding == null) {
			characterEncoding = WebUtils.DEFAULT_CHARACTER_ENCODING;
		}
		int length = Math.min(buffer.length, 2000);
		try {
			return new String(buffer, 0, length, characterEncoding);
		} catch (UnsupportedEncodingException e) {
			return "[unknown]";
		}
	}

	private HttpServletResponse wrapResponse(HttpServletResponse response, final OutputStream loggingOutputStream) {
		return new HttpServletResponseWrapper(response) {
			@Override
			public ServletOutputStream getOutputStream() throws IOException {
				return new DelegatingServletOutputStream(new TeeOutputStream(super.getOutputStream(),
						loggingOutputStream));
			}
		};
	}

	private HttpServletRequest wrapRequest(HttpServletRequest request, final OutputStream loggingOutputStrem) {
		return new HttpServletRequestWrapper(request) {
			@Override
			public ServletInputStream getInputStream() throws IOException {
				return new DelegatingServletInputStream(new TeeInputStream(super.getInputStream(), loggingOutputStrem));
			}
		};
	}
}
