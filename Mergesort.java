// prompts user for a list of positive integers, then
// mergesorts them
class Main extends Lib {
	
	// main method: reads a list from keyboard and sorts it
    public void main() {
    	// prompt for and read list
    	printStr("Please type a list of integers, ending with 0: ");
    	IntList list = readIntList();
    	
    	// print the list
    	printStr("The original list is:");
    	printIntList(list);
    	printStr("\n");
    	
    	// sort the list
    	list = mergesort(list);

    	// print the sorted list
    	printStr("The sorted list is:");
    	printIntList(list);
    	printStr("\n");
    	
    }
    
    // reads a list from the keyboard, terminating when 0 is read    
    public IntList readIntList() {
    	// initialize return value to null
    	IntList rtnVal = null;
    	
    	// read value; if non-zero, create list and recursively
    	// create rest of list
    	int value = readInt();
    	if (value != 0) {
    		rtnVal = new IntList().init(value, readIntList());
    	}
    	
    	// return the list
    	return rtnVal;
    }
    
    // prints a list
    public void printIntList(IntList list) {
    	// if list is not null, print its first value; then
    	// recursively print rest
    	if (list != null) {
    		printStr(" ");
    		printInt(list.value);
    		printIntList(list.next);
    	}
    }
    
    // mergesorts a list
    public IntList mergesort(IntList list) {
    	// determine the list's length
    	int len = 0;
    	for (IntList lst = list; lst != null; lst = lst.next) {
    		len++;
    	}
    	
    	// call the helper function to do the main work
    	return helpMergesort(list, len);
    }
    
    
    public IntList helpMergesort(IntList list, int length) {
    	// default value (for length < 2) is the list itself
    	IntList rtnVal = list;
    	
    	// if length >= 2, we have the recursive case
    	if (length >= 2) {
    		
    		// determine spot to split the list: halfway down
    		// the list; we increment by 2 because we want to
    		// go to the halfway point
    		IntList splitSpot = list;
    		for (int i = 2; i < length; i = i + 2) {
    			splitSpot = splitSpot.next;
    		}
    		
    		// break the list at the halfway point; second list
    		// begins with the node at the halfway point
    		IntList firstHalf = list;
    		IntList secondHalf = splitSpot.next;
    		splitSpot.next = null; // breaks the list
    		
    		// recursively sort both halves
    		firstHalf = helpMergesort(firstHalf, (length+1)/2);
    		secondHalf = helpMergesort(secondHalf, length/2);
    		
    		// merge the lists    		
    		rtnVal = merge(firstHalf, secondHalf);
    	}
    	
    	// return the sorted list
    	return rtnVal;
    }
    
    // merges two sorted lists into a sorted list
    public IntList merge(IntList first, IntList second) {
    	
    	// variable for our return value
    	IntList rtnVal = null;

    	if (first == null) {
    		// first list is null, so return the second
    		rtnVal = second;
    	}
    	else if (second == null) {
    		// second list is null, so return the first
    		rtnVal = first;
    	}
    	else if (first.value < second.value) {
    		// both lists non-null, and first element in first list
    		// is smaller: first element will be first element of
    		// first list, recursively merge to build the rest
    		// of the list
    		rtnVal = first;
    		rtnVal.next = merge(first.next, second);
    	}
    	else {
    		// both lists non-null, and first element in second list
    		// is smaller: first element will be first element of
    		// second list, recursively merge to build the rest
    		// of the list
    		rtnVal = second;
    		rtnVal.next = merge(first, second.next);
    	}
    	
    	// return the sorted list
    	return rtnVal;
    }
}

// class: linked list of int
class IntList {
	
	// data element in the list
	int value;
	
	// next-pointer
	IntList next;
	
	// initializes a list from its parameters
	public IntList init(int value, IntList next) {
		this.value = value;
		this.next = next;
		return this;
	}
}
