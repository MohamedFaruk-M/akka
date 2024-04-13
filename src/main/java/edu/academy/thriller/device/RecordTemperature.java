package edu.academy.thriller.device;

import akka.actor.typed.ActorRef;

public class RecordTemperature implements Device.Command {

    public final long requestId;
    public final double value;
    public final ActorRef<TemperatureRecorded> replyTo;

    public RecordTemperature(long requestId, double value, ActorRef<TemperatureRecorded> replyTo) {
        this.requestId = requestId;
        this.value = value;
        this.replyTo = replyTo;
    }
}
