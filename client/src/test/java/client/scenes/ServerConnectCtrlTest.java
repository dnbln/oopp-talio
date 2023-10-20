package client.scenes;


import client.utils.InvalidServerException;
import client.utils.ServerUtilsInterface;

import client.utils.WebsocketConnectionException;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class ServerConnectCtrlTest {

    @Mock
    private MainCtrl mainCtrl;

    @Mock
    private ServerUtilsInterface server;

    @InjectMocks
    private ServerConnectCtrl serverConnectCtrl;

    @BeforeEach
    final void setUp() {
        MockitoAnnotations.openMocks(this);

        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MainCtrl.class).toInstance(mainCtrl);
                bind(ServerUtilsInterface.class).toInstance(server);
            }
        });

        serverConnectCtrl = injector.getInstance(ServerConnectCtrl.class);
    }

    @Test
    void validateTrueTest() throws Exception {

        Mockito.doNothing().when(server)
                .validateAndSetServer("http://localhost:8080");

        boolean result = serverConnectCtrl.isValid("http://localhost:8080");
        assertTrue(result);
    }

    @Test
    void validateFalseTest() throws Exception {

        Mockito.doThrow(InvalidServerException.class).when(server)
                .validateAndSetServer("false");

        ServerConnectCtrl aSpy = Mockito.spy(serverConnectCtrl);
        Mockito.doNothing().when(aSpy).showErrorMessage(
                Mockito.any(Exception.class),
                Mockito.any(String.class));

        boolean result = aSpy.isValid("false");

        Mockito.verify(server, Mockito.times(1))
                .validateAndSetServer(Mockito.eq("false"));

        Mockito.verify(aSpy, Mockito.times(1))
                .showErrorMessage(Mockito.any(Exception.class), Mockito.any(String.class));

        assertFalse(result);
    }

    @Test
    void validateInvalidServerExceptionTest() throws Exception {
        Mockito.doThrow(InvalidServerException.class).when(server)
                .validateAndSetServer("invalidServer");

        ServerConnectCtrl aSpy = Mockito.spy(serverConnectCtrl);
        Mockito.doNothing().when(aSpy).showErrorMessage(
                Mockito.any(InvalidServerException.class),
                Mockito.eq("The provided URL is not of a valid Talio server. "
                        + "Try another server."));

        boolean result = aSpy.isValid("invalidServer");

        Mockito.verify(server, Mockito.times(1))
                .validateAndSetServer(Mockito.eq("invalidServer"));

        Mockito.verify(aSpy, Mockito.times(1))
                .showErrorMessage(Mockito.any(Exception.class), Mockito.any(String.class));

        assertFalse(result);
    }

    @Test
    void validateURISyntaxExceptionTest() throws Exception {
        Mockito.doThrow(URISyntaxException.class).when(server)
                .validateAndSetServer("invalidURI");

        ServerConnectCtrl aSpy = Mockito.spy(serverConnectCtrl);
        Mockito.doNothing().when(aSpy).showErrorMessage(
                Mockito.any(URISyntaxException.class),
                Mockito.eq("The provided input was not a valid URL. " +
                        "Check your spelling and try again"));

        boolean result = aSpy.isValid("invalidURI");

        Mockito.verify(server, Mockito.times(1))
                .validateAndSetServer(Mockito.eq("invalidURI"));

        Mockito.verify(aSpy, Mockito.times(1))
                .showErrorMessage(Mockito.any(Exception.class), Mockito.any(String.class));

        assertFalse(result);
    }

    @Test
    void validateInterruptedExceptionTest() throws Exception {
        Mockito.doThrow(InterruptedException.class).when(server)
                .validateAndSetServer("interrupted");

        ServerConnectCtrl aSpy = Mockito.spy(serverConnectCtrl);
        Mockito.doNothing().when(aSpy).showErrorMessage(
                Mockito.any(InterruptedException.class),
                Mockito.eq("Something unexpected happened. " +
                        "If you are experiencing trouble, reload the application"));

        boolean result = aSpy.isValid("interrupted");

        Mockito.verify(server, Mockito.times(1))
                .validateAndSetServer(Mockito.eq("interrupted"));

        Mockito.verify(aSpy, Mockito.times(1))
                .showErrorMessage(Mockito.any(Exception.class), Mockito.any(String.class));

        assertFalse(result);
    }

    @Test
    void validateWebsocketConnectionExceptionTest() throws Exception {
        Mockito.doThrow(WebsocketConnectionException.class).when(server)
                .validateAndSetServer("websocketConnection");

        ServerConnectCtrl aSpy = Mockito.spy(serverConnectCtrl);
        Mockito.doNothing().when(aSpy).showErrorMessage(
                Mockito.any(WebsocketConnectionException.class),
                Mockito.eq("Could not connect to the Talio server. " +
                        "Check the spelling or your connection and try again."));

        boolean result = aSpy.isValid("websocketConnection");

        Mockito.verify(server, Mockito.times(1))
                .validateAndSetServer(Mockito.eq("websocketConnection"));

        Mockito.verify(aSpy, Mockito.times(1))
                .showErrorMessage(Mockito.any(Exception.class), Mockito.any(String.class));

        assertFalse(result);
    }

}
