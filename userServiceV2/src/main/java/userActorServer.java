import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.Http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import akka.http.javadsl.model.ws.Message;
import akka.http.javadsl.model.ws.TextMessage;

import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.server.Directives;
import akka.http.javadsl.server.Route;
import akka.stream.Materializer;
import ie.foodie.messages.ClientMessage;
import ie.foodie.messages.models.CustomerV2;
import org.json.JSONObject;
import services.UserMaster;

import akka.stream.javadsl.Flow;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletionStage;


public class userActorServer {
    public static Route createRoute(ActorSystem system) {
        return Directives.concat(
                Directives.path("foodie", () -> Directives.handleWebSocketMessages(foodieFlow(system)))
        );
    }
    public static void main(String[] args) throws UnknownHostException {
        final ActorSystem userSystem = ActorSystem.create("user-system");
        Route wsRoute = createRoute(userSystem);
        Http.get(userSystem).newServerAt("localhost", 4321).bind(wsRoute);
        System.out.println("Server started at " + InetAddress.getLocalHost().getHostAddress());
    }
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static Flow<Message, Message, NotUsed> foodieFlow(ActorSystem system) {
        return Flow.<Message>create()
                .mapConcat(message -> {
                    if (message.isText()) {
                        ActorRef userMaster = system.actorOf(UserMaster.props(), "user-service-master");
                        String text = ((TextMessage) message).getStrictText();
                        JSONObject jsonObject = new JSONObject(message);
                        String msgType = jsonObject.getString("msgType");
                        switch (msgType) {
                            case "CustomerDetails":
                                CustomerV2 customerV2 = objectMapper.readValue(text, CustomerV2.class);
                                userMaster.tell(customerV2, ActorRef.noSender());
                                break;
//                                return Collections.singletonList(TextMessage.create("Server received customer details of " + customerV2.getCustomerId() + "."));
                            case "selectRestaurant":
                            case "selectFood":
                            case "continueOrder":
                            case "payMethod":
                            case "payWithSavedCard":
                            case "useNewCard":
                                ClientMessage clientMessage = objectMapper.readValue(text, ClientMessage.class);
                                system.actorSelection("/user/user-service-slave-" + jsonObject.getString("uuid")).tell(clientMessage, ActorRef.noSender());
//                                return Collections.singletonList(TextMessage.create("");
                                break;
                        }
                    } else { return Collections.emptyList(); }
                    return null;
                });
    }
//
//    private static Flow<Message, Message, NotUsed> logoutFlow(ActorRef actorRef) {
//        return Flow.<Message>create()
//                .mapConcat(message -> {
//                    if (message.isText()) {
//                        String text = ((TextMessage) message).getStrictText();
//                        actorRef.tell(text, ActorRef.noSender());
//                        return Collections.singletonList(TextMessage.create("Customer " + text + " logout successfully!"));
//                    } else { return Collections.emptyList(); }
//                });
//    }

//    private static Flow<Message, Message, NotUsed> orderFlow(ActorRef actorRef) {
//        return Flow.<Message>create()
//                .mapConcat(message -> {
//                    if (message.isText()) {
//                        String text = ((TextMessage) message).getStrictText();
//                        try {
//                            CustomerV2 customerV2 = objectMapper.readValue(text, CustomerV2.class);
//                            String userUuid = customerV2.getUuid();
//                            actorRef.tell(userUuid, ActorRef.noSender());
//                            return Collections.singletonList(TextMessage.create("Customer " + customerV2.getCustomerId() + " logout successfully!"));
//                        } catch (JsonProcessingException e) {
//                            e.printStackTrace();
//                            return Collections.singletonList(TextMessage.create("Error processing user logout process"));
//                        }
//                    } else { return Collections.emptyList(); }
//                });
//    }
}
