package segmentedfilesystem;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.omg.CORBA.portable.OutputStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Main {

    public static void main(String[] args) throws UnknownHostException, SocketException {
    	
    	// Setup the DatagramSocket and connect to the file server
    	InetAddress target = InetAddress.getByName("146.57.33.55");
    	int server_port = 6014;
    	DatagramSocket socket = new DatagramSocket();

    	byte[] emptyBuf = new byte[0];
    	DatagramPacket emptyPacket = new DatagramPacket(emptyBuf, 0, target, server_port);

    	try {
    		socket.send(emptyPacket);
    	} catch (IOException e) {

    		System.out.println("IOException when starting connection");

    	}
    	// Declare a buffer to store the data from the packet we receive
    	byte[] buf = new byte[1028];

    	DatagramPacket p = new DatagramPacket(buf, buf.length);
    	try {
			socket.receive(p);
		} catch (IOException e) {
			e.printStackTrace();
		}

    	int headerCount = 0;
    	int footerCount = 0;
    	int packetCount = 0;
    	ArrayList<DatagramPacket> packetArray = new ArrayList<DatagramPacket>();
    	ArrayList<DatagramPacket> headerArray = new ArrayList<DatagramPacket>();
    	ArrayList<Byte> IDArray = new ArrayList<Byte>();

    	
    	
    	while (true) {
    		if (p != null) {
    			packetArray.add(p);
    			if (isHeader(p)) {
    				headerArray.add(p);
    				byte[] testb = new byte[p.getData().length - 2];
    				for (int i = 2; i < testb.length; i++) {
    					testb[i - 2] = p.getData()[i];
    				}
    				headerCount++;
    				packetCount++;
    			}
    			if (isFooter(p)) {
    				footerCount++;
    				packetCount += getPacketNumber(p) + 1;
    			}
    			if (!IDArray.contains(p.getData()[1])) {
    				IDArray.add(p.getData()[1]);
    			}

    		}
    		// Make sure that we have seen all the header, footer, and data packets.
    		if ((headerCount == footerCount) && (footerCount == IDArray.size()) && (packetCount == packetArray.size())) {
    			break;
    		}
    		try {
    			buf = new byte[1028];
    			p = new DatagramPacket(buf, buf.length);
    			socket.receive(p);
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    	
    	// Filter the packets, assemble the file byte arrays, and sort them.
    	filterData(packetArray);
    	ArrayList<ArrayList<DatagramPacket>> assembledFiles = assembleFiles(packetArray, headerArray);
    	sortFiles(assembledFiles);
    	
    	// Write each file
    	for (int i = 0; i < assembledFiles.size(); i++) {
    		writeFile(assembledFiles.get(i), headerArray.get(i));
    	}

    	
    }

    /* Public methods that use the class TestPacket to mimic the structure of data
    / of DatagramPacket for testing. These methods are public so that main can
    / make use of them during testing.
    */
    
    public boolean isHeader(byte[] packetData) {
    	byte statusByte = packetData[0];

    	if ((statusByte % 2) == 0) {
    		if ((statusByte % 4) == 3) {
    			return false;
    		}
    		return true;
    	}
    	return false;
    }

    public boolean isFooter(byte[] packetData) {
    	byte statusByte = packetData[0];

    	if ((statusByte % 4) == 3 || (statusByte % 4) == -1) {

    		return true;

    	}
    	return false;

    }
    
    
    public int testgetPacketNumber(byte[] packetData) {

    	byte[] packetNumber = new byte[4];
    	byte[] b = packetData;
    	packetNumber[0] = (byte)0;
    	packetNumber[1] = (byte)0;
    	packetNumber[2] = b[2];
    	packetNumber[3] = b[3];

    	return ByteBuffer.wrap(packetNumber).getInt();


    }
    
    public void testfilterData(ArrayList<TestPacket> data) {
    	Iterator<TestPacket> packetIter = data.listIterator();
    	ArrayList<TestPacket> filteredData = new ArrayList<>();
    	while (packetIter.hasNext()) {
    		if (isHeader(packetIter.next().getData())) {
    			packetIter.remove();
    		}
    	}
    	filteredData = data;
    }
    
    public ArrayList<ArrayList<TestPacket>> testassembleFiles(ArrayList<TestPacket> data, ArrayList<TestPacket> files) {
    	ArrayList<ArrayList<TestPacket>> assembledFiles = new ArrayList<>();
      for (int i = 0; i < files.size(); i++) {
        ArrayList<TestPacket> currentFile = new ArrayList<>();
        int currentFileID = (int) files.get(i).getData()[1];
    		for (TestPacket currentPacket : data) {
    			if (((int) currentPacket.getData()[1]) == currentFileID) {
    				currentFile.add(currentPacket);
    				System.out.println("stored" + currentPacket.toString());
    			}
    		}
    		assembledFiles.add(currentFile);
    	}

    	return assembledFiles;
    }

    // filters out the header packets from the data packets 
    private static void filterData(ArrayList<DatagramPacket> data) {
    	
    	Iterator<DatagramPacket> packetIter = data.listIterator();
    	ArrayList<DatagramPacket> filteredData = new ArrayList<>();
    	while (packetIter.hasNext()) {
    		if (isHeader(packetIter.next())) {
    			packetIter.remove();
    		}
    	}
    	filteredData = data;
    }
    
    // Writes and returns, in un-sorted order, lists of datagram packets associated to each unique fileID. 
    private static ArrayList<ArrayList<DatagramPacket>> assembleFiles(ArrayList<DatagramPacket> data, ArrayList<DatagramPacket> files) {
    	ArrayList<ArrayList<DatagramPacket>> assembledFiles = new ArrayList<>();
    	for (int i = 0; i < files.size(); i++) {
    		ArrayList<DatagramPacket> currentFile = new ArrayList<>();
    		int currentFileID = (int) files.get(i).getData()[1];
    		for (DatagramPacket currentPacket : data) {
    			if (((int) currentPacket.getData()[1]) == currentFileID) {
    				currentFile.add(currentPacket);
    			}
    		}
    		assembledFiles.add(currentFile);
    	}
    	return assembledFiles;
    }
    
    // Sorts the order of the packets based on their packet number using a DatagramPacketComparator
    private static void sortFiles(ArrayList<ArrayList<DatagramPacket>> assembledFiles) {
    	for (ArrayList<DatagramPacket> currentFile : assembledFiles) {
    		Collections.sort(currentFile, new DatagramPacketComparator());
    	}
    }
    // Write the sorted DatagramPacket arrays to their respective filename
    private static void writeFile(ArrayList<DatagramPacket> fileData, DatagramPacket header) {
    	ArrayList<Byte> byteList = new ArrayList<Byte>();
    	ArrayList<Byte> fileNameByteList = new ArrayList<Byte>();

    	for (DatagramPacket p : fileData) {
    		for (int i = 4; i < p.getLength(); i++) {
    			byteList.add(p.getData()[i]);
    		}
    	}

    	for (int i = 2; i < header.getLength(); i++) {
    		fileNameByteList.add(header.getData()[i]);
    	}

    	byte[] b = new byte[byteList.size()];
    	byte[] h = new byte[fileNameByteList.size()];
    	
    	for (int i = 0; i < byteList.size(); i++) {
    		b[i] = byteList.get(i);
    	}
    	
    	for (int i = 0; i < fileNameByteList.size(); i++) {
    		h[i] = fileNameByteList.get(i);
    	}


    	String fileName = new String(h);
    	
    	//Template used for creating/writing to files:
    	//https://docs.oracle.com/javase/tutorial/essential/io/file.html
    	//Note: This only works if the files do not exist. Manually delete these files after each run

    	String pathName = "./"+fileName.trim();
    	Path path = Paths.get("./"+fileName);
    	try(BufferedOutputStream out = new BufferedOutputStream(
    			Files.newOutputStream(path,CREATE))) {
    			out.write(b);
    	} catch (IOException e) {
    		e.printStackTrace();
    	}

    }
    
    // Check if the given Datagram Packet is a header
    private static boolean isHeader(DatagramPacket p) {

    	byte[] b = p.getData();
    	byte statusByte = b[0];

    	if ((statusByte % 2) == 0) {
    		if ((statusByte % 4) == 3) {
    			return false;
    		}
    		return true;
    	}
    	return false;


    }
    
    // Check if the given Datagram Packet is a footer
    private static boolean isFooter(DatagramPacket p) {

    	byte[] b = p.getData();
    	byte statusByte = b[0];

    	if ((statusByte % 4) == 3 || (statusByte % 4) == -1) {

    		return true;

    	}
    	return false;

    }
    
    // Get the int value of the 3rd and 4th bytes of the DatagramPacket
    // ByteBuffer.wrap().getInt() requires a minimum of 4 bytes,
    // so prefix the int with two zero-valued bytes.
    private static int getPacketNumber(DatagramPacket p) {

    	byte[] packetNumber = new byte[4];
    	byte[] b = p.getData();
    	packetNumber[0] = (byte)0;
    	packetNumber[1] = (byte)0;
    	packetNumber[2] = b[2];
    	packetNumber[3] = b[3];

    	return ByteBuffer.wrap(packetNumber).getInt();


    }
    
    // Get the fileID from the 2nd byte of the packet.
    private static int getFileID(DatagramPacket p){

    	byte[] packetNumber = new byte[4];
    	byte[] b = p.getData();
    	packetNumber[0] = (byte)0;
    	packetNumber[1] = (byte)0;
    	packetNumber[1] = (byte)0;
    	packetNumber[3] = b[1];

    	return ByteBuffer.wrap(packetNumber).getInt();
    }
    
    // DatagramPacketComparator is used by the method sortFiles()
    // Compares DatagramPackets based on their packet number value
	private static class DatagramPacketComparator implements Comparator<DatagramPacket> {

		public DatagramPacketComparator(){

		}

		@Override
		public int compare(DatagramPacket d1, DatagramPacket d2) {

			int d1PacketNumber = getPacketNumber(d1);
			int d2PacketNumber = getPacketNumber(d2);

			if (d1PacketNumber < d2PacketNumber) {
				return -1;
			}

			if (d1PacketNumber > d2PacketNumber) {
				return 1;
			}

			else {
				return 0;
			}
		}

	}
}
