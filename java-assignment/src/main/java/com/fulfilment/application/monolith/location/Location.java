package com.fulfilment.application.monolith.location;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum Location {
    ZWOLLE_001("ZWOLLE-001", 1, 40),
    ZWOLLE_002("ZWOLLE-002", 2, 50),
    AMSTERDAM_001("AMSTERDAM-001", 5, 100),
    AMSTERDAM_002("AMSTERDAM-002", 3, 75),
    TILBURG_001("TILBURG-001", 1, 40),
    HELMOND_001("HELMOND-001", 1, 45),
    EINDHOVEN_001("EINDHOVEN-001", 2, 70),
    VETSBY_001("VETSBY-001", 1, 90);

    private final String identification;
    private final int zone;
    private final int capacity;

    Location(String identification, int zone, int capacity) {
        this.identification = identification;
        this.zone = zone;
        this.capacity = capacity;
    }

    public String identification() {
        return identification;
    }

    public int zone() {
        return zone;
    }

    public int capacity() {
        return capacity;
    }
}