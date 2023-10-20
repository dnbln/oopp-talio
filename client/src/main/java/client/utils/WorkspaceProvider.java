package client.utils;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import jakarta.annotation.PreDestroy;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The provider for the workspace singleton.
 */
public class WorkspaceProvider implements Provider<Workspace> {
    private final Path filePath;
    private final Path workspaceDir;
    private Workspace theInstance = new Workspace();

    /**
     * Constructor for the WorkspaceProvider. It is annotated with @Inject which means that this constructor is used
     * when the injector is asked to get an instance of this class.
     *
     * @param filename     The name of the file to store the singleton in.
     * @param workspaceDir The name of the directory to store the singleton in.
     */
    @Inject
    public WorkspaceProvider(@Named("workspaceFilename") final String filename,
                             @Named("workspaceDir") final String workspaceDir) {
        // Get the user's home directory
        String userHome = System.getProperty("user.home");

        // Create a subdirectory for your application's workspace
        this.workspaceDir = Paths.get(userHome, workspaceDir);
        this.filePath = this.workspaceDir.resolve(filename);

        Runtime.getRuntime().addShutdownHook(new Thread(this::saveWorkspaceToFile));
    }

    /**
     * Gets the singleton from a file.
     *
     * @return The singleton.
     */
    @Override
    public Workspace get() {
        if (!Files.exists(this.filePath)) {
            return this.theInstance;
        }
        if (!this.theInstance.equals(new Workspace())) {
            return this.theInstance;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(this.filePath.toFile()))) {
            this.theInstance = (Workspace) ois.readObject();
            return this.theInstance;
        } catch (final IOException | ClassNotFoundException e) {
            throw new RuntimeException("Error reading file: %s".formatted(this.filePath.toAbsolutePath()), e);
        }
    }

    /**
     * Saves the workspace to a file.
     * This method is called when the application is closed.
     */
    @PreDestroy
    public void saveWorkspaceToFile() {
        try {
            if (!Files.exists(this.workspaceDir)) {
                Files.createDirectory(this.workspaceDir);
            }
            this.writeObject();
        } catch (final IOException e) {
            throw new RuntimeException("Error creating file: %s".formatted(this.filePath), e);
        }
    }

    private void writeObject() {
        try (ObjectOutput oos = new ObjectOutputStream(new FileOutputStream(this.filePath.toFile()))) {
            oos.writeObject(this.theInstance);
        } catch (final IOException e) {
            throw new RuntimeException("Error writing file: %s".formatted(this.filePath.toAbsolutePath()), e);
        }
    }
}
