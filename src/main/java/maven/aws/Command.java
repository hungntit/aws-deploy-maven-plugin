package maven.aws;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Command {

    public enum ClasspathCommandType {
        bash,
        sh
    }
    private String executable;

    private List<String> commandArgs;
    private File workingDirectory;
    private boolean ignoreExecuteFailure;

    public Command(File workingDirectory, boolean ignoreExecuteFailure, String executable, List<String> commandArgs) {
        this.executable = executable;
        this.commandArgs = commandArgs;
        this.workingDirectory = workingDirectory;
        this.ignoreExecuteFailure = ignoreExecuteFailure;
    }

    public Command(File workingDirectory, String executable, List<String> commandArgs) {
        this(workingDirectory, false, executable, commandArgs);
    }

    public Command(File workingDirectory, String executable, String... args) {
        this(workingDirectory, false, executable, args == null ? Collections.<String>emptyList() : Arrays.asList(args));
    }

    public static String toCommandString(String executable, List<String> commandArgs) {
        final StringBuilder sb = new StringBuilder(executable);
        for (String arg : commandArgs) {
            if (arg != null) {
                sb.append(" ").append(arg);
            }
        }
        return sb.toString();
    }

    public static Command convertClasspathToShCommand(Command classpathCommand) throws MojoExecutionException {
        return convertClasspathToShCommand(classpathCommand, ClasspathCommandType.sh);
    }

    public static Command convertClasspathToShCommand(Command classpathCommand, ClasspathCommandType type ) throws MojoExecutionException {
        String path = classpathCommand.getExecutable();
        boolean isClassPath = path.startsWith("classpath:");
        if (isClassPath) {
            FileOutputStream fos = null;
            try {
                File scriptFile = File.createTempFile("bash", ".sh");
                InputStream in = Command.class.getClassLoader().getResourceAsStream(path.split(":", 2)[1]);
                fos = new FileOutputStream(scriptFile);
                IOUtil.copy(in, fos);
                List<String> args = new ArrayList<String>();
                args.add(scriptFile.getAbsolutePath());
                args.addAll(classpathCommand.commandArgs);
                return new Command(classpathCommand.getWorkingDirectory(), classpathCommand.isIgnoreExecuteFailure(), type.name(), args);
            } catch (IOException ex) {
                throw new MojoExecutionException(path + ": file does not exist");
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                    }
                }
            }
        } else {
            throw new MojoExecutionException(classpathCommand.toCommandString() + " is not classpath");
        }
    }

    public String getExecutable() {
        return executable;
    }

    public void setExecutable(String executable) {
        this.executable = executable;
    }

    public List<String> getCommandArgs() {
        return commandArgs;
    }

    public void setCommandArgs(List<String> commandArgs) {
        this.commandArgs = commandArgs;
    }

    public File getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public boolean isIgnoreExecuteFailure() {
        return ignoreExecuteFailure;
    }

    public void setIgnoreExecuteFailure(boolean ignoreExecuteFailure) {
        this.ignoreExecuteFailure = ignoreExecuteFailure;
    }

    public String toCommandString() {
        return toCommandString(this.executable, this.commandArgs);
    }

    @Override
    public String toString() {
        return "Command{" +
                "executable='" + executable + '\'' +
                ", commandArgs=" + commandArgs +
                ", workingDirectory=" + workingDirectory +
                ", ignoreExecuteFailure=" + ignoreExecuteFailure +
                '}';
    }
}
