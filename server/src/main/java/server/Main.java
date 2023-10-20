package server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SuppressWarnings({"checkstyle:FinalClass", "checkstyle:HideUtilityClassConstructor"})
@SpringBootApplication
@EntityScan(basePackages = {"commons", "server"})
public class Main {
    /**
     * The main method for the server application. From here, the spring application is run.
     * @param args The arguments passed to the main method.
     */
    public static void main(final String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
