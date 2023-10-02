// prompts user for a list of positive integers, then
// heapsorts them
class Main extends Lib {
	
	// main method: reads a list from keyboard and sorts it
    public void main() {
    	// prompt for and read list
    	printStr("Please type a list of integers, ending with 0: ");
    	int[] arr = readIntArrayList().toArray();
    	
    	// print the list
    	printStr("The original list is:");
    	printIntArray(arr);
    	printStr("\n");
    	
    	// sort the list
    	heapsort(arr);

    	// print the sorted list
    	printStr("The sorted list is:");
    	printIntArray(arr);
    	printStr("\n");
    	
    }
    
    // reads a list from the keyboard, terminating when 0 is read    
    public IntArrayList readIntArrayList() {
    	// initialize return value to null
    	IntArrayList rtnVal = new IntArrayList().init();
    	
    	// loop through, adding elements to the ArrayList until 0 is seen
    	for (;;) {
    		int val = readInt();
    		if (val == 0) break;
    		rtnVal.add(val);
    	}
    	
    	// return the list
    	return rtnVal;
    }
    
    // prints a list
    public void printIntArray(int[] arr) {
    	for (int i = 0; i < arr.length; i++) {
    		printStr(" ");
    		printInt(arr[i]);
    	}
    }
    
    // heapsorts an array
    public void heapsort(int[] arr) {
    	// Create a heap by max-heapifying at every level from the bottom up. We
    	// can start at the halfway point because everything in the higher-half
    	// indices are leave.
    	for (int i = arr.length/2; i >= 0; i--) {
    		maxHeapify(arr, i, arr.length);
    	}
    	
    	// starting at the high index, iteratively removes elements
    	// from the heap, and putting them in the vacated spots
    	for (int limitIdx = arr.length; limitIdx > 0; limitIdx--) {
    		arr[limitIdx-1] = removeElement(arr, limitIdx);
    	}
    }
    
    // remove an element
    public int removeElement(int[] arr, int limitIdx) {
    	// default value to return if heap is empty
    	int rtnVal = -1;
    	
    	// if heap is not empty, return value is top element, and last
    	// element goes to top, followed by maxHeapify
    	if (limitIdx != 0) {
    		rtnVal = arr[0];
    		arr[0] = arr[limitIdx-1];
    		maxHeapify(arr, 0, limitIdx-1);
    	}
    	return rtnVal;
    }
    
    // min=-heapifies the array, with root at index 'rootIdx', where
    // elements at 'limitIdx' and beyond are not considered part of
    // the heap
    public void maxHeapify(int[] arr, int rootIdx, int limitIdx) {
    	for (;;) {
    		// determine left and right indices
    		int leftIdx = rootIdx * 2 + 1;
    		int rightIdx = rootIdx * 2 + 2;
    		
    		// if neither left or right index is valid, we're done
    		if (leftIdx >= limitIdx) break;
    		
    		// set the lower root index corresponding to the minimum of
    		// the left data and (if present) the right data
    		int lowerRootIdx = leftIdx;
    		if (rightIdx < limitIdx && arr[rightIdx] > arr[leftIdx]) {
    			lowerRootIdx = rightIdx;
    		}
    		
    		// if the current root is <= than the minimum, we've found
    		// our spot, so quit
    		if (arr[rootIdx] >= arr[lowerRootIdx]) break;
    		
    		// if the current root is greater, swap it; we'll then loop
    		// back to do the next level
    		int temp = arr[rootIdx];
    		arr[rootIdx] = arr[lowerRootIdx];
    		arr[lowerRootIdx] = temp;
    		rootIdx = lowerRootIdx;
    	}
    }
}

// class: ArrayList of int
class IntArrayList {
	
	// array containing our data
	int[] array;
	
	// number of elements actually in list
	int count;
	
	// initializes a list from its parameters
	public IntArrayList init() {
		this.count = 0; // no elements in heap
		this.array = new int[10]; // initial array of size 10
		return this;
	}
	
	// adds an element to the and of the ArrayList
	public void add(int val) {
		
		// if there is no more room in the array, reallocate
		// at twice the size and copy the elements
		if (count >= array.length) {
			// reallocate
			int[] newArray = new int[array.length*2];
			// copy elements
			for (int i = 0; i < array.length; i++) {
				newArray[i] = array[i];
			}
			// set new array as our array
			array = newArray;
		}
		
		// add the element to the end and bump the count
		array[count] = val;
		count++;
	}
	
	// converts ArrayList of int to array of int
	public int[] toArray() {
		// return value whose size is the actual number of elements
		int[] rtnVal = new int[count];
		
		// copy the elements and return the array
		for (int i = 0; i < count; i++) {
			rtnVal[i] = array[i];
		}
		return rtnVal;
	}
}
