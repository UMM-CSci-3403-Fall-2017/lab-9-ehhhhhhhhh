package segmentedfilesystem;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
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
    	
    	byte[] buf = new byte[256];

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
    				System.out.println(new String(testb));
    				System.out.println("^ is header filename");
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
    		if ((headerCount == footerCount) && (footerCount == IDArray.size()) && (packetCount == packetArray.size())) {
    			break;
    		}
    		try {
    			buf = new byte[256];
    			p = new DatagramPacket(buf, buf.length);
    			socket.receive(p);
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    	
    	filterData(packetArray);
    	ArrayList<ArrayList<DatagramPacket>> assembledFiles = assembleFiles(packetArray, headerArray);
    	sortFiles(assembledFiles);

    	for (int i = 0; i < assembledFiles.size(); i++) {
    		writeFile(assembledFiles.get(i), headerArray.get(i));
    	}

    	
    }

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

    private static ArrayList<ArrayList<DatagramPacket>> sortFiles(ArrayList<ArrayList<DatagramPacket>> assembledFiles) {
    	ArrayList<ArrayList<DatagramPacket>> sortedFiles = new ArrayList<>();
    	for (ArrayList<DatagramPacket> currentFile : assembledFiles) {
    		Collections.sort(currentFile, new DatagramPacketComparator());
    	}
    	return sortedFiles;
    }
    
    private static void writeFile(ArrayList<DatagramPacket> file, DatagramPacket header) {
    	ArrayList<Byte> byteList = new ArrayList<Byte>();
    	ArrayList<Byte> fileNameByteList = new ArrayList<Byte>();

    	for (DatagramPacket p : file) {
    		for (int i = 4; i < p.getData().length; i++) {
    			byteList.add(p.getData()[i]);
    		}
    	}

    	for (int i = 2; i < header.getData().length; i++) {
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
    	System.out.println("File name is:" + fileName);

    	//Credit for this file to StackOverflow question
    	//https://stackoverflow.com/questions/4350084/byte-to-file-in-java
    	//Asker
    	//https://stackoverflow.com/users/432539/elcool
    	//Edited by
    	//https://stackoverflow.com/users/2598/jjnguy
    	//Answerer
    	//https://stackoverflow.com/users/131433/bmargulies
    	//Edited by
    	//https://stackoverflow.com/users/184746/caesay
    	//https://stackoverflow.com/users/432294/jay-sullivan
    	//https://stackoverflow.com/users/57611/erike

    	//FileUtils.writeByteArrayToFile(new File("/" + fileName), myByteArray);
    	
    	File file0 = new File("src/" + fileName);
    	System.out.println(file0.getAbsolutePath());
    	
    	try {
    	if (!file0.exists()) {
    		file0.createNewFile();
    	}
    	} catch (IOException e) {
    		System.out.println("God fucking damnit");
    	}
    	
    	try (FileOutputStream o = new FileOutputStream(file0.getAbsolutePath())) {
    		o.write(b);
    		o.close();
    	} catch (IOException e) {
    		System.out.println("Error when writing to file");
    		e.printStackTrace();
    	}

    }
    
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

    private static boolean isFooter(DatagramPacket p) {

    	byte[] b = p.getData();
    	byte statusByte = b[0];

    	if ((statusByte % 4) == 3 || (statusByte % 4) == -1) {

    		return true;

    	}
    	return false;

    }

    private static int getPacketNumber(DatagramPacket p) {

    	byte[] packetNumber = new byte[4];
    	byte[] b = p.getData();
      packetNumber[0] = (byte)0;
    	packetNumber[1] = (byte)0;
    	packetNumber[2] = b[2];
    	packetNumber[3] = b[3];

    	return ByteBuffer.wrap(packetNumber).getInt();


    }

    private static int getFileID(DatagramPacket p){

    	byte[] packetNumber = new byte[4];
    	byte[] b = p.getData();
      packetNumber[0] = (byte)0;
    	packetNumber[1] = (byte)0;
    	packetNumber[1] = (byte)0;
    	packetNumber[3] = b[1];

    	return ByteBuffer.wrap(packetNumber).getInt();
    }

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
