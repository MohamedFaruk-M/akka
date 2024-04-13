package edu.academy.thriller.device.management;

import java.util.Set;

public class ReplyDeviceList {

    public final long requestId;
    public final Set<String> ids;

    public ReplyDeviceList(long requestId, Set<String> ids) {
        this.requestId = requestId;
        this.ids = ids;
    }
}
