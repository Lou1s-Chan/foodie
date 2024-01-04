package ie.foodie.SSE;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
public class SSEController {

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    @CrossOrigin(origins = "http://host.docker.internal:3000")
    @GetMapping("/order_stream")
    public SseEmitter stream() {
        SseEmitter emitter = createEmitter();
        this.emitters.add(emitter);

        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> this.emitters.remove(emitter));

        return emitter;
    }
    public SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event().comment("keep-alive"));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }, 0, 15, TimeUnit.SECONDS); // Send a keep-alive every 15 seconds
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
