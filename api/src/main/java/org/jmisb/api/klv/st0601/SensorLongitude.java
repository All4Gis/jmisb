package org.jmisb.api.klv.st0601;

import org.jmisb.core.klv.PrimitiveConverter;

import java.util.Arrays;

/**
 * Sensor Longitude (ST 0601 tag 14)
 * <p>
 * From ST:
 * <blockquote>
 * Sensor Longitude. Based on WGS84 ellipsoid.
 * <p>
 * Map -(2^31-1)..(2^31-1) to +/-180. Use -(2^31) as an "error" indicator.
 * -(2^31) = 0x80000000.
 * <p>
 * Resolution: ~84 nano degrees.
 * </blockquote>
 */
public class SensorLongitude extends UasDatalinkLongitude
{
    public SensorLongitude(double degrees)
    {
        super(degrees);
    }

    public SensorLongitude(byte[] bytes)
    {
        super(bytes);
    }
}
