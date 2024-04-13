package edu.academy.thriller.device;

import akka.actor.typed.ActorRef;

public class ReadTemperature implements Device.Command {

    public final long requestId;

    public final ActorRef<RespondTemperature> replyTo;

    public ReadTemperature(long requestId, ActorRef<RespondTemperature> replyTo) {
        this.requestId = requestId;
        this.replyTo = replyTo;
    }
}
