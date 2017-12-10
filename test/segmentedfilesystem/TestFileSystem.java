package segmentedfilesystem;

import static org.junit.Assert.*;
import org.junit.Test;

public class TestFileSystem {
	private Main test = new Main();
	
	@Test
	public void testIsHeader() {
		byte[] data1 = {(byte)0x00, (byte)0x34, (byte)0x43};
		byte[] data2 = {(byte)0x01, (byte)0x34, (byte)0x43};
		byte[] data3 = {(byte)0xF1, (byte)0x34, (byte)0x43};
		byte[] data4 = {(byte)0xF2, (byte)0x34, (byte)0x43};
		
		assertTrue(test.isHeader(data1));
		assertFalse(test.isHeader(data2));
		assertFalse(test.isHeader(data3));
		assertTrue(test.isHeader(data4));
	}
	
	@Test
	public void testIsFooter() {
		byte[] data1 = {(byte)0x00, (byte)0x34, (byte)0x43};
		byte[] data2 = {(byte)0x01, (byte)0x34, (byte)0x43};
		byte[] data3 = {(byte)0xF3, (byte)0x34, (byte)0x43};
		byte[] data4 = {(byte)0x03, (byte)0x34, (byte)0x43};
		
		assertFalse(test.isFooter(data1));
		assertFalse(test.isFooter(data2));
		assertTrue(test.isFooter(data3));
		assertTrue(test.isFooter(data4));
	}
	
	
	
	@Test
	public void testGetPacketNumber() {
		byte[] data1 = {(byte)0x00, (byte)0x00, (byte)0x34, (byte)0x63};
		byte[] data2 = {(byte)0x00, (byte)0x00, (byte)0xA1, (byte)0x23};
		byte[] data3 = {(byte)0xF3, (byte)0x24, (byte)0x33};
		byte[] data4 = {(byte)0x03, (byte)0x34, (byte)0x43};
		
		assertEquals(13411, test.getPacketNumber(data1));
		assertEquals(41251, test.getPacketNumber(data2));
	}
}
