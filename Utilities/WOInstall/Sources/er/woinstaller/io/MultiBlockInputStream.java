package er.woinstaller.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class MultiBlockInputStream extends InputStream {
	private final InputStream _inputSource;
	private final LinkedList<BlockEntry> _blockList;
	private InputStream _delegate;
	
	public MultiBlockInputStream(InputStream input, List<BlockEntry> blockList) {
		_inputSource = input;
		LinkedList<BlockEntry> newList = new LinkedList<BlockEntry>();
		newList.addAll(blockList);
		Collections.sort(newList);
		_blockList = newList;
	}
	
	@Override
	public int read() throws IOException {
		if (_delegate == null) {
			_delegate = getNextDelegate();
		}
		if (_delegate == null) {
			return -1;
		}
		int result = _delegate.read();
		if (result == -1) {
			_delegate = getNextDelegate();
			if (_delegate == null) {
				return result;
			}
			return _delegate.read();
		}
		return result;
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (_delegate == null) {
			_delegate = getNextDelegate();
		}
		if (_delegate == null) {
			return -1;
		}
		
		int result = _delegate.read(b, off, len);
		if (result < len) {
			_delegate = getNextDelegate();
			if (_delegate == null) {
				return result;
			}
			int result2 = _delegate.read(b, off+result, len-result);
			if (result2 != -1) {
				return result + result2;
			}
		}
		return result;
	}

	private InputStream getNextDelegate() throws IOException {
		BlockEntry currentBlock = _blockList.element();
		if (_delegate != null) {
			_delegate = null;
			_blockList.remove();
		}
		if (!_blockList.isEmpty()) {
			BlockEntry newBlock = _blockList.element();
			long newOffset = newBlock.offset;
			if (currentBlock != newBlock) {
				newOffset -= (currentBlock.offset + currentBlock.length);
			}
			_delegate = new BoundedInputStream(_inputSource, newOffset, newBlock.length);
		}
		return _delegate;
	}
}
