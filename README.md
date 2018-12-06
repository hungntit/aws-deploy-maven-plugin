# aws-deploy-maven-plugin

## Deploy Cloudformation stack:
- Add the folder /cloudformation to your source code
- Put the  /cloudformation/template.yml for cloudformation template, we can the template file by changing the `aws.stack.template.name` property  
- (OPTIONAL)  Create the file /cloudformation/environments/dev.json for cloudformation parameters file if you want.
- Add below config to your pom

            <build>
                <plugins>
                    <plugin>
                        <groupId>com.dzodi</groupId>
                        <artifactId>aws-deploy-maven-plugin</artifactId>
                        <version>1.0.8</version>
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
 - The stack will be created with ${project.name} name, you can change this name by add the `aws.stack.name` property
 - To deploy, we just run `mvn clean install -Daws.region=us-east-1 -Daws.ecr.account.id=$(aws sts get-caller-identity --output text --query 'Account') `
 - To deploy with parameter, using `-Daws.stack.env` property in your maven command. If your want to prefix your stack by `${aws.stack.env}` (in this case, the stack will be created with name `${aws.stack.env}-${project.name}`) ,set the property `-Daws.stack.prefix.by.env=true`
 
## Deploy Docker Image to ECS
- Add the `Dockerfile` to your `${baseDir}`
- (OPTIONAL) Set the `exec.workingDir` property for docker working dir. 

  Give an example, if you set

            <properties>
                <exec.workingdir>${basedir}/target</exec.workingdir>
            </properties>

In this case, The `Dockerfile` will be clone to `${baseDir}/target/` directory, on that /target directory we have a jar file, so we can add jar file to docker image. Please see the examples to have more detail

- Add below configuration to your pom

                   <plugin>
                        <groupId>com.dzodi</groupId>
                        <artifactId>aws-deploy-maven-plugin</artifactId>
                        <version>1.0.8</version>
                        <executions>
                            <execution>
                                <id>deploy</id>
                                <phase>install</phase>
                                <goals>
                                    <goal>ecr:deploy</goal>
                                </goals>
                            </execution>
                        </executions>
                   </plugin>

## Deploy Docker Image to ECS then create ECS Service

                   <plugin>
                        <groupId>com.dzodi</groupId>
                        <artifactId>aws-deploy-maven-plugin</artifactId>
                        <version>1.0.8</version>
                        <executions>
                            <execution>
                                <id>deploy</id>
                                <phase>install</phase>
                                <goals>
                                    <goal>ecr:deploy</goal>
                                    <goal>cloudformation:deploy</goal>
                                </goals>
                            </execution>
                        </executions>
                   </plugin>

To more detail, please view on examples
