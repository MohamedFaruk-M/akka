package edu.academy.thriller.device.management;

public final class DeviceGroupTerminated implements DeviceManager.Command {
    public final String groupId;

    public DeviceGroupTerminated(String groupId) {
        this.groupId = groupId;
    }
}
