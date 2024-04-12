package edu.academy.thriller.component;

import java.util.Optional;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.japi.function.Function;
import edu.academy.thriller.message.Command;
import edu.academy.thriller.message.ReadTemperature;
import edu.academy.thriller.message.RecordTemperature;
import edu.academy.thriller.message.RespondTemperature;
import edu.academy.thriller.message.TemperatureRecorded;

public class Device extends AbstractBehavior<Command> {

    private final String deviceId;
    private final String groupId;

    private Optional<Double> lastTemperatureReading = Optional.empty();

    public Device(ActorContext<Command> context, String deviceId, String groupId) {
        super(context);
        this.deviceId = deviceId;
        this.groupId = groupId;

        getContext().getLog().info("Device actor {}-{} started successfully :-)", deviceId, groupId);
    }

    public static Behavior<Command> create(String deviceId, String groupId) {
        return Behaviors.setup(context -> new Device(context, deviceId, groupId));
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(ReadTemperature.class, this::onReadTemperature)
                .onMessage(RecordTemperature.class, this::onRecordTemperature)
                .onSignal(PostStop.class, onPostStop())
                .build();
    }

    private Behavior<Command> onReadTemperature(ReadTemperature read) {
        read.replyTo.tell(new RespondTemperature(read.requestId, lastTemperatureReading));
        return this;
    }

    private Behavior<Command> onRecordTemperature(RecordTemperature record) {
        getContext().getLog().info("Recording temperature value {} for request {}", record.value, record.requestId);
        lastTemperatureReading = Optional.of(record.value);
        record.replyTo.tell(new TemperatureRecorded(record.requestId));
        return this;
    }

    private Function<PostStop, Behavior<Command>> onPostStop() {
        getContext().getLog().error("Device actor {}-{} stopped unexpectedly :-(", deviceId, groupId);
        return null;
    }
}
