package synapticloop.hedera.github;

import org.apache.tools.ant.BuildException;

public class GithubReleaseListTask extends GithubReleaseBaseTask {
	@Override
	public void execute() throws BuildException {
		checkParameter("owner", owner);
		checkParameter("repo", repo);
		checkParameter("asset", asset);


		listReleaseBeans();
	}
}
