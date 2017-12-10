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

	@Test
	public void testFilterData() {
		// Header Packets <statusbyte> 1 byte, <fileID> 1 byte, <filename> rest of bytes
		byte[] h1Data = {(byte)0x02, (byte)0x35, (byte)0x45};
		byte[] h2Data = {(byte)0x02, (byte)0x34, (byte)0x44};
		byte[] h3Data = {(byte)0x02, (byte)0x33, (byte)0x43};
		TestPacket h1 = new TestPacket(h1Data);
		TestPacket h2 = new TestPacket(h2Data);
		TestPacket h3 = new TestPacket(h3Data);
		// Footer Packets <statusbyte> 1 byte, <fileID> 1 byte, <packetnumber> 2 bytes, <data> rest of bytes
		byte[] f1Data = {(byte)0xF3,(byte)0x35,(byte)0x00,(byte)0x03,(byte)0x13,(byte)0x14,(byte)0x15};
		byte[] f2Data = {(byte)0xF3,(byte)0x34,(byte)0x00,(byte)0x03,(byte)0x23,(byte)0x24,(byte)0x25};
		byte[] f3Data = {(byte)0xF3,(byte)0x33,(byte)0x00,(byte)0x03,(byte)0x33,(byte)0x34,(byte)0x35};
		TestPacket f1 = new TestPacket(f1Data);
		TestPacket f2 = new TestPacket(f2Data);
		TestPacket f3 = new TestPacket(f3Data);
		// h1, f1 data packets
		byte[] data1 = {(byte)0x00, (byte)0x35, (byte)0x00, (byte)0x00, (byte)0x13, (byte)0x14, (byte)0x15};
		byte[] data2 = {(byte)0x00, (byte)0x35, (byte)0x00, (byte)0x01, (byte)0x13, (byte)0x14, (byte)0x15};
		byte[] data3 = {(byte)0x00, (byte)0x35, (byte)0x00, (byte)0x02, (byte)0x13, (byte)0x14, (byte)0x15};
		TestPacket d1 = new TestPacket(data1);
		TestPacket d2 = new TestPacket(data2);
		TestPacket d3 = new TestPacket(data3);
		// h2, f2 data packets
		byte[] data4 = {(byte)0x00, (byte)0x34, (byte)0x00, (byte)0x00, (byte)0x13, (byte)0x14, (byte)0x15};
		byte[] data5 = {(byte)0x00, (byte)0x34, (byte)0x00, (byte)0x01, (byte)0x13, (byte)0x14, (byte)0x15};
		byte[] data6 = {(byte)0x00, (byte)0x34, (byte)0x00, (byte)0x02, (byte)0x13, (byte)0x14, (byte)0x15};
		TestPacket d4 = new TestPacket(data4);
		TestPacket d5 = new TestPacket(data5);
		TestPacket d6 = new TestPacket(data6);
		// h3, f3 data packets
		byte[] data7 = {(byte)0x00, (byte)0x33, (byte)0x00, (byte)0x00, (byte)0x13, (byte)0x14, (byte)0x15};
		byte[] data8 = {(byte)0x00, (byte)0x33, (byte)0x00, (byte)0x01, (byte)0x13, (byte)0x14, (byte)0x15};
		byte[] data9 = {(byte)0x00, (byte)0x33, (byte)0x00, (byte)0x02, (byte)0x13, (byte)0x14, (byte)0x15};
		TestPacket d7 = new TestPacket(data7);
		TestPacket d8 = new TestPacket(data8);
		TestPacket d9 = new TestPacket(data9);

		ArrayList<TestPacket> files = new ArrayList<>();
		files.add(h1);
		files.add(h2);
		files.add(h3);
		ArrayList<TestPacket> data = new ArrayList<>();
		// footers
		data.add(f1);
		data.add(f2);
		data.add(f3);
		// file 1 data
		data.add(d1);
		data.add(d2);
		data.add(d3);
		// file 2 data
		data.add(d4);
		data.add(d5);
		data.add(d6);
		// file 3 data
		data.add(d7);
		data.add(d8);
		data.add(d9);

		ArrayList<TestPacket> file1data = new ArrayList<>();
		file1data.add(f1);
		file1data.add(d1);
		file1data.add(d2);
		file1data.add(d3);

		ArrayList<TestPacket> file2data = new ArrayList<>();
		file2data.add(f2);
		file2data.add(d4);
		file2data.add(d5);
		file2data.add(d6);

		ArrayList<TestPacket> file3data = new ArrayList<>();
		file3data.add(f3);
		file3data.add(d7);
		file3data.add(d8);
		file3data.add(d9);

		ArrayList<ArrayList<TestPacket>> solution = new ArrayList<>();
		solution.add(file1data);
		solution.add(file2data);
		solution.add(file3data);
		assertEquals(solution, test.assembleFiles());
	}
}
