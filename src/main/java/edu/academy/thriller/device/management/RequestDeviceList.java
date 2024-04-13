package edu.academy.thriller.device.management;

import akka.actor.typed.ActorRef;
import edu.academy.thriller.device.group.DeviceGroup;

public class RequestDeviceList implements DeviceManager.Command, DeviceGroup.Command {

    public final long requestId;
    public final String groupId;
    public final ActorRef<ReplyDeviceList> replyTo;

    public RequestDeviceList(long requestId, String groupId, ActorRef<ReplyDeviceList> replyTo) {
        this.requestId = requestId;
        this.groupId = groupId;
        this.replyTo = replyTo;
    }
}
