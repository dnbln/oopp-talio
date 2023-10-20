package server;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * The controller for the server.
 */
@Controller
@RequestMapping("/")
public class SomeController {

    /**
     * The mapping of root for get requests.
     * @return a response entity containing I_AM_A_TEAPOT.
     */
    @GetMapping("/")
    @ResponseBody
    public ResponseEntity<Void> index() {
        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).build();
    }
}
