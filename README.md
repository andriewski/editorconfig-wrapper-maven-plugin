Plugin that can be used to set default .editorconfig among all projects and update it from single place

It adds to .idea/workspace.xml file "Optimize imports" and "Reformat code" on save action

.idea/workspace.xml is generally ignored by .gitignore file, so it won't make any harm

This part of adding properties to workspace.xml can be deleted if Jetbrains team resolves
issue https://youtrack.jetbrains.com/issue/IDEA-276784/Store-'Actions-on-Save'-Settings-in-VCS-(as-xml-file-in-.idea-fo

Common configuration

    <build>
        <plugins>
            <plugin>
                <groupId>by.mark</groupId>
                <artifactId>editorconfig-wrapper-maven-plugin</artifactId>
                <version>0.0.5-SNAPSHOT</version>
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
