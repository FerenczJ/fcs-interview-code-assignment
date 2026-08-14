package com.fulfilment.application.monolith.location;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LocationTest {

    // class member containing all argument entries (reusable)
    static final List<Arguments> VALID_ENTRIES = Arrays.stream(Location.values())
            .map(loc -> Arguments.of(loc.name(), loc))
            .toList();

    @ParameterizedTest
    @MethodSource("validEntries")
    void testValueOfNameForValidEnumNames(String enumName, Location expected) {
        var actual = Location.valueOf(enumName);
        assertNotNull(actual, "Expected non-null for enum name: " + enumName);
        assertSame(expected, actual, "Expected same enum constant for name: " + enumName);
        // identification() is the external identifier (with hyphen); compare with expected
        assertEquals(expected.identification(), actual.identification(), "Identification should match");
    }

    static Stream<Arguments> validEntries() {
        return VALID_ENTRIES.stream();
    }

    @ParameterizedTest
    @MethodSource("invalidNames")
    void testValueOfThrowsForInvalidNames(String invalidName) {
        assertThrows(IllegalArgumentException.class,
                () -> Location.valueOf(invalidName),
                "Expected IllegalArgumentException for invalid name: " + invalidName);
    }

    static Stream<String> invalidNames() {
        // include some invalid enum names and the old hyphen-based identifiers which are not enum names
        return Stream.of(
                "UNKNOWN",
                "ZWOLLE-001", // hyphenated identification is not an enum constant name
                "AMSTERDAM-001",
                "NON_EXISTENT_123",
                "" // empty name
        );
    }

    @ParameterizedTest
    @MethodSource("allLocations")
    void testPropertiesAreConsistent(Location loc) {
        // ensure valueOf by enum name returns the same instance and properties are present
        var byName = Location.valueOf(loc.name());
        assertSame(loc, byName);
        assertNotNull(loc.identification(), "Identification must be present");
        assertTrue(loc.identification().contains("-"), "Identification should contain '-'");
        assertTrue(loc.zone() > 0, "Zone should be positive");
        assertTrue(loc.capacity() > 0, "Capacity should be positive");
    }

    static Stream<Location> allLocations() {
        return Arrays.stream(Location.values());
    }
}