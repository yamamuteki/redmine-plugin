package hudson.plugins.redmine;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.model.AbstractProject;
import hudson.model.Job;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Property for {@link AbstractProject} that stores the associated Redmine website URL.
 *
 * @author gaooh
 * @date 2008/10/13
 */
public class RedmineProjectProperty extends JobProperty<AbstractProject<?, ?>> {

	public final String redmineWebsite;

	public final String projectName;

	public final String redmineVersionNumber;

	public final String redmineRepositoryRoot;

	@DataBoundConstructor
	public RedmineProjectProperty(String redmineWebsite, String projectName, String redmineVersionNumber, String redmineRepositoryRoot) {
		if (StringUtils.isBlank(redmineWebsite)) {
			redmineWebsite = null;
		} else {
			if (!redmineWebsite.endsWith("/")) {
				redmineWebsite += '/';
			}
		}
		this.redmineWebsite = redmineWebsite;
		this.projectName = projectName;
		this.redmineVersionNumber = redmineVersionNumber;
		this.redmineRepositoryRoot = redmineRepositoryRoot;
	}

	@Override
    public Action getJobAction(AbstractProject<?,?> job) {
        return new RedmineLinkAction(this);
    }

	@Override
	public JobPropertyDescriptor getDescriptor() {
		return DESCRIPTOR;
	}

	@Extension
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	public static final class DescriptorImpl extends JobPropertyDescriptor {
		private transient String redmineWebsite;

		public DescriptorImpl() {
			super(RedmineProjectProperty.class);
			load();
		}

		@Override
		public boolean isApplicable(Class<? extends Job> jobType) {
			return AbstractProject.class.isAssignableFrom(jobType);
		}

		public String getDisplayName() {
			return "Associated Redmine website";
		}

		@Override
		public JobProperty<?> newInstance(StaplerRequest req, JSONObject formData) throws FormException {
			try {
				String redmineWebSite = req.getParameter("redmine.redmineWebsite");
				String projectName = req.getParameter("redmine.projectName");
				String redmineVersionNumber = req.getParameter("redmine.redmineVersionNumber");
				String redmineRepositoryRoot = req.getParameter("redmine.redmineRepositoryRoot");

				return new RedmineProjectProperty(redmineWebSite, projectName, redmineVersionNumber, redmineRepositoryRoot);

			} catch (IllegalArgumentException e) {
				throw new FormException("redmine.redmineWebsite", "redmine.redmineWebSite");
			}
		}

	}
}
