package edu.academy.thriller.device;

import static junit.framework.TestCase.assertEquals;

import java.util.Optional;

import org.junit.ClassRule;
import org.junit.Test;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;

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

}
