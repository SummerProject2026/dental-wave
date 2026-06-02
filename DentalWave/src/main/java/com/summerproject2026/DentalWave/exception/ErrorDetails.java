import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Returns a 404 error when a requested resource is not found
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}