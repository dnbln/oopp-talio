import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import server.SomeController;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SomeControllerTest {

    @Test
    void testReturnIndex() {
        SomeController controller = new SomeController();
        HttpStatusCode expected = HttpStatus.I_AM_A_TEAPOT;
        final HttpStatusCode actual = controller.index().getStatusCode();
        assertEquals(expected, actual);
    }
}
