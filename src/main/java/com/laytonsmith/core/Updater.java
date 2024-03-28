package com.laytonsmith.core;

import com.google.gson.JsonSyntaxException;
import com.laytonsmith.PureUtilities.ArgumentParser;
import com.laytonsmith.core.apps.AppsApiUtil;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.functions.Meta;
import com.laytonsmith.core.natives.interfaces.Mixed;
import io.swagger.client.ApiException;
import io.swagger.client.api.BuildsApi;
import io.swagger.client.model.BuildArtifact;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Contains command line tools and other tools to handle self-updating.
 */
public class Updater {

	private static final String DEFAULT_UPDATE_CHANNEL = "commandhelperjar";
	private static final BuildsApi API = new BuildsApi();

	public static Date getDateFromString(String date) throws ParseException {
		date = date.replaceAll("Z$", "+0000");
		return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(date);
	}

	public static BuildArtifact getLatestVersionInfo() throws ApiException, ParseException {
		List<BuildArtifact> versions = API.buildsArtifactGet(DEFAULT_UPDATE_CHANNEL);
		BuildArtifact maxVersion = null;
		for(int i = 0; i < versions.size(); i++) {
			BuildArtifact version = versions.get(i);
			Date versionDate = getDateFromString(version.getDate());
			if(maxVersion == null || versionDate.after(getDateFromString(maxVersion.getDate()))) {
				maxVersion = version;
			}
		}
		return maxVersion;
	}

	public static Boolean isUpdateAvailable() {
		Mixed buildDate = new Meta.engine_build_date().exec(null, null, (Mixed[]) null);
		if(buildDate instanceof CNull) {
			return null;
		}
		@SuppressWarnings("null")
		long iBuildDate = ((CInt) buildDate).getInt();
		Date time = new Date(iBuildDate);
		try {
			BuildArtifact latestVersion = getLatestVersionInfo();
			if(latestVersion != null) {
				// date, buildId, and name point to the latest version. Check if it's earlier than our
				// build date. Note we add 5 minutes to the build time, because the engine build date and
				// date on the api aren't precisely the same, since we don't parse the date from the jar,
				// but rather use file creation timestamps on the blob store, which will vary a bit. This
				// does mean that we can't resolve builds that are done within 5 minutes of each other, but
				// this shouldn't be a problem in most cases. In any case, the newest build will be grabbed
				// if a forced update is done, regardless of the mismatch in resolution.

				Date versionDate = getDateFromString(latestVersion.getDate());
				Calendar cal = Calendar.getInstance();
				cal.setTime(versionDate);
				cal.add(Calendar.MINUTE, 5);
				versionDate = cal.getTime();
				return time.before(versionDate);
			}
			return null;
		} catch (ApiException | JsonSyntaxException | IllegalStateException | ParseException ex) {
			return null;
		}
	}

	@tool("check-update")
	public static class CheckUpdate extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
					.addDescription("Checks if an update is available from the build server.")
					.addExtendedDescription("By default, the command checks against the official build server, "
							+ " which is located at " + AppsApiUtil.OFFICIAL_API_LOCATION
							+ " however this can be changed to other build servers by setting the "
							+ AppsApiUtil.APPS_SERVER_PROPERTY + " system property.");
			// Eventually, we want to have update channels, but for now there's only one, so no need
			// to offer to change that.
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			Boolean updateAvailable = isUpdateAvailable();
			if(updateAvailable == null) {
				System.err.println("Unable to determine build date of this installation, or version info is wrong,"
					+ " cannot check for updates.");
					//+ " You may attempt to update anyhow, and the latest build version will be obtained.");
				System.exit(1);
			}
			if(updateAvailable) {
				System.out.println("An update is available! Check https://methodscript.com/docs/"
						+ MSVersion.LATEST.getVersionString() +  "/Download.html"); // or run the update command.");
			} else {
				System.out.println("No update is available."); // You can force a download of the latest version anyways"
						//+ " by running the update command.");
			}
		}

	}
}
