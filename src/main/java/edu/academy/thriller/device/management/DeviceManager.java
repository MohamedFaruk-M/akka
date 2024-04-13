package edu.academy.thriller.device.management;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import edu.academy.thriller.device.group.DeviceGroup;

public class DeviceManager extends AbstractBehavior<DeviceManager.Command> {

    public interface Command {
    }

    public static Behavior<DeviceManager.Command> create() {
        return Behaviors.setup(DeviceManager::new);
    }

    public DeviceManager(ActorContext<Command> context) {
        super(context);
        getContext().getLog().info("Device manager started ...");
    }

    public final Map<String, ActorRef<DeviceGroup.Command>> groupIdToActor = new HashMap<>();

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(RequestTrackDevice.class, this::onTrackDevice)
                .onMessage(RequestDeviceList.class, this::onDeviceList)
                .onMessage(DeviceGroupTerminated.class, this::onTerminated)
                .onSignal(PostStop.class, sgl -> onPostStop())
                .build();
    }

    private Behavior<Command> onTrackDevice(RequestTrackDevice requestTrackDevice) {
        ActorRef<DeviceGroup.Command> groupActor = groupIdToActor.get(requestTrackDevice.groupId);

        if (groupActor == null) {
            getContext().getLog().info("Creating group actor for {} ", requestTrackDevice.groupId);
            groupActor = getContext().spawn(DeviceGroup.create(requestTrackDevice.groupId),
                    "group-" + requestTrackDevice.groupId);

            // perform watch death
            getContext().watchWith(groupActor, new DeviceGroupTerminated(requestTrackDevice.groupId));

            groupIdToActor.put(requestTrackDevice.groupId, groupActor);
        }

        groupActor.tell(requestTrackDevice);
        return this;
    }

    private Behavior<Command> onDeviceList(RequestDeviceList requestDeviceList) {
        ActorRef<DeviceGroup.Command> groupActor = groupIdToActor.get(requestDeviceList.groupId);

        if (groupActor != null)
            groupActor.tell(requestDeviceList);
        else
            requestDeviceList.replyTo.tell(new ReplyDeviceList(requestDeviceList.requestId, Collections.emptySet()));

        return this;
    }

    private Behavior<Command> onTerminated(DeviceGroupTerminated deviceGroupTerminated) {
        getContext().getLog().warn("Device group {} has been terminated :-(", deviceGroupTerminated.groupId);
        groupIdToActor.remove(deviceGroupTerminated.groupId);
        return this;
    }

    private Behavior<Command> onPostStop() {
        getContext().getLog().error("Device manager stopped ;;-(");
        return this;
    }

}
