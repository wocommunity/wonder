package er.woinstaller.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class MultiBlockInputStream extends InputStream {
	private final InputStream _inputSource;
	private final List<BlockEntry> _blockList;
	private InputStream _delegate;
	private int _blockPosition = 0;
	private BlockEntry _currentBlock = null;
	
	public MultiBlockInputStream(InputStream input, List<BlockEntry> blockList) {
		_inputSource = input;
		List<BlockEntry> newList = new ArrayList<BlockEntry>();
		newList.addAll(blockList);
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
		  if (_delegate != null) {
		    return _delegate.read();
		  }
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
		
		int result = 0;
		while (result < len) {
		  result = _delegate.read(b, off, len);
		  if (result < len) {
		    int result2 = _delegate.read(b, off+result, len-result);
		    if (result2 != -1) {
		      result += result2;
		    } else {
		      _delegate = getNextDelegate();
		      if (_delegate == null) {
		        return result;
		      }
		    }
		  }
		}
		return result;
	}

	private InputStream getNextDelegate() throws IOException {
		if (_delegate != null) {
			_delegate = null;
			_blockPosition++;
		}
		if (_blockList.size() > _blockPosition) {
			BlockEntry newBlock = _blockList.get(_blockPosition);
			long newOffset = newBlock.offset;
			if (_currentBlock != null && _currentBlock != newBlock) {
				newOffset -= (_currentBlock.offset + _currentBlock.length);
			}
			_currentBlock = newBlock;
			_delegate = new BoundedInputStream(_inputSource, newOffset, newBlock.length);
		}
		return _delegate;
	}
}
