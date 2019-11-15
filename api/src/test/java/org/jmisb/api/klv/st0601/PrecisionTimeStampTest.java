package org.jmisb.api.klv.st0601;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

public class PrecisionTimeStampTest
{
    // Example from MISB ST doc
    @Test
    public void testExample()
    {
        // Convert byte[] -> value
        byte[] bytes = new byte[]{(byte)0x00, (byte)0x04, (byte)0x59, (byte)0xf4,
                (byte)0xA6, (byte)0xaa, (byte)0x4a, (byte)0xa8};
        PrecisionTimeStamp pts = new PrecisionTimeStamp(bytes);
        LocalDateTime dateTime = pts.getLocalDateTime();

        Assert.assertEquals(dateTime.getYear(), 2008);
        Assert.assertEquals(dateTime.getMonth(), Month.OCTOBER);
        Assert.assertEquals(dateTime.getDayOfMonth(), 24);
        Assert.assertEquals(dateTime.getHour(), 0);
        Assert.assertEquals(dateTime.getMinute(), 13);
        Assert.assertEquals(dateTime.getSecond(), 29);
        Assert.assertEquals(dateTime.getNano(), 913000000);

        // Convert value -> byte[]
        long microseconds = dateTime.toInstant(ZoneOffset.UTC).toEpochMilli() * 1000;
        PrecisionTimeStamp pts2 = new PrecisionTimeStamp(microseconds);
        Assert.assertEquals(pts2.getBytes(), new byte[]{(byte)0x00, (byte)0x04, (byte)0x59, (byte)0xf4,
                (byte)0xA6, (byte)0xaa, (byte)0x4a, (byte)0xa8});

        Assert.assertEquals(microseconds, 1224807209913000L);
    }

    @Test
    public void testNow()
    {
        LocalDateTime now = LocalDateTime.now();
        PrecisionTimeStamp pts = new PrecisionTimeStamp(now);
        Assert.assertEquals(pts.getLocalDateTime().getDayOfMonth(), now.getDayOfMonth());
        Assert.assertEquals(pts.getLocalDateTime().getHour(), now.getHour());
        long ptsMicroseconds = pts.getMicroseconds();
        long nowMicroseconds = ChronoUnit.MICROS.between(Instant.EPOCH, now.toInstant(ZoneOffset.UTC));
        Assert.assertEquals(ptsMicroseconds, nowMicroseconds);
    }

    @Test
    public void testMinAndMax()
    {
        PrecisionTimeStamp pts = new PrecisionTimeStamp(0L);
        Assert.assertEquals(pts.getLocalDateTime().getYear(), 1970);
        Assert.assertEquals(pts.getLocalDateTime().getMonth(), Month.JANUARY);
        Assert.assertEquals(pts.getLocalDateTime().getDayOfMonth(), 1);

        // Create max value and ensure no exception is thrown
        new PrecisionTimeStamp(Long.MAX_VALUE);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testTooSmall()
    {
        new PrecisionTimeStamp(-1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testTooBig()
    {
        // Oct 12, 2263 at 08:30
        new PrecisionTimeStamp(LocalDateTime.of(2263, 10, 12, 8, 30));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void badArrayLength()
    {
        new PrecisionTimeStamp(new byte[]{0x00, 0x00, 0x00, 0x00});
    }
}
