package com.gigaspaces.bestpractices.bitpage;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceRouting;

@SpaceClass
public class BitPage {
	private static final int PAGE_SIZE_BYTES=128;
	private static final int PAGE_SIZE_BITS=PAGE_SIZE_BYTES*8;
	private Integer id;
	private byte[] bits=new byte[PAGE_SIZE_BYTES];
	
	public BitPage(){}
	
	/**
	 * Sets a value in the bitmap, and sets the
	 * id based on the supplied value.
	 * 
	 * @param value
	 */
	public BitPage(int value){
		id=getPageId(value);
		setBit(value);
	}
	
	public void setBit(int val) {
		int offset=val-id;
		bits[offset/8] |= offset%8;
	}
	
	public void clearBit(int val){
		int offset=val-id;
		bits[offset/8] &= 0;
	}

	@SpaceId(autoGenerate=false)
	@SpaceRouting
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public byte[] getBits() {
		return bits;
	}

	public void setBits(byte[] bits) {
		this.bits = bits;
	}
	
	public boolean isBitSet(int val){
		int offset=val-id;
		return (bits[offset/8] & offset%8)!=0;
	}
	
	/**
	 * Calculates the page id for the given
	 * integer.  The space of signed integer
	 * values is divided into pages of PAGE_SIZE
	 * bytes.  Each page represent 1024 bits
	 * of the bitmap representing all 4.3+B 
	 * ints.  The id of a page is the first 
	 * value represented by the bits it 
	 * contains.
	 * 
	 * @param val
	 * @return
	 */
	public int getPageId(final int val){
		long pageno=((long)val-(long)Integer.MIN_VALUE)/PAGE_SIZE_BITS;
		return Integer.MIN_VALUE+((int)pageno*PAGE_SIZE_BITS);
	}
	
	
}
