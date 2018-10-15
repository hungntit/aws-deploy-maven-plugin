package maven.aws;

import org.apache.maven.plugins.annotations.Parameter;

public abstract class AbstractAwsBuildMojo extends AbstractBuildMojo {

    @Parameter(
            property = "aws.region",
            required = true
    )
    protected String region;

    @Parameter(
            property = "aws.profile"
    )
    protected String profile;
}
