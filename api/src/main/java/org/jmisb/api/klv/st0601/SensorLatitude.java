package org.jmisb.api.klv.st0601;

/**
 * Sensor Latitude (ST 0601 Item 13).
 *
 * <p>From ST:
 *
 * <blockquote>
 *
 * Sensor Latitude. Based on WGS84 ellipsoid.
 *
 * <p>Map -(2^31-1)..(2^31-1) to +/-90. Use -(2^31) as an "error" indicator. -(2^31) = 0x80000000.
 *
 * <p>Resolution: ~42 nano degrees.
 *
 * </blockquote>
 */
public class SensorLatitude extends UasDatalinkLatitude {
    /**
     * Create from value.
     *
     * @param degrees Latitude, in degrees [-90,90], or {@code Double.POSITIVE_INFINITY} to
     *     represent an error condition
     */
    public SensorLatitude(double degrees) {
        super(degrees);
    }

    /**
     * Create from encoded bytes.
     *
     * @param bytes Latitude, encoded as a 4-byte int
     */
    public SensorLatitude(byte[] bytes) {
        super(bytes);
    }

    @Override
    public String getDisplayName() {
        return "Sensor Latitude";
    }
}
