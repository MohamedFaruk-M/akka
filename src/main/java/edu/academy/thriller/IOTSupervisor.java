package edu.academy.thriller;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class IOTSupervisor extends AbstractBehavior<Void> {

    public static Behavior<Void> create() {
        return Behaviors.setup(IOTSupervisor::new);
    }

    public IOTSupervisor(ActorContext<Void> context) {
        super(context);
        getContext().getLog().info("IOT Application started....");
    }

    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder()
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<Void> onPostStop() {
        getContext().getLog().error("~~~~~~~~ IOT Application stopped... ~~~~~~");
        return this;
    }
}
