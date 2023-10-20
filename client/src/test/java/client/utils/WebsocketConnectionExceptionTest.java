package client.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class WebsocketConnectionExceptionTest {

    @Test
    void testEmptyConstructor() {
        WebsocketConnectionException exception = new WebsocketConnectionException();
        Assertions.assertNotNull(exception);
    }

    @Test
    void testStringConstructor() {
        String str = "exception";
        WebsocketConnectionException exception = new WebsocketConnectionException(str);
        Assertions.assertNotNull(exception);
        Assertions.assertEquals(str, exception.getMessage());
    }

    @Test
    void testExceptionConstructor() {
        NullPointerException npe = new NullPointerException();
        WebsocketConnectionException exception = new WebsocketConnectionException(npe);
        Assertions.assertNotNull(exception);
        Assertions.assertEquals(npe, exception.getCause());
    }
}
