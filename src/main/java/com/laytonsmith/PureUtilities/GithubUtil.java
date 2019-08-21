package com.laytonsmith.PureUtilities;

import com.laytonsmith.PureUtilities.Web.HTTPHeaders;
import com.laytonsmith.PureUtilities.Web.HTTPMethod;
import com.laytonsmith.PureUtilities.Web.HTTPResponse;
import com.laytonsmith.PureUtilities.Web.RequestSettings;
import com.laytonsmith.PureUtilities.Web.WebUtility;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * A utility class containing methods for calling towards the github api.
 */
public class GithubUtil {

	private static final String GITHUB_URL = "https://api.github.com";
	private static final String DATE_FORMAT = "YYYY-MM-DDTHH:MM:SSZ";

	private final String authToken;

	public GithubUtil(String bearerToken) {
		this.authToken = bearerToken;
	}

	public static class GithubException extends IOException {

		private final int responseCode;
		private final String responseCodeText;

		public GithubException(int responseCode, String responseCodeText, String message) {
			super(responseCode + " " + responseCodeText + ": " + message);
			this.responseCode = responseCode;
			this.responseCodeText = responseCodeText;
		}

		public GithubException(IOException ex) {
			super(ex.getMessage(), ex);
			responseCode = 0;
			responseCodeText = "";
		}

		/**
		 * Returns the response code. If the underlying request failed due to a general IOException, this will be
		 * 0.
		 * @return
		 */
		public int getResponseCode() {
			return responseCode;
		}

		/**
		 * Returns the response code text, for instance "Bad Request" for a 400.
		 * @return
		 */
		public String getResponesCodeText() {
			return responseCodeText;
		}
	}

	/**
	 * Represents a generic Github response for all calls. The content varies, but will be either a JSONObject or
	 * JSONArray, depending on the call. You can look up that fact with isObject. The remaining quota is also stored
	 * in the response.
	 */
	public static class GithubResponse {
		public Object content;
		public Boolean isObject;
		public int remainingQuota;

		public JSONObject asObject() {
			return (JSONObject) content;
		}

		public JSONArray asArray() {
			return (JSONArray) content;
		}
	}

	/**
	 * Makes a generic call towards the Github API. This method handles all authentication and pagination steps.
	 * @param method The http method
	 * @param path The path, including template, starting with a slash. For instance, {@code /repos/:owner/:repo/forks}.
	 * The path params "owner" and "repo" must be included in the pathParams object.
	 * @param pathParams The parameters that should be replaced in the path.
	 * @param queryParams Any additional query parameters.
	 * @return The raw contents
	 * @throws com.laytonsmith.PureUtilities.GithubUtil.GithubException If we received a response from github, but
	 * it was a non-2xx response, or the connection failed for a more general IOException.
	 */
	public GithubResponse githubApiCall(HTTPMethod method, String path, Map<String, String> pathParams,
			Map<String, String> queryParams) throws GithubException {
		if(pathParams == null) {
			pathParams = new HashMap<>();
		}
		if(queryParams == null) {
			queryParams = new HashMap<>();
		}
		for(Map.Entry<String, String> e : pathParams.entrySet()) {
			path = path.replaceAll(":" + e.getKey(), e.getValue());
		}

		queryParams = new HashMap<>(queryParams);
		queryParams.put("per_page", "100");

		GithubResponse r = new GithubResponse();
		String url = GITHUB_URL + path;
		while(true) {
			RequestSettings rs = new RequestSettings();
			rs.setMethod(method);
			rs.setHeaders(MapBuilder
					.start("Accept", Arrays.asList("application/vnd.github.v3+json"))
					.set("Authorization", Arrays.asList("token " + authToken)));
			rs.setQueryParameters(queryParams);
			HTTPResponse response;
			try {
				response = WebUtility.GetPage(new URL(url), rs);
			} catch (IOException ex) {
				throw new GithubException(ex);
			}
			if(response.getResponseCode() < 200 || response.getResponseCode() > 299) {
				throw new GithubException(response.getResponseCode(), response.getResponseText(),
						response.getContentAsString());
			}

			HTTPHeaders headers = response.getHeaderObject();

			r.remainingQuota = Integer.parseInt(headers.getFirstHeader("X-RateLimit-Remaining"));
			Object value;
			try {
				value = JSONValue.parse(response.getContentAsString(headers.getContentType().charset));
			} catch (UnsupportedEncodingException ex) {
				throw new GithubException(ex);
			}
			if(r.isObject == null) {
				r.isObject = value instanceof JSONObject;
			}


			if(r.content == null) {
				r.content = value;
			} else if(!r.isObject) {
				// At this point, we know it's an array, and that there were previous pages, so just append these
				// to the existing list
				JSONArray existing = (JSONArray) r.content;
				JSONArray current = (JSONArray) value;
				existing.addAll(current);
			}

			if(r.isObject || headers.getLink("next") == null) {
				break;
			}

			url = headers.getLink("next");
			if(url.startsWith("/")) {
				// relative path, put the domain back on
				url = GITHUB_URL + url;
			}
		}
		return r;
	}


	/**
	 * Lists repositories that the authenticated user has explicit permission ({@code :read}, {@code :write}, or
	 * {@code :admin}) to access.
	 * <p>
	 * The authenticated user has explicit permission to access repositories they own, repositories where they are a
	 * collaborator, and repositories that they can access through an organization membership.
	 *
	 * https://developer.github.com/v3/repos/#list-your-repositories
	 * @param visibility Can be one of {@code all}, {@code public}, or {@code private}. Default: {@code all}
	 * @param affiliation Comma-separated list of values. Can include:
	 * <ul>
	 * <li>{@code owner}: Repositories that are owned by the authenticated user.</li>
	 * <li>{@code collaborator}: Repositories that the user has been added to as a collaborator.</li>
	 * <li>{@code organization_member}: Repositories that the user has access to through being a member of
	 * an organization.
	 * This includes every repository on every team that the user is on.</li>
	 * </ul>
	 * <br>Default: {@code owner,collaborator,organization_member}
	 * @param type Can be one of {@code all}, {@code owner}, {@code public}, {@code private}, {@code member}.
	 * <br>Default: {@code all}
	 * <br>Will cause a 422 error if used in the same request as visibility or affiliation.
	 * @param sort Can be one of {@code created}, {@code updated}, {@code pushed}, {@code full_name}.
	 * <br>Default: {@code full_name}
	 * @param direction Can be one of {@code asc} or {@code desc}. Default: {@code asc} when using full_name,
	 * otherwise desc
	 * @return The returned repos will not have complete information available. For those you are interested in details
	 * about, you should call {@link #getRepo} to get full repository information. The owner parameter is the
	 * owner.login field, and the repo is the name field.
	 * @throws com.laytonsmith.PureUtilities.GithubUtil.GithubException
	 */
	public List<Repository> listRepos(String visibility, String affiliation, String type, String sort,
			String direction) throws GithubException {

		GithubResponse r = githubApiCall(HTTPMethod.GET, "/user/repos", null,
				MapBuilder.empty(String.class, String.class)
						.setIfValueNotNull("visibility", visibility)
						.setIfValueNotNull("affiliation", affiliation)
						.setIfValueNotNull("type", type)
						.setIfValueNotNull("sort", sort)
						.setIfValueNotNull("direction", direction));
		List<Repository> ret = new ArrayList<>();
		for(Object o : r.asArray()) {
			ret.add(Repository.parse((JSONObject) o));
		}
		return ret;
	}

	/**
	 * Returns full details about an existing repository.
	 * @param owner (Required) repo.owner.login
	 * @param repo (Required) repo.name
	 * @return
	 * @throws com.laytonsmith.PureUtilities.GithubUtil.GithubException
	 */
	public Repository getRepo(String owner, String repo) throws GithubException {
		GithubResponse r = githubApiCall(HTTPMethod.GET, "/repos/:owner/:repo", MapBuilder
				.start("owner", owner)
				.set("repo", repo), null);

		return Repository.parse(r.asObject());
	}

	/**
	 * Forks the given repository. The resulting fork is returned.
	 * <p>
	 * Note: Forking a Repository happens asynchronously. You may have to wait a short period of time before you can
	 * access the git objects.
	 * @param owner (Required) owner.login
	 * @param repo (Required) repo.name
	 * @param organization
	 * @return
	 * @throws com.laytonsmith.PureUtilities.GithubUtil.GithubException
	 */
	public Repository forkRepo(String owner, String repo, String organization) throws GithubException {
		GithubResponse r = githubApiCall(HTTPMethod.POST, "/repos/:owner/:repo/forks",
				MapBuilder.start("owner", owner).set("repo", repo),
				MapBuilder.empty(String.class, String.class).setIfValueNotNull("organization", organization));
		return Repository.parse(r.asObject());
	}


	// *****************************************************************************************************************
	// * Models                                                                                                        *
	// *****************************************************************************************************************

	public static class Owner {
		public String login;
		public int id;

		static Owner parse(JSONObject obj) {
			Owner o = new Owner();
			o.login = obj.get("login").toString();
			o.id = Integer.parseInt(obj.get("id").toString());
			return o;
		}
	}

	public static class Repository {
		public int id;
		public String name;
		public String fullName;
		public Owner owner;
		public String htmlUrl;
		/**
		 * This is the http based clone url.
		 */
		public String cloneUrl;
		/**
		 * This is the ssh based clone url.
		 */
		public String sshUrl;
		public boolean fork;
		/**
		 * parent is the repository this repository was forked from. Will be null if {@code fork} is false.
		 */
		public Repository parent;
		/**
		 * source is the ultimate source for the network. Will be null if {@code fork} is false.
		 */
		public Repository source;

		static Repository parse(JSONObject obj) {
			if(obj == null) {
				return null;
			}
			Repository r = new Repository();
			r.id = Integer.parseInt(obj.get("id").toString());
			r.name = obj.get("name").toString();
			r.fullName = obj.get("full_name").toString();
			r.owner = Owner.parse((JSONObject) obj.get("owner"));
			r.htmlUrl = obj.get("html_url").toString();
			r.cloneUrl = obj.get("clone_url").toString();
			r.sshUrl = obj.get("ssh_url").toString();
			r.fork = Boolean.parseBoolean(obj.get("fork").toString());
			if(r.fork) {
				r.parent = Repository.parse((JSONObject) obj.get("parent"));
				r.source = Repository.parse((JSONObject) obj.get("source"));
			}
			return r;
		}
	}

}
