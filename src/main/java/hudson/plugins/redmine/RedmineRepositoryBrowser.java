package hudson.plugins.redmine;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.scm.EditType;
import hudson.scm.RepositoryBrowser;
import hudson.scm.SubversionRepositoryBrowser;
import hudson.scm.SubversionChangeLogSet.LogEntry;
import hudson.scm.SubversionChangeLogSet.Path;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * produces redmine links.
 *
 * @author gaooh
 * @date 2008/10/26
 */
public class RedmineRepositoryBrowser extends SubversionRepositoryBrowser {

	@DataBoundConstructor
    public RedmineRepositoryBrowser() {
    }

	@Override
	public URL getDiffLink(Path path) throws IOException {
		if(path.getEditType()!= EditType.EDIT) {
            return null;
		}
        URL baseUrl = getRedmineURL(path.getLogEntry());
        String projectName = getProject(path.getLogEntry());
        String filePath = getFilePath(path.getLogEntry(), path.getValue());

        int revision = path.getLogEntry().getRevision();
        return new URL(baseUrl, "projects/" + projectName + "/repository/revisions/" + revision + "/diff" + filePath);
	}

	@Override
	public URL getFileLink(Path path) throws IOException {
		URL baseUrl = getRedmineURL(path.getLogEntry());
		String projectName = getProject(path.getLogEntry());
		String filePath = getFilePath(path.getLogEntry(), path.getValue());

        int revision = path.getLogEntry().getRevision();
        return baseUrl == null ? null : new URL(baseUrl, "projects/" + projectName + "/repository/revisions/" + revision + "/entry" + filePath);
	}

	@Override
	public URL getChangeSetLink(LogEntry changeSet) throws IOException {
		URL baseUrl = getRedmineURL(changeSet);
		String projectName = getProject(changeSet);
        return baseUrl == null ? null : new URL(baseUrl, "projects/" + projectName + "/repository/revisions/" + changeSet.getRevision());
	}

	@Override
	public Descriptor<RepositoryBrowser<?>> getDescriptor() {
		 return DESCRIPTOR;
	}

	private URL getRedmineURL(LogEntry logEntry) throws MalformedURLException {
        AbstractProject<?,?> p = (AbstractProject<?,?>)logEntry.getParent().build.getProject();
        RedmineProjectProperty rpp = p.getProperty(RedmineProjectProperty.class);
        if(rpp == null) {
        	return null;
        } else {
        	return new URL(rpp.redmineWebsite);
        }
    }

	private String getProject(LogEntry logEntry) {
		AbstractProject<?,?> p = (AbstractProject<?,?>)logEntry.getParent().build.getProject();
		RedmineProjectProperty rpp = p.getProperty(RedmineProjectProperty.class);
        if(rpp == null) {
        	return null;
        } else {
        	return rpp.projectName;
        }
	}

	private String getFilePath(LogEntry logEntry, String fileFullPath) throws MalformedURLException {
		AbstractProject<?,?> p = (AbstractProject<?,?>)logEntry.getParent().build.getProject();
		RedmineProjectProperty rpp = p.getProperty(RedmineProjectProperty.class);

		String filePath = "";
        if(VersionUtil.isVersionBefore081(rpp.redmineVersionNumber)) {
        	String[] filePaths = fileFullPath.split("/");
        	filePath = "/";
        	if(filePaths.length > 2) {
        		for(int i = 2 ; i < filePaths.length; i++) {
        			filePath = filePath + filePaths[i];
        			if(i != filePaths.length - 1) {
        				filePath = filePath + "/";
        			}
        		}
        	}
        } else {
        	filePath = fileFullPath;
        }

        if(rpp != null && rpp.redmineRepositoryRoot != null) {
        	filePath = filePath.replaceFirst(Pattern.quote(rpp.redmineRepositoryRoot), "");
        }

        return filePath;

	}

	@Extension
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends Descriptor<RepositoryBrowser<?>> {
        public DescriptorImpl() {
            super(RedmineRepositoryBrowser.class);
        }

        public String getDisplayName() {
            return "Redmine";
        }
    }
}
