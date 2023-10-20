package client.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class InvalidServerExceptionTest {

    @Test
    void testEmptyConstructor() {
        InvalidServerException exception = new InvalidServerException();
        Assertions.assertNotNull(exception);
    }

    @Test
    void testStringConstructor() {
        String str = "exception";
        InvalidServerException exception = new InvalidServerException(str);
        Assertions.assertNotNull(exception);
        Assertions.assertEquals(str, exception.getMessage());
    }

    @Test
    void testExceptionConstructor() {
        NullPointerException npe = new NullPointerException();
        InvalidServerException exception = new InvalidServerException(npe);
        Assertions.assertNotNull(exception);
        Assertions.assertEquals(npe, exception.getCause());
    }
}
