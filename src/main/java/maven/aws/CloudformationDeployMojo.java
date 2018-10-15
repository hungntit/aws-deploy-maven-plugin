package maven.aws;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static maven.aws.Command.convertClasspathToShCommand;

@Mojo(name = "cloudformation:deploy", defaultPhase = LifecyclePhase.DEPLOY)
public class CloudformationDeployMojo extends AbstractAwsBuildMojo {
    @Parameter(
            property = "aws.stack.name",
            defaultValue = "${project.name}"
    )
    protected String stackName;

    @Parameter(
            property = "aws.stack.template.name",
            defaultValue = "template.yml"
    )
    protected String templateFileName;

    @Parameter(
            property = "aws.stack.env"
    )
    protected String environment;

    public List<Command> getCommands() throws MojoExecutionException {
        getTargetDir().mkdir();
        System.out.println("target dir:"+ getTargetDir().exists());
        executeCommand(new Command(this.getWorkingDirectory(),
                "cp", "-rp", getBaseCloudFormationDir().getAbsolutePath(), getTargetDir().getAbsolutePath() + "/"));
        handleBuildNumber();
        handleTemplate();
        File environmentFile = getEnvironmentFile();
        Command cloudformationCmd = convertClasspathToShCommand(new Command(getWorkingDirectory(),
                "classpath:cloudformation.sh", region, stackName,
                getTemplateFile().getAbsolutePath(),
                environmentFile == null ? "" : environmentFile.getAbsolutePath()), Command.ClasspathCommandType.bash);
        return Arrays.asList(cloudformationCmd);
    }

    private void handleTemplate() throws MojoExecutionException {
        PropertiesUtils.handlePropertyFile(getFullProperties(), getTemplateFile().getAbsolutePath());
        if (environment != null && !"".equals(environment)) {
            PropertiesUtils.handlePropertyFile(getFullProperties(), getEnvironmentFile().getAbsolutePath());
        }
    }

    public File getTemplateFile() {
        return new File(getCloudFormationDir(), templateFileName);
    }

    public File getEnvironmentFile() {
        if (environment != null && !"".equals(environment)) {
            return new File(new File(getCloudFormationDir(), "environments"), environment);
        } else {
            return null;
        }
    }

    public File getBaseTemplateFile() {
        return new File(getBaseCloudFormationDir(), templateFileName);
    }

    public File getBaseEnvironmentFile() {
        return new File(new File(getBaseCloudFormationDir(), "environments"), environment);
    }

    private File getBaseCloudFormationDir() {
        return new File(getBasedir(), "cloudformation");
    }

    private File getCloudFormationDir() {
        return new File(getTargetDir(), "cloudformation");
    }

    @Override
    public void verify() throws MojoExecutionException {
        if (!getBaseCloudFormationDir().exists()) {
            throw new MojoExecutionException("cloudformation folder must exists on " + getBasedir());
        }
        if (!getBaseTemplateFile().exists()) {
            throw new MojoExecutionException(getBaseTemplateFile().getAbsolutePath() + " does exists on " + getBasedir());
        }
        System.out.println("environment:" + environment);

        if (environment != null && !"".equals(environment)) {
            if (!getBaseEnvironmentFile().exists()) {
                throw new MojoExecutionException(getBaseEnvironmentFile().getAbsolutePath() + " does exists on " + getBasedir());
            }
        }
    }
}
