package segmentedfilesystem;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Main {

    public static void main(String[] args) throws UnknownHostException, SocketException {

    	DatagramSocket socket = new DatagramSocket(6014, InetAddress.getByName("146.57.33.55"));

    	byte[] emptyBuf = new byte[0];
    	DatagramPacket emptyPacket = new DatagramPacket(emptyBuf, 0);

    	try {
    		socket.send(emptyPacket);
    	} catch (IOException e) {

    		System.out.println("IOException when starting connection");

    	}

    	DatagramPacket p = null;
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
    			socket.receive(p);
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	}



    }

//    public boolean isHeader(byte[] packetData) {
//    	byte statusByte = packetData[0];
//
//    	if ((statusByte % 2) == 0) {
//    		if ((statusByte % 4) == 3) {
//    			return false;
//    		}
//    		return true;
//    	}
//    	return false;
//    }
//
//    public boolean isFooter(byte[] packetData) {
//    	byte statusByte = packetData[0];
//
//    	if ((statusByte % 4) == 3 || (statusByte % 4) == -1) {
//
//    		return true;
//
//    	}
//    	return false;
//
//    }
//
//    public int getPacketNumber(byte[] packetData) {
//
//    	byte[] packetNumber = new byte[4];
//    	byte[] b = packetData;
//    	packetNumber[0] = (byte)0;
//    	packetNumber[1] = (byte)0;
//    	packetNumber[2] = b[2];
//    	packetNumber[3] = b[3];
//
//    	return ByteBuffer.wrap(packetNumber).getInt();
//
//
//    }

    public ArrayList<ArrayList<TestPacket>> assembleFiles(ArrayList<TestPacket> data, ArrayList<TestPacket> files) {
    	ArrayList<ArrayList<TestPacket>> assembledFiles = new ArrayList<>();
      for (int i = 0; i < files.size(); i++) {
        ArrayList<TestPacket> currentFile = new ArrayList<>();
        int currentFileID = (int) files.get(i).getData()[1];
    		for (TestPacket currentPacket : data) {
    			if (((int) currentPacket.getData()[1]) == currentFileID) {
    				currentFile.add(currentPacket);
    			}
    		}
    	}
    }

    private static void filterData(ArrayList<DatagramPacket> data) {
    	ArrayList<DatagramPacket> filteredData = new ArrayList<>();
    	for (DatagramPacket i : data) {
    		if (isHeader(i)) {
    			data.remove(i);
    		}0
    	}
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

    private static void writeFile

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
