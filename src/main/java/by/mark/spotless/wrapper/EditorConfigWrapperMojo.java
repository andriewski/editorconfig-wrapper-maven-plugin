package by.mark.spotless.wrapper;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static by.mark.spotless.wrapper.IdeaWorkSpaceXmlSettings.FORMAT_ON_SAVE_OPTIONS;
import static by.mark.spotless.wrapper.IdeaWorkSpaceXmlSettings.OPTIMIZE_IMPORTS;
import static by.mark.spotless.wrapper.IdeaWorkSpaceXmlSettings.OPTIMIZE_ON_SAVE_OPTIONS;
import static by.mark.spotless.wrapper.IdeaWorkSpaceXmlSettings.REFORMAT_CODE;
import static java.util.Objects.requireNonNull;

@Mojo(name = "add", defaultPhase = LifecyclePhase.CLEAN)
public class EditorConfigWrapperMojo extends AbstractMojo {

    private final Log log = getLog();

    @Override
    public void execute() throws MojoExecutionException {
        try {
            copyEditorConfigIfNotTheSame();
            changeWorkSpaceIdeaFile();
        } catch (IOException e) {
            throw new MojoExecutionException("Something went wrong...", e);
        }
    }

    private void copyEditorConfigIfNotTheSame() throws IOException {
        Path projectEditorConfigPath = Paths.get("./.editorconfig");
        ClassLoader classLoader = this.getClass()
                .getClassLoader();

        byte[] editorConfigBytes;

        try (InputStream is = requireNonNull(classLoader.getResourceAsStream(".editorconfig"))) {
            editorConfigBytes = is.readAllBytes();
        }

        if (!Files.exists(projectEditorConfigPath) || !filesAreIdentical(projectEditorConfigPath, editorConfigBytes)) {
            Files.write(projectEditorConfigPath, editorConfigBytes);
            log.info("new .editorconfig file was wrote");
        } else {
            log.info(".editorconfig is up to date");
        }
    }

    private void changeWorkSpaceIdeaFile() throws IOException {
        Path workspaceXml = Paths.get("./.idea/workspace.xml");

        if (!Files.exists(workspaceXml)) {
            throw new IllegalStateException("No workspace.xml in .idea folder");
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
            throw new IllegalStateException("Invalid workspace.xml in .idea folder. Empty content");
        }
        if (twoPropertiesCounter.get() > 2) {
            throw new IllegalStateException(
                    "Invalid workspace.xml in .idea folder. More than 2 of " + OPTIMIZE_ON_SAVE_OPTIONS + ", " + FORMAT_ON_SAVE_OPTIONS);
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
            throw new IllegalStateException("Invalid workspace.xml in .idea folder. Invalid structure");
        }

        int indexToAddData = i + 1;

        xmlLines.add(indexToAddData, OPTIMIZE_IMPORTS);
        xmlLines.add(indexToAddData, REFORMAT_CODE);

        Files.write(workspaceXml, xmlLines);

        log.info("new data was added to workspace.xml");
    }

    public static boolean filesAreIdentical(Path projectEditorConfigPath, byte[] editorConfigBytes) throws IOException {
        try (BufferedInputStream projectEditorConfigIs = new BufferedInputStream(
                new FileInputStream(projectEditorConfigPath.toFile()))) {
            for (int i = 0, projectByte; i < editorConfigBytes.length; i++) {
                int editorConfigByte = editorConfigBytes[i];
                projectByte = projectEditorConfigIs.read();

                if (editorConfigByte != projectByte) {
                    return false;
                }
            }

            return projectEditorConfigIs.read() == -1;
        }
    }
}
