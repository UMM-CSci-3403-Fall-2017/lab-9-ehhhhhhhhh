package segmentedfilesystem;

public class TestPacket {
  private byte[] data;

  public TestPacket(byte[] data) {
    this.data = data;
  }

  public byte[] getData() {
    return data;
  }
}
