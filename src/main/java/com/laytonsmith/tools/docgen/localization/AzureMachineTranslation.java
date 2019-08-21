package com.laytonsmith.tools.docgen.localization;

import com.laytonsmith.PureUtilities.Common.MutableObject;
import com.laytonsmith.PureUtilities.Web.HTTPHeaders;
import com.laytonsmith.PureUtilities.Web.HTTPMethod;
import com.laytonsmith.PureUtilities.Web.HTTPResponse;
import com.laytonsmith.PureUtilities.Web.RequestSettings;
import com.laytonsmith.PureUtilities.Web.WebUtility;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Provides an Azure based MachineTranslation interface. To use the machine translation features, a subscription to
 * the Azure Cognitive Services
 * must be set up. More information about how to sign up for an account and subscription can be found
 * <a href="https://azure.microsoft.com/en-us/services/cognitive-services/">here</a>. While paid subscriptions
 * exist, there is a free tier, which can be used in limited amounts per month. Once a subscription to
 * Cognitive Services is set up, the key and endpoint can be found under Resource Management -> Quick Start.
 * <p>
 * The spec for this implementation can be found here
 * <a href="https://docs.microsoft.com/en-us/azure/cognitive-services/translator/reference/v3-0-translate">
 * https://docs.microsoft.com/en-us/azure/cognitive-services/translator/reference/v3-0-translate</a>.
 */
public class AzureMachineTranslation implements MachineTranslation {

	/**
	 * The maximum number of items per request
	 */
	private static final int PAGE_SIZE = 100;
	/**
	 * The maximum input text size per request
	 */
	private static final int MAX_TEXT_SIZE = 5000;

	private static final String AZURE_TRANSLATE_ENDPOINT = "https://api.cognitive.microsofttranslator.com/translate";

	private static class ErrorType {
		public final int code;
		public final String detailMessage;
		public final boolean fatal;
		public ErrorType(int code, String detailMessage, boolean fatal) {
			this.code = code;
			this.detailMessage = detailMessage;
			this.fatal = fatal;
		}
	}

	/**
	 * These are the reason strings behind the various response codes provided in the spec. 200 is missing, but
	 * is a successful response.
	 */
	private static final Map<Integer, ErrorType> RESPONSE_CODES = new HashMap<Integer, ErrorType>() {{
		put(400, new ErrorType(400, "One of the query parameters is missing or not valid. Correct request parameters"
				+ " before retrying.", true));
		put(401, new ErrorType(401, "The request could not be authenticated. Check that credentials are specified and"
				+ " valid.", true));
		put(403, new ErrorType(403, "The request is not authorized. Check the details error message. This often"
				+ " indicates that all free translations provided with a trial subscription have been used up.", true));
		put(408, new ErrorType(408, "The request could not be fulfilled because a resource is missing. Check the"
				+ " details error message. When using a custom category, this often indicates that the custom"
				+ " translation system is not yet available to serve requests. The request should be retried after a"
				+ " waiting period (e.g. 1 minute).", true));
		put(429, new ErrorType(429, "The server rejected the request because the client has exceeded request limits.",
				true));
		put(500, new ErrorType(500, "An unexpected error occurred. If the error persists, report it with: date and time"
				+ " of the failure, request identifier from response header X-RequestId, and client identifier from"
				+ " request header X-ClientTraceId.", false));
		put(503, new ErrorType(503, "Server temporarily unavailable. Retry the request. If the error persists, report"
				+ " it with: date and time of the failure, request identifier from response header X-RequestId, and"
				+ " client identifier from request header X-ClientTraceId.", false));
	}};

	private final URL endpoint;
	private final String key;

	/**
	 * Creates a new AzureMachineTranslation object.
	 * @param key The cognitive services key
	 */
	public AzureMachineTranslation(String key) {
		try {
			this.endpoint = new URL(AZURE_TRANSLATE_ENDPOINT);
		} catch (MalformedURLException ex) {
			throw new Error(ex);
		}
		this.key = key;
	}

	@Override
	public String translate(String locale, String english) throws TranslationException {
		MutableObject<TranslationException> ex = new MutableObject<>();
		Map<String, String> ret = bulkTranslate(locale, new HashSet<>(Arrays.asList(english)),
				(e) -> {
					ex.setObject(e);
				});
		if(ex.getObject() != null) {
			throw ex.getObject();
		}
		return ret.get(english);
	}

	@Override
	public Map<String, String> bulkTranslate(String locale, Set<String> english, ErrorCallback callback) {
		if("art".equals(locale)) {
			return MachineTranslation.ArtMachineTranslation.doArtTranslation(english);
		}

		Map<String, String> ret = new HashMap<>();
		{
			Iterator<String> it = english.iterator();
			int itemCount = 0;
			int charCount = 0;
			Set<String> section = new HashSet<>();
			while(it.hasNext()) {
				String next = it.next();
				if(next.length() > MAX_TEXT_SIZE) {
					// We can't deal with a single segment this large, so we just have to skip it, unfortunately.
					// I don't see a good way to report this failure either.
					continue;
				}
				if(itemCount + 1 >= PAGE_SIZE || charCount + next.length() >= MAX_TEXT_SIZE) {
					try {
						ret.putAll(doRequest(locale, section));
					} catch (TranslationException ex) {
						callback.error(ex);
						// Don't continue though.
						break;
					}
					section = new HashSet<>();
					itemCount = 0;
					charCount = 0;
				} else {
					section.add(next);
				}
			}

			if(!section.isEmpty()) {
				try {
					ret.putAll(doRequest(locale, section));
				} catch (TranslationException ex) {
					callback.error(ex);
				}
			}
		}
		return ret;
	}

	private Map<String, String> doRequest(String locale, Set<String> english) throws TranslationException {
		try {
			Map<String, String> parameters = new HashMap<>();
			parameters.put("api-version", "3.0");
			parameters.put("from", "en");
			parameters.put("to", locale);
			parameters.put("textType", "plain");
			parameters.put("includeAlignment", "true");

			Map<String, List<String>> headers = new HashMap<>();
			headers.put("Content-Type", Arrays.asList("application/json"));
			headers.put("Ocp-Apim-Subscription-Key", Arrays.asList(key));

			// Order is important, because that's how we correlate the request to the response.
			List<String> requestStrings = new ArrayList<>(english);

			List<Object> body = new ArrayList<>();
			for(String e : requestStrings) {
				Map<String, String> tr = new HashMap<>();
				tr.put("Text", e);
				body.add(tr);
			}

			RequestSettings rs = new RequestSettings();
			rs.setParameters(parameters);
			rs.setRawParameter(JSONValue.toJSONString(body).getBytes("UTF-8"));
			rs.setMethod(HTTPMethod.POST);
			rs.setHeaders(headers);
			HTTPResponse response = WebUtility.GetPage(endpoint, rs);
			if(response.getResponseCode() != 200) {
				String exceptionText = response.getResponseCode() + " " + response.getResponseText() + "\n";
				String detailedErrorMessage = "No further information is available.";
				boolean fatal = true;
				if(RESPONSE_CODES.containsKey(response.getResponseCode())) {
					ErrorType t = RESPONSE_CODES.get(response.getResponseCode());
					detailedErrorMessage = t.detailMessage;
					fatal = t.fatal;
				}
				exceptionText += "RequestId: " + response.getFirstHeader("X-RequestId") + "\n";
				exceptionText += detailedErrorMessage + "\n";
				exceptionText += response.getContentAsString();
				throw new TranslationException(exceptionText, fatal);
			}

			Map<String, String> ret = new HashMap<>();
			try {
				String charset = "ISO-8859-1";
				HTTPHeaders.ContentType contentType = response.getHeaderObject().getContentType();
				if(contentType != null && contentType.charset != null) {
					charset = contentType.charset;
				}
				String payload = response.getContentAsString(charset);
				JSONArray array = (JSONArray) JSONValue.parse(payload);
				if(array.size() != requestStrings.size()) {
					// Uh oh. We can't trust the result correlation here, so bail.
					throw new TranslationException("Returned results were not the size expected, unsure how to"
							+ " correlate, so stopping. Here's the response text:\n" + response.getContentAsString(),
							true);
				}
				for(int i = 0; i < array.size(); i++) {
					JSONObject tr = (JSONObject) array.get(i);
					JSONArray translations = (JSONArray) tr.get("translations");
					String text = (String) ((JSONObject) translations.get(0)).get("text");
					ret.put(requestStrings.get(i), text);
				}
			} catch (ClassCastException cce) {
				throw new TranslationException("While parsing the request, it was not in the expected format. Here's"
						+ " the returned value:\n" + response.getContentAsString(), true);
			}

			return ret;
		} catch (IOException ex) {
			throw new TranslationException(ex, true);
		}
	}


}
