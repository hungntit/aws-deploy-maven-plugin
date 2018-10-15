package com.dzodi;

import org.apache.maven.plugins.annotations.Parameter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public abstract class AbstractBuildMojo extends AbstractCommandMojo {
    @Parameter(
            property = "project.artifactId"
    )
    protected String artifactId;

    @Parameter(
            property = "project.groupId"
    )
    protected String groupId;

    @Parameter(
            property = "project.version"
    )
    protected String version;

    @Parameter(
            property = "project.name"
    )
    protected String projectName;

    @Parameter(
            property = "user.name"
    )
    protected String username;

    @Parameter(
            property = "build.number"
    )

    protected String buildNumber;

    @Parameter(defaultValue = "${session.request.startTime}", readonly = true)
    protected Date mavenBuildTimeStamp;


    public void handleBuildNumber() {
        if (buildNumber == null) {
            buildNumber = new SimpleDateFormat("yyyyMMddHHmmss").format(mavenBuildTimeStamp);
        }
    }

    @Override
    public Properties getFullProperties() {
        Properties properties = super.getFullProperties();
        properties.put("build.number", buildNumber);
        return properties;
    }
}
