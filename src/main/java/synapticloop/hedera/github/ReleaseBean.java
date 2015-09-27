package synapticloop.hedera.github;

public class ReleaseBean {
	private String releaseNumber = null;
	private boolean isDraft = false;
	private boolean isPreRelease = false;

	public ReleaseBean(String releaseNumber, boolean isDraft, boolean isPreRelease) {
		this.releaseNumber = releaseNumber;
		this.isDraft = isDraft;
		this.isPreRelease = isPreRelease;
	}

	public String getReleaseNumber() { return this.releaseNumber; }
	public boolean getIsDraft() { return this.isDraft; }
	public boolean getIsPreRelease() { return this.isPreRelease; }
}
