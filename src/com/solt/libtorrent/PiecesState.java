package com.solt.libtorrent;

public class PiecesState {
	private String hashCode;
	private int fromIdx;
	private int len;
	private byte[] states;
	private int stateLen;
	private int stateIdx;
	private byte mark;
	
	public PiecesState(String hashCode) {
		this.hashCode = hashCode;
	}
	
	public PiecesState(String hashCode, int fromIdx, int len) {
		this.hashCode = hashCode;
		setFromIdx(fromIdx);
		setLength(len, true);
	}

	byte[] getStates() {
		return states;
	}

	int getStateLen() {
		return stateLen;
	}

	int getStateIdx() {
		return stateIdx;
	}

	public int getFromIdx() {
		return fromIdx;
	}

	public void setFromIdx(int fromIdx) {
		if (fromIdx < 0) {
			return;
		}
		this.fromIdx = fromIdx;
		stateIdx = fromIdx >> 3;
		mark = (byte) (0xff >>> (fromIdx & 7));
	}

	public int getLenght() {
		return len;
	}

	public void setLength(int len, boolean force) {
		if (len <= 0)
			return;
		stateLen = (len + 7) >> 3;
		if (states == null || stateLen > states.length || force) {
			states = new byte[stateLen];
		}
		this.len = len;
	}

	/**
	 * check whether a given piece index is done. Note index must be in range <i>[fromIdx</i>, <i>fromIdx + len)</i>
	 * @param index
	 * @return
	 */
	public boolean isDone(int index) {
		return (states[index >> 3 - stateIdx] & (0x80 >> (index & 7))) != 0;
	}
	
	/**
	 * get number piece was done
	 * @return
	 */
	public int getNumDone() {
		int counter = 0;
		int idx = 0;
		states[0] = (byte) (states[0] & mark);
		for (int i = 0; i < stateLen; ++i) {
			idx = states[i] >= 0 ? states[i] : (256 + states[i]);
			counter += BitsSetTable256[idx];
		}
		return counter;
	}

	public String getHashCode() {
		return hashCode;
	}

	public void setHashCode(String hashCode) {
		this.hashCode = hashCode;
	}
	
	/**
	 * get first incomplete piece from <i>fromIdx</i>
	 * @return
	 */
	public int getFirstIncomplete() {
		int i = fromIdx;
		for (int n = fromIdx + len; i < n && isDone(i); ++i) {
		}
		return i;
	}
	
	public int getLastIncomplete() {
		int i = fromIdx + len - 1;
		for (; i <= fromIdx && isDone(i); --i) {
		}
		return i;
	}
	
	public int getLastIncomplete(int fromIdx) {
		int i = fromIdx;
		for (; i <= this.fromIdx && isDone(i); --i) {
		}
		return i;
	}
	
	private static byte[] BitsSetTable256 = new byte[256];
	static {
		BitsSetTable256[0] = 0;
		for (int i = 0; i < 256; i++) {
			BitsSetTable256[i] = (byte)((i & 1) + BitsSetTable256[i / 2]);
		}
	}

}
