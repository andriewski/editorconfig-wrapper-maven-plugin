package by.mark.spotless.wrapper;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.io.InputStream;

import static java.util.Objects.requireNonNull;

@Mojo(name = "check", defaultPhase = LifecyclePhase.TEST)
public class EditorConfigWrapperMojo extends AbstractMojo {

    @Component
    private MavenProject mavenProject;

    @Component
    private MavenSession mavenSession;

    @Component
    private BuildPluginManager pluginManager;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            copyEditorConfigIfNotTheSame();
            copyDefaultIdeaSettings();
        } catch (IOException e) {
            throw new MojoExecutionException("Something went wrong...", e);
        }
    }

    private void copyEditorConfigIfNotTheSame() throws IOException {
        try (InputStream is = requireNonNull(this.getClass()
                .getClassLoader()
                .getResourceAsStream(".editorconfig"))) {

            byte[] bytes = is.readAllBytes();

            // TODO implement the rest
        }
    }

    private void copyDefaultIdeaSettings() {

    }
}
