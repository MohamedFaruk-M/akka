package edu.academy.thriller.device.group;

import java.util.HashMap;
import java.util.Map;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import edu.academy.thriller.device.Device;
import edu.academy.thriller.device.management.DeviceRegistered;
import edu.academy.thriller.device.management.ReplyDeviceList;
import edu.academy.thriller.device.management.RequestDeviceList;
import edu.academy.thriller.device.management.RequestTrackDevice;

public class DeviceGroup extends AbstractBehavior<DeviceGroup.Command> {

    public interface Command {
    }

    public final String groupId;

    public final Map<String, ActorRef<Device.Command>> deviceIdToActor = new HashMap<>();

    public static Behavior<Command> create(String groupId) {
        return Behaviors.setup(context -> new DeviceGroup(context, groupId));
    }

    public DeviceGroup(ActorContext<Command> context, String groupId) {
        super(context);
        this.groupId = groupId;

        getContext().getLog().info("Device group {} started ;-)", groupId);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(RequestTrackDevice.class, this::onTrackDevice)
                .onMessage(DeviceTerminated.class, this::onTerminated)
                .onMessage(RequestDeviceList.class, r -> r.groupId.equals(groupId), this::onDeviceList)
                .onSignal(PostStop.class, sgl -> onPostStop())
                .build();
    }

    private Behavior<Command> onTrackDevice(RequestTrackDevice trackDevice) {
        if (groupId.equals(trackDevice.groupId)) {
            ActorRef<Device.Command> deviceActor = deviceIdToActor.get(trackDevice.deviceId);

            if (deviceActor == null) {
                getContext().getLog().info("Creating device actor for {}", trackDevice.deviceId);
                deviceActor = getContext().spawn(Device.create(trackDevice.groupId, trackDevice.deviceId),
                        "device-" + trackDevice.deviceId);
                //perform watch death
                getContext().watchWith(deviceActor, new DeviceTerminated(groupId, trackDevice.deviceId, deviceActor));

                deviceIdToActor.put(trackDevice.deviceId, deviceActor);
            }

            trackDevice.replyTo.tell(new DeviceRegistered(deviceActor));
        } else {
            getContext().getLog()
                    .warn("Requested device actor {}-{} is not linked with this group {}, hence ignoring it :-| ",
                            trackDevice.groupId, trackDevice.deviceId, groupId);
        }

        return this;
    }

    private Behavior<Command> onTerminated(DeviceTerminated deviceTerminated) {
        getContext().getLog().warn("Device actor for {} has been terminated ", deviceTerminated.deviceId);
        deviceIdToActor.remove(deviceTerminated.deviceId);
        return this;
    }

    private Behavior<Command> onDeviceList(RequestDeviceList reqList) {
        reqList.replyTo.tell(new ReplyDeviceList(reqList.requestId, deviceIdToActor.keySet()));
        return this;
    }


    private DeviceGroup onPostStop() {
        getContext().getLog().error("Device group {} stopped :-(", this.groupId);
        return this;
    }


}
