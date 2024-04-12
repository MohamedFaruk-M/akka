package edu.academy.thriller.message;

import akka.actor.typed.ActorRef;

public class ReadTemperature implements Command {

    public final long requestId;

    public final ActorRef<RespondTemperature> replyTo;

    public ReadTemperature(long requestId, ActorRef<RespondTemperature> replyTo) {
        this.requestId = requestId;
        this.replyTo = replyTo;
    }
}
