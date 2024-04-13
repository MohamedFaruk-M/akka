package edu.academy.thriller.device.management;

import akka.actor.typed.ActorRef;
import edu.academy.thriller.device.Device;

public final class DeviceRegistered {
    public final ActorRef<Device.Command> device;

    public DeviceRegistered(ActorRef<Device.Command> device) {
        this.device = device;
    }
}
