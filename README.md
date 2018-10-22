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
 - The stack will be created with ${project.name} name, you can change this name by add the `aws.stack.name` property
 - To deploy, we just run `mvn clean install -Daws.region=us-east-1 -Daws.ecr.account.id=$(aws sts get-caller-identity --output text --query 'Account') `
## Deploy Docker Image to ECS
