package er.woinstaller.io;

public class BlockEntry implements Comparable<BlockEntry> {
	public Long offset;
	public Long length;

	@Override
  public int compareTo(BlockEntry o) {
		return offset.compareTo(o.offset);
	}
	
	@Override
  public String toString() {
		return ("offset: " + offset + " length: " + length);
	}
	
}
