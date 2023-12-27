package service.SSE;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
public class SSEController {

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/stream")
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter();
        this.emitters.add(emitter);

        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> this.emitters.remove(emitter));

        return emitter;
    }

    public void sendMessageToClients(String message) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(message, MediaType.APPLICATION_JSON);
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }
}
