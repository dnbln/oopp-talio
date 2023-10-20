package client;

import com.google.inject.Injector;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.Builder;
import javafx.util.BuilderFactory;
import javafx.util.Callback;
import javafx.util.Pair;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Loads the FXML to the controllers.
 */
public class MyFXML {

    private final Injector injector;

    /**
     * The constructor for MyFXML.
     *
     * @param injector The injector used.
     */
    public MyFXML(final Injector injector) {
        this.injector = injector;
    }

    /**
     * Loads the FXML files to the controllers.
     *
     * @param <T>   The type of the object to be loaded.
     * @param ignoredC     The controller class to be loaded.
     * @param parts The parts that make up the package of the FXML files to be loaded (including the FXML file).
     *              E.g.: "client", "scenes", "Example.fxml"
     * @return A pair of the controller associated with the root object and the parent
     * (the object hierarchy from a FXML document).
     */
    @SuppressWarnings("ProhibitedExceptionThrown")
    public <T> Pair<T, Parent> load(final Class<T> ignoredC, final String... parts) {
        try {
            final var loader =
                    new FXMLLoader(getLocation(parts), null, null, new MyFactory(), StandardCharsets.UTF_8);
            final Parent parent = loader.load();
            final T ctrl = loader.getController();
            return new Pair<>(ctrl, parent);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static URL getLocation(final String... parts) {
        final var path = Path.of("", parts).toString();
        return MyFXML.class.getClassLoader().getResource(path);
    }

    private class MyFactory implements BuilderFactory, Callback<Class<?>, Object> {

        @Override
        @SuppressWarnings("rawtypes")
        public Builder<?> getBuilder(final Class<?> type) {
            return (Builder) () -> MyFXML.this.injector.getInstance(type);
        }

        @Override
        public Object call(final Class<?> param) {
            return MyFXML.this.injector.getInstance(param);
        }
    }
}
