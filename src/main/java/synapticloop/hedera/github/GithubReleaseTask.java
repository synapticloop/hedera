package synapticloop.hedera.github;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.json.JSONArray;
import org.json.JSONObject;

import synapticloop.hedera.github.util.HttpHelper;

public class GithubReleaseTask extends GithubReleaseBaseTask {

	// the version - if not set - will default to 'latest'
	protected String version = null;
	// the output directory
	protected String outDir = null;
	// whether to over-write the file
	protected boolean overwrite = false;

	// the shortname for logging
	protected String details = null;

	@Override
	public void execute() throws BuildException {
		checkParameter("owner", owner);
		checkParameter("repo", repo);
		checkParameter("asset", asset);
		checkParameter("outDir", outDir);

		if(null == version || version.trim().length() == 0) {
			getProject().log(this, "[ " + owner + "/" + repo + " " + asset + " ] No version set, using version 'latest'", Project.MSG_INFO);
			version = "latest";

			getProject().log(this, "[ " + owner + "/" + repo + " " + asset + " ] Current available releases are: ", Project.MSG_INFO);
			listReleaseBeans();
		} else {
			// we need to get the version from the tagged release
			version = "tags/" + version;
		}

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("[ ");
		stringBuilder.append(owner);
		stringBuilder.append("/");
		stringBuilder.append(repo);
		stringBuilder.append(" ");
		stringBuilder.append(asset);
		stringBuilder.append("@");
		stringBuilder.append(version);
		stringBuilder.append(" ] ");

		details = stringBuilder.toString();

		String url = GITHUB_API_REPOS + "/" + owner + "/" + repo + "/releases/" + version;

		String downloadableAssetUrl = null;

		try {
			JSONObject jsonObject = new JSONObject(HttpHelper.getUrlContents(url));
			JSONArray jsonArray = jsonObject.getJSONArray(JSON_KEY_ASSETS);
			if(jsonArray.length() == 0) {
				throw new BuildException("There were no assets in the release version '" + version + "'");
			} else {
				for(int i = 0; i < jsonArray.length(); i++) {
					JSONObject assetObject = jsonArray.getJSONObject(i);
					String name = assetObject.getString(JSON_KEY_NAME);
					if(name.equals(asset)) {
						// this is the one we want
						downloadableAssetUrl = assetObject.getString(JSON_KEY_BROWSER_DOWNLOAD_URL);
						break;
					}
				}
			}

			if(null != downloadableAssetUrl) {
				File outputDirectory = new File(outDir);
				// ensure that the directory exists
				if(!outputDirectory.exists()) {
					// create the directories
					boolean mkdirs = outputDirectory.mkdirs();
					if(mkdirs) {
						getProject().log(this, details + "Created missing output directory of '" + outputDirectory.getPath() + "'.", Project.MSG_INFO);
					} else {
						logAndThrow(details + "Could not create missing output directory of '" + outputDirectory.getPath() + "'.");
					}
				}

				if(outputDirectory.exists() && outputDirectory.isFile()) {
					logAndThrow(details + "Output directory '" + outputDirectory.getPath() + "', exists, but is not a directory, please remove this file.");
				}

				File outputFile = new File(outputDirectory.getPath() + File.separatorChar + asset);
				if(outputFile.exists() && !overwrite) {
					logAndThrow(details + "File '" + outputFile.getName() + "' already exists, please delete this file or use the overwrite=\"true\" attribute on this task.");
				}

				HttpHelper.writeUrlToFile(downloadableAssetUrl, outputFile);
				getProject().log(this, details + "Successfully downloaded release -> " + outputFile.getPath(), Project.MSG_INFO);
			} else {
				throw new BuildException("Could not find a downloadable asset for '" + asset + "'.");
			}
		} catch (IOException ioex) {
			ioex.printStackTrace();
			throw new BuildException("Could not determine releases from '" + url + "'.", ioex);
		}
	}

	public void setOwner(String owner) { this.owner = owner; }
	public void setRepo(String repo) { this.repo = repo; }
	public void setVersion(String version) { this.version = version; }
	public void setOutDir(String outDir) { this.outDir = outDir; }
	public void setAsset(String asset) { this.asset = asset; }
	public void setOverwrite(boolean overwrite) { this.overwrite = overwrite; }
}
