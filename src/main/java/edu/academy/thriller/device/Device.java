package edu.academy.thriller.device;

import java.util.Optional;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Device extends AbstractBehavior<Device.Command> {
    public interface Command {
    }

    private final String deviceId;

    private final String groupId;

    private Optional<Double> lastTemperatureReading = Optional.empty();

    public Device(ActorContext<Command> context, String groupId, String deviceId) {
        super(context);
        this.groupId = groupId;
        this.deviceId = deviceId;

        getContext().getLog().info("Device actor {}-{} started successfully :-)", groupId, deviceId);
    }

    public static Behavior<Command> create(String groupId, String deviceId) {
        return Behaviors.setup(context -> new Device(context, groupId, deviceId));
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(ReadTemperature.class, this::onReadTemperature)
                .onMessage(RecordTemperature.class, this::onRecordTemperature)
                .onSignal(PostStop.class, sgl -> onPostStop())
                .build();
    }

    private Behavior<Command> onReadTemperature(ReadTemperature read) {
        getContext().getLog().info("Reading present temperature value {} for request {}", lastTemperatureReading, read.requestId);
        read.replyTo.tell(new RespondTemperature(read.requestId, lastTemperatureReading));
        return this;
    }

    private Behavior<Command> onRecordTemperature(RecordTemperature record) {
        getContext().getLog().info("Recording temperature value {} for request {}", record.value, record.requestId);
        lastTemperatureReading = Optional.of(record.value);
        record.replyTo.tell(new TemperatureRecorded(record.requestId));
        return this;
    }

    private Behavior<Command> onPostStop() {
        getContext().getLog().error("Device actor {}-{} stopped unexpectedly :-(", groupId, deviceId);
        return this;
    }

}
