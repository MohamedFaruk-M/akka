package edu.academy.thriller.device.management;

import akka.actor.typed.ActorRef;
import edu.academy.thriller.device.group.DeviceGroup;

public final class RequestTrackDevice implements DeviceManager.Command, DeviceGroup.Command {

    public final String groupId;
    public final String deviceId;

    public final ActorRef<DeviceRegistered> replyTo;

    public RequestTrackDevice(String groupId, String deviceId, ActorRef<DeviceRegistered> replyTo) {
        this.groupId = groupId;
        this.deviceId = deviceId;
        this.replyTo = replyTo;
    }
}
