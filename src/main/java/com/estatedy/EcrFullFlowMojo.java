package com.estatedy;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static com.estatedy.Command.convertClasspathToShCommand;

@Mojo(name = "deploy", defaultPhase = LifecyclePhase.DEPLOY)
public class EcrFullFlowMojo extends AbstractAwsBuildMojo {

    @Parameter(
            property = "aws.ecr.account.id",
            required = true
    )
    private String ecrAccountId;

    private String getEcrRegistryId() {
        return String.format("%s.dkr.ecr.%s.amazonaws.com", ecrAccountId, region);
    }

    public List<Command> getCommands() throws MojoExecutionException {
        handleBuildNumber();

        Command ecrLogin = convertClasspathToShCommand(new Command(getWorkingDirectory(),
                "classpath:ecr_login.sh", ecrAccountId, region, groupId + "-" + artifactId, version + "-" + buildNumber, profile));
        return Arrays.asList(
                new Command(this.getWorkingDirectory(),
                        "cp", "-rp", getDockerFile().getAbsolutePath(), getWorkingDirectory().getAbsolutePath() + "/"),
                ecrLogin);
    }

    private File getDockerFile() {
        return new File(getBasedir(), "Dockerfile");
    }

    @Override
    public void verify() throws MojoExecutionException {
        super.verify();
        if (!getDockerFile().exists()) {
            throw new MojoExecutionException("No Dockerfile exist on " + getBasedir());
        }
    }
}
