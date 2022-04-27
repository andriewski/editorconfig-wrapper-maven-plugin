package by.mark.editorconfig.wrapper;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static by.mark.editorconfig.wrapper.IdeaWorkSpaceXmlSettings.FORMAT_ON_SAVE_OPTIONS;
import static by.mark.editorconfig.wrapper.IdeaWorkSpaceXmlSettings.OPTIMIZE_IMPORTS;
import static by.mark.editorconfig.wrapper.IdeaWorkSpaceXmlSettings.OPTIMIZE_ON_SAVE_OPTIONS;
import static by.mark.editorconfig.wrapper.IdeaWorkSpaceXmlSettings.REFORMAT_CODE;
import static java.util.Objects.requireNonNull;

@Mojo(name = "add", defaultPhase = LifecyclePhase.CLEAN)
public class EditorConfigWrapperMojo extends AbstractMojo {

    private final Log log = getLog();

    @Component
    private MavenProject mavenProject;

    @Override
    public void execute() throws MojoExecutionException {
        Path projectRootPath = getRootMavenProject(mavenProject)
                .getBasedir()
                .toPath();

        try {
            copyEditorConfigIfNotTheSame(projectRootPath);
            changeWorkSpaceIdeaFile(projectRootPath);
        } catch (IOException e) {
            throw new MojoExecutionException("Something went wrong...", e);
        }
    }

    private void copyEditorConfigIfNotTheSame(Path projectRootPath) throws IOException {
        Path projectEditorConfigPath = projectRootPath.resolve(".editorconfig");

        if (Files.exists(projectEditorConfigPath) && editorConfigHasNotChanged(projectEditorConfigPath)) {
            log.info(".editorconfig is up to date");
            return;
        }

        try (InputStream libraryEditorConfigIs = libraryEditorConfigStream()) {
            Files.copy(libraryEditorConfigIs, projectEditorConfigPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("new .editorconfig file was wrote");
        }
    }

    private void changeWorkSpaceIdeaFile(Path projectRootPath) throws IOException {
        Path workspaceXml = projectRootPath.resolve(".idea/workspace.xml");

        if (!Files.exists(workspaceXml)) {
            throw new IllegalStateException("No workspace.xml in .idea folder " + workspaceXml);
        }

        AtomicInteger twoPropertiesCounter = new AtomicInteger();

        List<String> xmlLines;
        try (Stream<String> lines = Files.lines(workspaceXml)) {
            xmlLines = lines.map(line -> {
                        if (line.contains(OPTIMIZE_ON_SAVE_OPTIONS)) {
                            twoPropertiesCounter.incrementAndGet();
                        }
                        if (line.contains(FORMAT_ON_SAVE_OPTIONS)) {
                            twoPropertiesCounter.incrementAndGet();
                        }
                        return line;
                    })
                    .collect(Collectors.toList());
        }

        if (xmlLines.isEmpty()) {
            throw new IllegalStateException("Invalid workspace.xml in .idea folder. Empty content " + workspaceXml);
        }
        if (twoPropertiesCounter.get() == 2) {
            log.info("workspace.xml is up to date");
            return;
        }

        writeDataToXml(workspaceXml, xmlLines);
    }

    private void writeDataToXml(Path workspaceXml, List<String> xmlLines) throws IOException {
        int i = xmlLines.size() - 1;
        for (; i >= 0; i--) {
            String line = xmlLines.get(i);
            boolean componentEnded = line.contains("</component>");

            if (componentEnded) {
                break;
            }
        }

        if (i <= 0 || i == xmlLines.size() - 1) {
            throw new IllegalStateException("Invalid workspace.xml in .idea folder. Invalid structure " + workspaceXml);
        }

        int indexToAddData = i + 1;

        xmlLines.add(indexToAddData, OPTIMIZE_IMPORTS);
        xmlLines.add(indexToAddData, REFORMAT_CODE);

        Files.write(workspaceXml, xmlLines);

        log.info("new data was added to workspace.xml");
    }

    public boolean editorConfigHasNotChanged(Path projectEditorConfigPath) throws IOException {
        try (InputStream projectEditorConfigIs = new BufferedInputStream(new FileInputStream(projectEditorConfigPath.toFile()));
                InputStream libraryEditorConfigIs = libraryEditorConfigStream()) {

            int projectEditorConfigByte;
            while ((projectEditorConfigByte = projectEditorConfigIs.read()) != -1) {
                if (projectEditorConfigByte != libraryEditorConfigIs.read()) {
                    return false;
                }
            }

            return libraryEditorConfigIs.read() == -1;
        }
    }

    private InputStream libraryEditorConfigStream() {
        ClassLoader classLoader = this.getClass()
                .getClassLoader();

        return new BufferedInputStream(requireNonNull(classLoader.getResourceAsStream(".editorconfig")));
    }

    private MavenProject getRootMavenProject(MavenProject mavenProject) {
        MavenProject parent = mavenProject.getParent();

        return parent == null || parent.getBasedir() == null
                ? mavenProject
                : getRootMavenProject(parent);
    }
}
