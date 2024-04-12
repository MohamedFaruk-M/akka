package edu.academy.thriller;


import akka.actor.typed.ActorSystem;

public class IOTApplicationBootStrap {

    public static void main(String[] args) {
        ActorSystem.create(IOTSupervisor.create(), "iot-supervisor");
    }

}
