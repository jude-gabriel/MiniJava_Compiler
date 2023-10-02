// insertion, removal and lookup of a hash set of strings

// the Main class
class Main extends Lib {

	// main method: reads a list from keyboard and sorts it
	public void main() {

		// print a "help" message for the user
		printHelp();
		
		// create an empty hash set
		StringHashSet set = new StringHashSet().init();

		// tells when we should exit the loop
		boolean done = false;

		// loop that processes then commands
		while (!done) {
			
			// prompt the user; read a line of text
			printStr("command: ");
			String line = this.readLine();

			// assuming the line is not empty, process the command
			if (line.length() > 0) {
				
				// extract the key, which is all but the first character of the line
				String key = line.substring(1, line.length());
				
				// perform the appropriate command (which is given by the first character
				switch(line.charAt(0)) {
				
				// quit the program: set the 'done' flag to true and emit a message
				case 'q': {
					done = true;
					printStr("done\n");
				}
				break;
				
				// insert: perform the insertion
				case 'i': {
					boolean result = set.insert(key);
					if (!result) {
						printStr("Element already there\n");
					}
				}
				break;
				
				// remove: perform the removal
				case 'r': {
					boolean result = set.remove(key);
					if (!result) {
						printStr("Element not there\n");
					}
				}
				break;
				
				// lookup: report whether the key was there
				case 'l': {
					boolean result = set.lookup(key);
					if (result) {
						printStr("'".concat(key).concat("' found\n"));
					}
					else {
						printStr("'".concat(key).concat("' not found\n"));
					}
				}
				break;

				// report the number of elements
				case 'n': {
					printStr("Number of elements: ");
					printInt(set.size());
					printStr("\n");
				}
				break;

				// print the elements
				case 'p': {
					String[] elements = set.asArray();
					for (int i = 0; i < elements.length; i++) {
						printStr(elements[i].concat("\n"));
					}
				}
				break;
				
				// illegal command: print message
				default:
					printStr("Illegal command\n");
					printUsage();
					break;
				}
			}
		}
	}
	
	// method that prints the initial help-message
	public void printHelp() {
		printStr("This program allows insertion and removal of Strings from a set.\n");
		printStr("The set is initially empty, but can be modified and examined using the \n");
		printStr("following commands:\n");
		printUsage();
	}

	// method that prints a help-message
	public void printUsage() {
		printStr("- 'iXyz' inserts string 'Xyz' into the set.\n");
		printStr("- 'rXyz' removes string 'Xyz' from the set.\n");
		printStr("- 'lXyz' reports whether string 'Xyz' is in the set.\n");
		printStr("- 'n' reports the number of elements in the set.\n");
		printStr("- 'p' prints each element in the set, one per line.\n");
		printStr("- 'q' quits the program.\n");
	}
}

// class StringHashSet: a set of strings
class StringHashSet {
	// number of elements
	int count;

	// buckets for the elements
	StringLinkedList[] elements;

	// initializes a StringHashSet to be an empty set
	public StringHashSet init() {
		this.count = 0;
		// start with a single slot so that we can exercise the code that
		// grows the hash table
		this.elements = new StringLinkedList[1];
		return this;		
	}

	// look up an element in the hash table
	public boolean lookup(String key) {
		// initially, have not found it
		boolean rtnVal = false;
		
		// iterate through the list in the hash slot, breaking if found
		int hashSlot = hashSlotFor(key);
		for (StringLinkedList p = elements[hashSlot]; p != null; p = p.next) {
			if (key.equals(p.data)) {
				rtnVal = true;
				break;
			}
		}
		
		// return indication if whether elemenet was found
		return rtnVal;
	}

	// remove an element from the hash table
	public boolean remove(String key) {
		// so far, have not found it
		boolean rtnVal = false;

		// keep track of previous slot, so that we can modify the list;
		// null implies that the current element is the first
		StringLinkedList prev = null;
		
		// iterate through, looking for element, deleting if found
		int hashSlot = hashSlotFor(key);
		for (StringLinkedList p = elements[hashSlot]; p != null; p = p.next) {
			if (key.equals(p.data)) {
				// we found the key, so unlink the current element by modifying
				// the previous element
				if (prev == null) {
					// at first element, so remove it from front
					elements[hashSlot] = elements[hashSlot].next;
				}
				else {
					// at some other element, so remove from next-field of previous
					prev.next = p.next;
				}
				
				// decrement element count; set return value; break out of loop, as we're done
				count--;
				rtnVal = true;
				break;
			}
			// update previous-pointer
			prev = p;
		}
		
		// return success indicator
		return rtnVal;
	}

	// insert an element into the hash table
	public boolean insert(String key) {
		// success indicator
		boolean rtnVal = true;
		
		// iterate through the element's slot's list, searching for it; if found,
		// we should fail, as the element is alread there
		int hashSlot = hashSlotFor(key);
		for (StringLinkedList p = elements[hashSlot]; p != null; p = p.next) {
			if (key.equals(p.data)) {
				rtnVal = false;
				break;
			}
		}
		
		// if element was not there, insert it, and bump the count
		if (rtnVal) {
			elements[hashSlot] = new StringLinkedList().init(key, elements[hashSlot]);
			count++;
			
			// if load factor exceeds three, so an array-grow and rehash
			if (count > 3*elements.length) {
				
				// original array
				StringLinkedList[] oldArray = elements;
				
				// new array
				elements = new StringLinkedList[oldArray.length*2];
				
				// copy elements into the new array, rehashing each to determine slot for
				// new array
				for (int i = 0; i < oldArray.length; i++) {
					while (oldArray[i] != null) {
						// extract from old list
						StringLinkedList current = oldArray[i];
						oldArray[i] = current.next;
						// compute hash slot
						int slot = hashSlotFor(current.data);
						// insert into new list
						current.next = elements[slot];
						elements[slot] = current;
					}
				}
			}
		}
		
		// return success indicator
		return rtnVal;
	}

	// number of elements in the hash table
	public int size() {
		return count;
	}

	// convert set to array of String
	public String[] asArray() {
		// create array of the correct size
		String[] rtnVal = new String[count];
		
		// iterate through all the lists, putting each element
		// we find in the array
		int spot = 0;
		for (int i = 0; i < elements.length; i++) {
			for (StringLinkedList sll = elements[i];
					sll != null;
					sll = sll.next) {
				rtnVal[spot] = sll.data;
				spot++;
			}
		}
		
		// return the array
		return rtnVal;
	}

	// compute hashSlot for a hash value
	public int hashSlotFor(String key) {
		int rtnVal = key.hashCode() % elements.length;
		if (rtnVal < 0) {
			// to handle negative remainder-values
			rtnVal = rtnVal + elements.length;
		}
		return rtnVal;
	}
}

// class: linked list of String
class StringLinkedList {

	// data value
	String data;

	// next pointer
	StringLinkedList next;

	// initializes a list from its parameters
	public StringLinkedList init(String data, StringLinkedList next) {
		this.data = data;
		this.next = next;
		return this;
	}
}
