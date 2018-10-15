package com.dzodi;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropertiesUtils {
    public static String resolveValueWithEnvVars(Properties properties, String value) {
        if (null == value) {
            return null;
        }

        Pattern p = Pattern.compile("\\@\\{([a-zA-Z0-9\\.]+)\\}|\\@([a-zA-Z0-9\\.]+)");
        Matcher m = p.matcher(value);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String envVarName = null == m.group(1) ? m.group(2) : m.group(1);
            String envVarValue = System.getenv(envVarName);
            String propertyValue = properties.getProperty(envVarName);

            m.appendReplacement(sb,
                    null == envVarValue ? (null == propertyValue ? "" : Matcher.quoteReplacement(propertyValue)) : Matcher.quoteReplacement(envVarValue));


        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static void handlePropertyFile(Properties properties, String filePath) throws MojoExecutionException {
        Path path = Paths.get(filePath);
        Charset charset = StandardCharsets.UTF_8;
        System.out.println("filePath"+ filePath);
        String content;
        try {

            content = new String(Files.readAllBytes(path), charset);
            String newContent = PropertiesUtils.resolveValueWithEnvVars(properties, content);
            Files.write(path, newContent.getBytes(charset));
        } catch (IOException e) {
            throw new MojoExecutionException("Can not read file", e);
        }

    }
}
