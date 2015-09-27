package synapticloop.hedera.github;

import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.json.JSONArray;
import org.json.JSONObject;

import synapticloop.hedera.github.util.HttpHelper;

public abstract class GithubReleaseBaseTask extends Task {

	protected static final String GITHUB_API_REPOS = "https://api.github.com/repos/";

	protected static final String JSON_KEY_BROWSER_DOWNLOAD_URL = "browser_download_url";
	protected static final String JSON_KEY_NAME = "name";
	protected static final String JSON_KEY_ASSETS = "assets";

	// the owner of the github reporitories
	protected String owner = null;
	// the repository name
	protected String repo = null;
	// the name of the asset that you want to download
	protected String asset = null;

	protected void checkParameter(String name, String parameter) throws BuildException {
		if(null == parameter || parameter.trim().length() == 0) {
			logAndThrow("Task parameter '" + name + "', was not provided, failing...");
		}
	}

	protected void listReleaseBeans() {
		String url = GITHUB_API_REPOS + owner + "/" + repo + "/releases";

		try {
			String urlContents = HttpHelper.getUrlContents(url);

			JSONArray jsonArray = new JSONArray(urlContents);

			int releasesLength = jsonArray.length();

			getProject().log(this, "[ " + releasesLength + " ] release" + ((releasesLength != 1)? "s" : "") + " found in GitHub repository '" + owner + "/" + repo + "'.", Project.MSG_ERR);


			boolean isFirstAsset = true;
			for (int i = 0; i < releasesLength; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String releaseNumber = jsonObject.getString("tag_name");
				boolean isDraft = jsonObject.getBoolean("draft");
				boolean isPreRelease = jsonObject.getBoolean("prerelease");

				JSONArray assetsArray = jsonObject.getJSONArray(JSON_KEY_ASSETS);

				for(int j = 0; j < assetsArray.length(); j++) {
					JSONObject assetObject = assetsArray.getJSONObject(j);
					String name = assetObject.getString(JSON_KEY_NAME);

					if(name.equals(asset)) {
						// this is the one we want
						getProject().log(this, "    version: " + releaseNumber + ((isDraft)? " [ DRAFT ]" : "") + ((isPreRelease)? " [ PRERELEASE ]" : "") +  ((isFirstAsset && !isDraft && !isPreRelease)? " <-- [ LATEST ]" : ""), Project.MSG_ERR);
						if(!isDraft && !isPreRelease) {
							isFirstAsset = false;
						}
						break;
					}
				}
			}

		} catch (IOException ioex) {
			throw new BuildException("Could not list releases from '" + url + "'.", ioex);
		}
	}

	protected void logAndThrow(String message) throws BuildException {
		getProject().log(this, message, Project.MSG_ERR);
		throw new BuildException(message);
	}

	public void setOwner(String owner) { this.owner = owner; }
	public void setRepo(String repo) { this.repo = repo; }
	public void setAsset(String asset) { this.asset = asset; }
}
