package com.fulfilment.application.monolith.location;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private static final Map<String, Location> BY_IDENTIFICATION = Map.copyOf(Stream.of(values())
                    .collect(Collectors.toMap(Location::identification, location -> location)));


    Location(String identification, int zone, int capacity) {
        this.identification = identification;
        this.zone = zone;
        this.capacity = capacity;
    }

    public static Location fromIdentification(String identification) {
        if (identification == null) {
            throw new IllegalArgumentException("Identification cannot be null");
        }

        var location = BY_IDENTIFICATION.get(identification);
        if (location == null) {
            throw new IllegalArgumentException("No Location found with identification: " + identification);
        }
        return location;
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