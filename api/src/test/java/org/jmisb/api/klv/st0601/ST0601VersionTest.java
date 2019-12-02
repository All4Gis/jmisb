package org.jmisb.api.klv.st0601;

import org.jmisb.api.common.KlvParseException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ST0601VersionTest {

    @Test
    public void testConstructFromValue() {
        // Min
        ST0601Version version = new ST0601Version((byte) 0);
        Assert.assertEquals(version.getBytes(), new byte[]{(byte) 0x00});
        Assert.assertEquals(version.getVersion(), (byte) 0);
        Assert.assertEquals(version.getDisplayableValue(), "0");

        // ST example
        version = new ST0601Version((byte) 13);
        Assert.assertEquals(version.getBytes(), new byte[]{(byte) 0x0d});
        Assert.assertEquals(version.getVersion(), 13);
        Assert.assertEquals(version.getDisplayableValue(), "13");
    }

    @Test
    public void testConstructFromEncoded() throws KlvParseException {
        // Min
        ST0601Version version = new ST0601Version(new byte[]{(byte) 0x00});
        Assert.assertEquals(version.getBytes(), new byte[]{(byte) 0x00});
        Assert.assertEquals(version.getVersion(), (byte) 0);
        Assert.assertEquals(version.getDisplayableValue(), "0");

        byte[] bytes = new byte[]{0x00};
        IUasDatalinkValue v = UasDatalinkFactory.createValue(UasDatalinkTag.UasLdsVersionNumber, bytes);
        Assert.assertTrue(v instanceof ST0601Version);
        version = (ST0601Version)v;
        Assert.assertEquals(version.getBytes(), new byte[]{(byte) 0x00});
        Assert.assertEquals(version.getVersion(), (byte) 0);
        Assert.assertEquals(version.getDisplayableValue(), "0");
        
        // Max
        version = new ST0601Version(new byte[]{(byte) 0xff});
        Assert.assertEquals(version.getBytes(), new byte[]{(byte) 0xff});


        // ST example
        version = new ST0601Version(new byte[]{(byte) 0x0d});
        Assert.assertEquals(version.getBytes(), new byte[]{(byte) 0x0d});
        Assert.assertEquals(version.getVersion(), 13);
        Assert.assertEquals(version.getDisplayableValue(), "13");
        
        bytes = new byte[]{0x0d};
        v = UasDatalinkFactory.createValue(UasDatalinkTag.UasLdsVersionNumber, bytes);
        Assert.assertTrue(v instanceof ST0601Version);
        version = (ST0601Version)v;
        Assert.assertEquals(version.getBytes(), new byte[]{(byte) 0x0d});
        Assert.assertEquals(version.getVersion(), 13);
        Assert.assertEquals(version.getDisplayableValue(), "13");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void badArrayLength() {
        new ST0601Version(new byte[]{0x00, 0x00});
    }
}
