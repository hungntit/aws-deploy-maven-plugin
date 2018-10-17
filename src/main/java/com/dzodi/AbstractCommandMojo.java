package com.dzodi;

import org.apache.commons.exec.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.exec.ExecMojo;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;


public abstract class AbstractCommandMojo extends ExecMojo {

    @Parameter(
            property = "exec.outputFile"
    )
    private File outputFile;
    @Parameter(
            property = "exec.workingdir"
    )
    private File workingDirectory;

    @Parameter(
            readonly = true,
            required = true,
            defaultValue = "${basedir}"
    )
    private File basedir;

    @Parameter
    private List<String> cloneFiles;

    @Parameter
    private File cloneFileTargetedDir;

    @Parameter
    private Map<String, String> environmentVariables = new HashMap<>();

    @Parameter
    private File environmentScript = null;


    @Parameter
    private int[] successCodes;

    static String findExecutable(String executable, List<String> paths) {
        File f = null;
        Iterator var3 = paths.iterator();

        while (var3.hasNext()) {
            String path = (String) var3.next();
            f = new File(path, executable);
            if (!OS.isFamilyWindows() && f.isFile()) {
                break;
            }

            Iterator var5 = getExecutableExtensions().iterator();

            while (var5.hasNext()) {
                String extension = (String) var5.next();
                f = new File(path, executable + extension);
                if (f.isFile()) {
                    return f != null && f.exists() ? f.getAbsolutePath() : null;
                }
            }
        }

        return f != null && f.exists() ? f.getAbsolutePath() : null;
    }

    private static boolean hasNativeExtension(String exec) {
        String lowerCase = exec.toLowerCase();
        return lowerCase.endsWith(".exe") || lowerCase.endsWith(".com");
    }

    private static boolean hasExecutableExtension(String exec) {
        String lowerCase = exec.toLowerCase();
        Iterator var2 = getExecutableExtensions().iterator();

        String ext;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            ext = (String) var2.next();
        } while (!lowerCase.endsWith(ext));

        return true;
    }

    private static List<String> getExecutableExtensions() {
        String pathExt = System.getenv("PATHEXT");
        return pathExt == null ? Arrays.asList(".bat", ".cmd") : Arrays.asList(StringUtils.split(pathExt.toLowerCase(), File.pathSeparator));
    }

    public File getWorkingDirectory() {
        return workingDirectory;
    }

    public void prepairFilesToWorkingDir() throws MojoExecutionException {

        if (cloneFiles != null) {
            if (cloneFileTargetedDir == null) {
                this.cloneFileTargetedDir = this.getWorkingDirectory();
            }
            for (String path : cloneFiles) {
                this.executeCommand(new Command(this.getWorkingDirectory(),
                                "cp", "-rp", path, this.cloneFileTargetedDir.getAbsolutePath() + "/"),
                        new HashMap<String, String>());
            }
        }

    }

    public abstract List<Command> getCommands() throws MojoExecutionException;

    public void verify() throws MojoExecutionException {

    }

    public File getBasedir() {
        return basedir;
    }

    public File getTargetDir() {
        return new File(basedir, "target");
    }

    protected void executeCommand(Command command) throws MojoExecutionException {
        executeCommand(command, new HashMap<String, String>());
    }

    protected void executeCommand(Command command, Map<String, String> environments) throws MojoExecutionException {
        this.getLog().warn("Run command: " + command);

        CommandLine commandLine = this.getExecutablePath(command.getExecutable(), environments, command.getWorkingDirectory());
        String[] args = command.getCommandArgs().toArray(new String[command.getCommandArgs().size()]);
        commandLine.addArguments(args, false);
        Executor exec = new DefaultExecutor();

        exec.setWorkingDirectory(command.getWorkingDirectory());
        try {
            int resultCode;
            if (this.outputFile != null) {
                if (!this.outputFile.getParentFile().exists() && !this.outputFile.getParentFile().mkdirs()) {
                    this.getLog().warn("Could not create non existing parent directories for log file: " + this.outputFile);
                }

                FileOutputStream outputStream = null;

                try {
                    outputStream = new FileOutputStream(this.outputFile);
                    resultCode = this.executeCommandLine(exec, commandLine, environments, (FileOutputStream) outputStream);
                } finally {
                    IOUtil.close(outputStream);
                }
            } else {
                resultCode = this.executeCommandLine(exec, commandLine, environments, System.out, System.err);
            }

            if (this.isResultCodeAFailure(resultCode)) {
                String message = "Result of " + commandLine.toString() + " execution is: '" + resultCode + "'.";
                this.getLog().error(message);
                throw new MojoExecutionException(message);
            }
        } catch (ExecuteException ex) {
            this.getLog().error("Command execution failed.", ex);
            throw new MojoExecutionException("Command execution failed.", ex);
        } catch (IOException ex) {
            this.getLog().error("Command execution failed.", ex);
            throw new MojoExecutionException("Command execution failed.", ex);
        }
    }

    public void execute()
            throws MojoExecutionException {

        if (this.isSkip()) {
            this.getLog().info("skipping execute as per configuration");
        } else if (this.basedir == null) {
            throw new IllegalStateException("basedir is null. Should not be possible.");
        } else {
            this.handleWorkingDirectory();
            Map<String, String> environments = this.handleSystemEnvVariables();
            verify();
            prepairFilesToWorkingDir();
            for (Command command : getCommands()) {
                try {
                    executeCommand(command, environments);
                } catch (MojoExecutionException ex) {
                    if (!command.isIgnoreExecuteFailure()) {
                        throw ex;
                    }
                }
            }
        }
    }

    private void fillSuccessCodes(Executor exec) {
        if (this.successCodes != null && this.successCodes.length > 0) {
            exec.setExitValues(this.successCodes);
        }

    }

    boolean isResultCodeAFailure(int result) {
        if (this.successCodes != null && this.successCodes.length != 0) {
            int[] var2 = this.successCodes;
            int var3 = var2.length;

            for (int var4 = 0; var4 < var3; ++var4) {
                int successCode = var2[var4];
                if (successCode == result) {
                    return false;
                }
            }

            return true;
        } else {
            return result != 0;
        }
    }

    private void handleWorkingDirectory() throws MojoExecutionException {
        if (this.workingDirectory == null) {
            this.workingDirectory = this.basedir;
        }

        if (!this.workingDirectory.exists()) {
            this.getLog().debug("Making working directory '" + this.workingDirectory.getAbsolutePath() + "'.");
            if (!this.workingDirectory.mkdirs()) {
                throw new MojoExecutionException("Could not make working directory: '" + this.workingDirectory.getAbsolutePath() + "'");
            }
        }
    }

    private Map<String, String> handleSystemEnvVariables() throws MojoExecutionException {
        HashMap enviro = new HashMap();

        Iterator var3;
        try {
            Properties systemEnvVars = CommandLineUtils.getSystemEnvVars();
            var3 = systemEnvVars.entrySet().iterator();

            while (var3.hasNext()) {
                Map.Entry<?, ?> entry = (Map.Entry) var3.next();
                enviro.put((String) entry.getKey(), (String) entry.getValue());
            }
        } catch (IOException var5) {
            this.getLog().error("Could not assign default system enviroment variables.", var5);
        }

        if (this.environmentVariables != null) {
            enviro.putAll(this.environmentVariables);
        }

        if (this.environmentScript != null) {
            this.getLog().info("Pick up external environment script: " + this.environmentScript);
            Map<String, String> envVarsFromScript = this.createEnvs(this.environmentScript);
            if (envVarsFromScript != null) {
                enviro.putAll(envVarsFromScript);
            }
        }

        if (this.getLog().isDebugEnabled()) {
            Set<String> keys = new TreeSet();
            keys.addAll(enviro.keySet());
            var3 = keys.iterator();

            while (var3.hasNext()) {
                String key = (String) var3.next();
                this.getLog().debug("env: " + key + "=" + (String) enviro.get(key));
            }
        }

        return enviro;
    }

    CommandLine getExecutablePath(String executable, Map<String, String> enviro, File dir) throws MojoExecutionException {
        File execFile = new File(executable);
        String exec = null;
        if (execFile.isFile()) {
            this.getLog().debug("Toolchains are ignored, 'executable' parameter is set to " + executable);
            exec = execFile.getAbsolutePath();
        }

        if (exec == null) {
            if (OS.isFamilyWindows()) {
                List<String> paths = this.getExecutablePaths(enviro);
                paths.add(0, dir.getAbsolutePath());
                exec = findExecutable(executable, paths);
            }
        }

        if (exec == null) {
            exec = executable;
        }

        CommandLine toRet;
        if (OS.isFamilyWindows() && !hasNativeExtension(exec) && hasExecutableExtension(exec)) {
            String comSpec = System.getenv("ComSpec");
            toRet = new CommandLine(comSpec == null ? "cmd" : comSpec);
            toRet.addArgument("/c");
            toRet.addArgument(exec);
        } else {
            toRet = new CommandLine(exec);
        }

        return toRet;
    }

    private List<String> getExecutablePaths(Map<String, String> enviro) {
        List<String> paths = new ArrayList();
        paths.add("");
        String path = (String) enviro.get("PATH");
        if (path != null) {
            paths.addAll(Arrays.asList(StringUtils.split(path, File.pathSeparator)));
        }

        return paths;
    }

    public MavenProject getMavenProject() {
        return (MavenProject) this.getPluginContext().get("project");
    }

    public Properties getFullProperties() {
        Properties properties = new Properties();
        MavenProject mavenProject = getMavenProject();
        properties.put("project.name", mavenProject.getName());
        properties.put("project.artifactId", mavenProject.getArtifactId());
        properties.put("project.groupId", mavenProject.getGroupId());
        properties.put("project.version", mavenProject.getVersion());
        properties.put("project.basedir", mavenProject.getBasedir().getAbsolutePath());
        properties.putAll(mavenProject.getProperties());
        properties.putAll(System.getProperties());
        return properties;
    }

}

