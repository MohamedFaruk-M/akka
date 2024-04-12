package edu.academy.thriller.message;

import java.util.Optional;

public class RespondTemperature {

    public final long requestId;
    public final Optional<Double> value;

    public RespondTemperature(long requestId, Optional<Double> value) {
        this.requestId = requestId;
        this.value = value;
    }
}
