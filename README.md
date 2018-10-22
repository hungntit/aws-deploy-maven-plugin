# aws-deploy-maven-plugin

## Deploy Cloudformation stack:
- Add the folder /cloudformation to your source code
- Put the template.yml file to this , put the directory environments for parameters
Add below config to your pom

            <build>
                <plugins>
                    <plugin>
                        <groupId>com.dzodi</groupId>
                        <artifactId>aws-deploy-maven-plugin</artifactId>
                        <version>1.0.3</version>
                        <executions>
                            <execution>
                                <id>deploy</id>
                                <phase>install</phase>
                                <goals>
                                    <goal>cloudformation:deploy</goal>
                                </goals>
                            </execution>
                        </executions>
                    </plugin>
                </plugins>
            </build>
       
    
## Deploy 
