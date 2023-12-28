package ie.foodie.actors;

import akka.actor.AbstractActor;
import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;

import java.time.Duration;

public abstract class FoodieActor extends AbstractActor {
    private static final SupervisorStrategy strategy =
            new OneForOneStrategy(
                    10,
                    Duration.ofMinutes(1),
                    DeciderBuilder.match(ArithmeticException.class, e -> SupervisorStrategy.resume())
                            .match(NullPointerException.class, e -> SupervisorStrategy.restart())
                            .match(IllegalArgumentException.class, e -> SupervisorStrategy.stop())
                            .matchAny(o -> SupervisorStrategy.escalate())
                            .build());

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }
}
