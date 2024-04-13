package edu.academy.thriller.device.group;

import akka.actor.typed.ActorRef;
import edu.academy.thriller.device.Device;

public final class DeviceTerminated implements DeviceGroup.Command {
    public final String groupId;
    public final String deviceId;
    public final ActorRef<Device.Command> replyTo;

    public DeviceTerminated(String groupId, String deviceId, ActorRef<Device.Command> replyTo) {
        this.groupId = groupId;
        this.deviceId = deviceId;
        this.replyTo = replyTo;
    }
}
