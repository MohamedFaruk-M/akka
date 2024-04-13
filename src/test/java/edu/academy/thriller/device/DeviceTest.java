package edu.academy.thriller.device;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.ClassRule;
import org.junit.Test;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import edu.academy.thriller.device.group.DeviceGroup;
import edu.academy.thriller.device.management.DeviceRegistered;
import edu.academy.thriller.device.management.ReplyDeviceList;
import edu.academy.thriller.device.management.RequestDeviceList;
import edu.academy.thriller.device.management.RequestTrackDevice;

public class DeviceTest {

    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testReplyWithEmptyReadingIfNoTemperatureIsKnown() {
        TestProbe<RespondTemperature> testProbe = testKit.createTestProbe(RespondTemperature.class);

        ActorRef<Device.Command> deviceActor = testKit.spawn(Device.create("group", "device"));
        deviceActor.tell(new ReadTemperature(42L, testProbe.getRef()));

        RespondTemperature response = testProbe.receiveMessage();

        assertEquals(42L, response.requestId);
        assertEquals(Optional.empty(), response.value);

    }

    @Test
    public void testReplyWithLatestTemperatureReading() {
        RespondTemperature response;
        TestProbe<TemperatureRecorded> recordProbe = testKit.createTestProbe(TemperatureRecorded.class);

        TestProbe<RespondTemperature> readProbe = testKit.createTestProbe(RespondTemperature.class);

        ActorRef<Device.Command> deviceActor = testKit.spawn(Device.create("group", "device"));

        deviceActor.tell(new RecordTemperature(1L, 12.0, recordProbe.getRef()));
        assertEquals(1L, recordProbe.receiveMessage().requestId);

        deviceActor.tell(new ReadTemperature(2L, readProbe.getRef()));
        response = readProbe.receiveMessage();
        assertEquals(2L, response.requestId);
        assertEquals(Optional.of(12.0), response.value);


        deviceActor.tell(new RecordTemperature(3L, 55.0, recordProbe.getRef()));
        assertEquals(3L, recordProbe.receiveMessage().requestId);

        deviceActor.tell(new ReadTemperature(4L, readProbe.getRef()));
        response = readProbe.receiveMessage();
        assertEquals(4L, response.requestId);
        assertEquals(Optional.of(55.0), response.value);
    }

    @Test
    public void testReplyToRegistrationRequests() {
        TestProbe<DeviceRegistered> regProbe = testKit.createTestProbe(DeviceRegistered.class);

        ActorRef<DeviceGroup.Command> groupActor = testKit.spawn(DeviceGroup.create("group"));

        groupActor.tell(new RequestTrackDevice("group", "deviceA", regProbe.getRef()));

        DeviceRegistered deviceA = regProbe.receiveMessage();

        //another device {B}
        groupActor.tell(new RequestTrackDevice("group", "deviceB", regProbe.getRef()));
        DeviceRegistered deviceB = regProbe.receiveMessage();

        assertNotEquals(deviceA.device, deviceB.device);

        // chk device actors A n B are working
        TestProbe<TemperatureRecorded> recordProbe = testKit.createTestProbe(TemperatureRecorded.class);
        deviceA.device.tell(new RecordTemperature(0L, 12.0, recordProbe.getRef()));
        assertEquals(0L, recordProbe.receiveMessage().requestId);

        deviceB.device.tell(new RecordTemperature(1L, 33.0, recordProbe.getRef()));
        assertEquals(1L, recordProbe.receiveMessage().requestId);

    }

    @Test
    public void testIgnoreWrongRegistrationRequests() {
        TestProbe<DeviceRegistered> devRegProbe = testKit.createTestProbe(DeviceRegistered.class);
        ActorRef<DeviceGroup.Command> groupActor = testKit.spawn(DeviceGroup.create("group"), "group");

        groupActor.tell(new RequestTrackDevice("groupZ", "device", devRegProbe.getRef()));
        devRegProbe.expectNoMessage();
    }

    @Test
    public void testReturnSameActorForSameDeviceId() {
        TestProbe<DeviceRegistered> devRegProbe = testKit.createTestProbe(DeviceRegistered.class);
        ActorRef<DeviceGroup.Command> groupActor = testKit.spawn(DeviceGroup.create("group"), "group");

        groupActor.tell(new RequestTrackDevice("group", "device", devRegProbe.getRef()));
        DeviceRegistered deviceRegistered = devRegProbe.receiveMessage();

        // registering the same device should be idempotent
        groupActor.tell(new RequestTrackDevice("group", "device", devRegProbe.getRef()));
        DeviceRegistered deviceReRegistered = devRegProbe.receiveMessage();

        assertEquals(deviceRegistered.device, deviceReRegistered.device);

    }

    @Test
    public void testListActiveDevices() {
        TestProbe<DeviceRegistered> devRegProbe = testKit.createTestProbe(DeviceRegistered.class);

        ActorRef<DeviceGroup.Command> groupActor = testKit.spawn(DeviceGroup.create("group"), "group");
        groupActor.tell(new RequestTrackDevice("group", "deviceA", devRegProbe.getRef()));
        devRegProbe.receiveMessage();

        groupActor.tell(new RequestTrackDevice("group", "deviceB", devRegProbe.getRef()));
        devRegProbe.receiveMessage();

        TestProbe<ReplyDeviceList> replyDevicesProbe = testKit.createTestProbe(ReplyDeviceList.class);
        groupActor.tell(new RequestDeviceList(0L, "group", replyDevicesProbe.getRef()));

        ReplyDeviceList replyDeviceList = replyDevicesProbe.receiveMessage();
        assertEquals(0L, replyDeviceList.requestId);
        assertEquals(Stream.of("deviceA", "deviceB").collect(Collectors.toSet()), replyDeviceList.ids);

    }


    @Test
    public void testListActiveDevicesAfterOneShutDown() {
        TestProbe<DeviceRegistered> devRegProbe = testKit.createTestProbe(DeviceRegistered.class);

        ActorRef<DeviceGroup.Command> groupActor = testKit.spawn(DeviceGroup.create("group"), "group");
        groupActor.tell(new RequestTrackDevice("group", "deviceA", devRegProbe.getRef()));
        DeviceRegistered deviceARegistered = devRegProbe.receiveMessage();


        groupActor.tell(new RequestTrackDevice("group", "deviceB", devRegProbe.getRef()));
        DeviceRegistered deviceBRegistered = devRegProbe.receiveMessage();

        ActorRef<Device.Command> toShutDown = deviceARegistered.device;

        TestProbe<ReplyDeviceList> replyDevicesProbe = testKit.createTestProbe(ReplyDeviceList.class);
        groupActor.tell(new RequestDeviceList(0L, "group", replyDevicesProbe.getRef()));

        ReplyDeviceList replyDeviceList = replyDevicesProbe.receiveMessage();
        assertEquals(0L, replyDeviceList.requestId);
        assertEquals(Stream.of("deviceA", "deviceB").collect(Collectors.toSet()), replyDeviceList.ids);

        toShutDown.tell(Device.PASSIVATE.INSTANCE);
        devRegProbe.expectTerminated(toShutDown, devRegProbe.getRemainingOrDefault());

        devRegProbe.awaitAssert(
                () -> {
                    groupActor.tell(new RequestDeviceList(1L, "group", replyDevicesProbe.getRef()));
                    ReplyDeviceList replyDeviceListAfterPassivate = replyDevicesProbe.receiveMessage();

                    assertEquals(1L, replyDeviceListAfterPassivate.requestId);
                    assertEquals(Stream.of("deviceB").collect(Collectors.toSet()), replyDeviceList.ids);

                    return null;
                });

    }


}
