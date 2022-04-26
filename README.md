Plugin that can be used to set default .editorconfig among all projects and update if from single place

Also it added to .idea/workspace.xml file, which is generally ignored some tags to "Optimize imports" and "Reformat code" on save action

This part of adding properties to workspace.xml can be deleted if Jetbrains team resolves
issue https://youtrack.jetbrains.com/issue/IDEA-276784/Store-'Actions-on-Save'-Settings-in-VCS-(as-xml-file-in-.idea-fo

Common configuration

    <build>
        <plugins>
            <plugin>
                <groupId>by.mark</groupId>
                <artifactId>editorconfig-wrapper-maven-plugin</artifactId>
                <version>0.0.3-SNAPSHOT</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>add</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
