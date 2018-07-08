/**
 * Simulates cache memory
 *
 * @author Esteban Acosta, Veronica Weiss
 */

import java.util.*;

public class CacheMemory{

	/** Set to true to print additional messages */
	private static final boolean DEBUG = false;

	/** The Main Memory this cache is connected to */
	private MainMemory mainMemory;

	/** Simulate cache as an array of CacheSet objects */
	private CacheSet[] cache;

	/** Number of bits used for selecting one byte within a cache line.
	 * These are the least significant bits of the memory address. */
	private int numByteBits;

	/** Number of bits used for specifying the cache set that a memory address
	 * belongs to. These are the middle bits of the memory address. */
	private int numSetBits;

	/** Number of bits used for specifying the tag associated with the
	 * memory address. These are the most significant bits of the memory address */
	private int numTagBits;

	/** Count of the total number of cache requests. This is used for implementing
	 * the least recently used replacement algorithm; and for reporting information
	 * about the cache simulation */
	private int requestCount;

	/** Count of the number of times a cache request is a hit. This is used for
	 * reporting information about the cache simulation */
	private int hitCount;

	/**
	 * DO NOT MODIFY THIS METHOD
	 *
	 * Constructor creates a CacheMemory object. Note the design rules for valid values of each parameter.
	 * The simulated computer reads or writes a unit of one word (4 Bytes).
	 *
	 * @param m The MainMemory object this cache is connected to.
	 * @param size The size of this cache, in Bytes. Must be a multiple of the lineSize.
	 * @param lineSize The size of one cache line, in Bytes. Must be a multiple of 4 Bytes.
	 * @param linesPerSet The number of lines per set. The number of lines in the cache must be a multiple
	 * of the linesPerSet.
	 *
	 * @exception IllegalArgumentExcepction if a parameter value violates a design rule.
	 */
	public CacheMemory(MainMemory m, int size, int lineSize, int linesPerSet) {

		if(lineSize % 4 != 0) {
			throw new IllegalArgumentException("lineSize is not a multiple of 4.");
		}

		if(size % lineSize != 0) {
			throw new IllegalArgumentException("size is not a multiple of lineSize.");
		}

		// number of lines in the cache
		int numLines = size / lineSize;

		if(numLines % linesPerSet != 0) {
			throw new IllegalArgumentException("number of lines is not a multiple of linesPerSet.");
		}

		// number of sets in the cache
		int numSets = numLines / linesPerSet;

		// Set the main memory
		mainMemory = m;

		// Initialize the counters to zero
		requestCount = 0;
		hitCount = 0;

		// Determine the number of bits required for the byte within a line,
		// for the set, and for the tag.
		int value;
		numByteBits = 0; // initialize to zero
		value = 1; // initialize to 2^0
		while(value < lineSize) {
			numByteBits++;
			value *= 2; // increase value by a power of 2
		}

		numSetBits = 0;
		value = 1;
		while(value < numSets) {
			numSetBits++;
			value *= 2;
		}

		// numTagBits is the remaining memory address bits
		numTagBits = 32 - numSetBits - numByteBits;

		System.out.println("CacheMemory constructor:");
		System.out.println("    numLines = " + numLines);
		System.out.println("    numSets = " + numSets);
		System.out.println("    numByteBits = " + numByteBits);
		System.out.println("    numSetBits = " + numSetBits);
		System.out.println("    numTagBits = " + numTagBits);
		System.out.println();

		// Create the array of CacheSet objects and initialize each CacheSet object
		cache = new CacheSet[numSets];
		for(int i = 0; i < cache.length; i++) {
			cache[i] = new CacheSet(lineSize, linesPerSet, numTagBits);
		}
	} // end of constructor

	/**
	 * DO NOT MODIFY THIS METHOD
	 *
	 * Prints the total number of requests and the number of requests that
	 * resulted in a cache hit.
	 */
	public void reportStats() {
		System.out.println("Number of requests: " + requestCount);
		System.out.println("Number of hits: " + hitCount);
		System.out.println("hit ratio: " + (double)hitCount / requestCount);
	}


	/**
	 * STUDENT MUST COMPLETE THIS METHOD
	 *
	 * Returns the 32-bits that begin at the specified memory address.
	 *
	 * @param address The byte address where the 32-bit value begins.
	 * @return The 32-bit value read from memory. Index 0 of the array holds
	 * the least significant bit of the binary value.
	 * @exception IllegalArgumentExcepction if the address is not valid.
	 */
	public boolean[] read32(boolean[] address) {
		if(address.length > 32) {
			throw new IllegalArgumentException("address parameter must be 32 bits");
		}

		//Increment request count every time we call this method
		requestCount++;

		//Creates three boolean arrays to reprsent the different partitions of the memory address 
		boolean [] numSet = new boolean[Binary.BINARY_LENGTH];

		boolean [] numByte = new boolean[Binary.BINARY_LENGTH];

		boolean[] numTag = new boolean[numTagBits];

		//We need to keep track of where we are in the address array
		int index = 0;

		//We take the byte bits and put it inside of its array
		for(int i = 0; i < numByteBits  ; i++){
			numByte[i] = address[i];
		}
	
		index = numByteBits;

		//We take the set bits and put it inside of its array
		for(int j = 0; j < numSetBits; j++){
			numSet[j] = address[index];
			index++;
		} 
		//And we extract tag bits out of address and place it inside of its array
		for(int h = 0; h < numTagBits; h++){
			numTag[h] = address[index];
			index++;
		}
		//This is going to tell us which set position the address is going to be at
		int setPosition = (int)Binary.binToSDec(numSet);

		//Determines whether or not the address is in cache
		boolean found = false;
		
		//Determines the line we are at in the cache
		int line = 0;
		//loops through cache size (for any # of set-associative cache) 
		for(int j = 0; j < cache[setPosition].size(); j++){
			//if tag bits from memory address = tag bits from cache 
			if(Arrays.equals(cache[setPosition].getLine(j).getTag(),numTag)){
				hitCount++;
				found = true;
				line = j;
				//make sure we update lastUsed here as well
				cache[setPosition].getLine(line).setLastUsed(requestCount);
			}
		}

		//If the address isn't in cache, call the ReadLineFromMemory method
		if(found == false){
			readLineFromMemory(address,setPosition,numTag);
		}


		int bite = (int) Binary.binToUDec(numByte);
		int count = 0;
		//Retrieving the data 
		boolean [] data = new boolean [Binary.BINARY_LENGTH];
		for(int i = bite; i < bite + 4; i++){
			for(int j = 0; j < 8; j++){
				data[count] = cache[setPosition].getLine(line).getData()[i][j];
				count++;
			}
		}
		return data;
	}


	/**
	 * STUDENT MUST COMPLETE THIS METHOD
	 *
	 * Copies a line of data from memory into cache. Selects the cache line to
	 * replace. Uses the Least Recently Used (LRU) algorithm when a choice must
	 * be made between multiple cache lines that could be replaced.
	 *
	 * @param address The requested memory address.
	 * @param setNum The set number where the address maps to.
	 * @param tagBits The tag bits from the memory address.
	 *
	 * @return The line that was read from memory. This line is also written
	 * into the cache.
	 */
	private CacheLine readLineFromMemory(boolean[] address, int setNum, boolean[] tagBits) {

		//Set the first line in the cache set as the least recently used 
		int min = cache[setNum].getLine(0).getLastUsed();
		int lineNum = 0;

		// Use the LRU (least recently used) replacement scheme to select a line
		// within the set.
		for(int j = 0; j < cache[setNum].size(); j++){
			//if the the minimum (which currently stores the least recently used line) is greater
			//than the the last used in that line
			int max = cache[setNum].getLine(j).getLastUsed();
			if(min > max){
				//make that line the least recently used line
				min = max;
				//Store the line number into lineNum
				lineNum = j; 
			}
			//updating set
		}
		
		//Take the numByteBits from the address array and make them all zeroes 
		//Place them into the copyAddress array
		boolean[] copyAddress = new boolean[Binary.BINARY_LENGTH];
		for(int i = 0 ; i < numByteBits; i++){
			copyAddress[i] = false;
		}

		//Copy the rest of the address that's passed to this method and store it in copyAddress
		for(int i = numByteBits; i < address.length;i++){
			copyAddress[i] = address[i];
		}

		//Take the data array
		boolean [][] data = mainMemory.read(copyAddress, cache[setNum].getLine(lineNum).size());

		// Copy the line read from memory into the cache
		cache[setNum].getLine(lineNum).setData(data);
		//Set valid to true
		cache[setNum].getLine(lineNum).isValid();
		//Set the last used to the current request count
		cache[setNum].getLine(lineNum).setLastUsed(requestCount);
		//Set the tag bits 
		cache[setNum].getLine(lineNum).setTag(tagBits);
		return cache[setNum].getLine(lineNum);
	}
}