// import java.math.BigInteger;

/*****
 * A big integer package modeled after Java's BigInteger class. It
 * implements the following methods:
 * - BigInt abs()
 * - BigInt add(BigInt val)
 * - int compareTo(BigInt val)
 * - BigInt divide(BigInt val)
 * - BigInt[] divideAndRemainder(BigInt val)
 * - boolean equals(Object x)
 * - int hashCode()
 *   - note: does not use the same hashing algorithm as BigInteger
 * - int intValue()
 * - BigInt max(BigInt val)
 * - BigInt min(BigInt val)
 * - BigInt multiply(BigInt val)
 * - BigInt negate()
 * - BigInt remainder(BigInt val)
 * - int signum()
 * - BigInt subtract(BigInt val)
 * - String toString()
 * It also implements an 'init' method that acts as a constructor
 * - BigInt init(int n)
 * which initializes the BigInt to have the value corresponding to n
 * 
 * @author vegdahl
 * @version 24 April 2019
 */

class Main {
	public void main() {
		int count = 1;
		BigIntTest tst = new BigIntTest();
//		tst = new BigInt2Test();
		tst.exec(count);
	}
}

//class BigInt2Test extends BigIntTest {
//	public BigInt newBigInt() {
//		return new BigInt2();
//	}
//}

class BigIntTest extends Lib {
	public BigInt newBigInt() {
		return new BigInt();
	}
	
	BigInt[] bigs;
	BigInt[] primes;
	BigInt zero;
	
	public void init(int numCount) {
		zero = newBigInt().init(0);
		bigs = new BigInt[numCount*2+1];
		bigs[0%numCount] = newBigInt().init(31);
		bigs[1%numCount] = newBigInt().init(2);
		bigs[2%numCount] = newBigInt().init(45);
		bigs[3%numCount] = newBigInt().init(100000);
		bigs[4%numCount] = newBigInt().init(10000);
		bigs[5%numCount] = newBigInt().init(-128*256*256*256);
		bigs[6%numCount] = fact(30);
		bigs[7%numCount] = fact(51);
		bigs[8%numCount] = fact(100);
		int limit = (bigs.length-1)/2;
		for (int i = 0; i < limit; i++) {
			bigs[i+limit] = bigs[i].negate();
		}
		bigs[limit*2] = zero;
		
		primes = new BigInt[numCount*2]; // must be even number
		primes[0%numCount] = newBigInt().init(43);
		primes[1%numCount] = newBigInt().init(97);
		primes[2%numCount] = newBigInt().init(421);
		primes[3%numCount] = newBigInt().init(617);
		primes[4%numCount] = newBigInt().init(919);
		primes[5%numCount] = newBigInt().init(1427);
		primes[6%numCount] = newBigInt().init(2657);
		primes[7%numCount] = newBigInt().init(5059);
		primes[8%numCount] = newBigInt().init(7873);
		limit = primes.length/2;
		for (int i = 0; i < limit; i++) {
			primes[i+limit] = primes[i].negate();
		}
	}
	
	public void exec(int numCount) {
		
		init(numCount);
		
		printStr("********* BEGIN TEST *********\n");
		printStr("====abs===\n");abs();
		printStr("====add===\n");add(); 
		printStr("====compareTo===\n");compareTo();  
		printStr("====equals===\n");equalsTest();
		printStr("====intValue===\n");intValue(); 
		printStr("====max===\n");max(); 
		printStr("====min===\n");min();
		printStr("====negate===\n");negate(); 
		printStr("====signum===\n");signum(); 
		printStr("====subtract===\n");subtract();
		printStr("====toString===\n");toStringTest(); 
		printStr("====multiply===\n");multiply(); 
		printStr("====divide===\n");divide();
		printStr("====divideAndRemainder===\n");divideAndRemainder();
		printStr("====remainder===\n");remainder(); 
		printStr("====hashCode===\n");hashCodeTest(); 
	}
	
	public BigInt fact(int n) {
		BigInt rtnVal = null;
		if (n <= 1) {
			rtnVal = newBigInt().init(1);
		}
		else {
			rtnVal = newBigInt().init(n).multiply(fact(n-1));
		}
		return rtnVal;
	}
	
	public void abs() {
		for (int i = 0; i < bigs.length; i++) {
			printStr(bigs[i].abs().toString().concat("\n"));
		}
		for (int i = 0; i < primes.length; i++) {
			printStr(primes[i].abs().toString().concat("\n"));
		}
	}
	public void add() {
		for (int i = 0; i < bigs.length; i++) {
			for (int j = 0; j < bigs.length; j++) {
				printStr(bigs[i].add(bigs[j]).toString().concat("\n"));
			}
			for (int j = 0; j < primes.length; j++) {
				printStr(bigs[i].add(primes[j]).toString().concat("\n"));
			}
		}
		for (int i = 0; i < primes.length; i++) {
			for (int j = 0; j < bigs.length; j++) {
				printStr(primes[i].add(bigs[j]).toString().concat("\n"));
			}
			for (int j = 0; j < primes.length; j++) {
				printStr(primes[i].add(primes[j]).toString().concat("\n"));
			}
		}
	}
	public void compareTo() {
		for (int i = 0; i < bigs.length; i++) {
			for (int j = 0; j < bigs.length; j++) {
				printInt(bigs[i].compareTo(bigs[j]));
				printStr("\n");
			}
			for (int j = 0; j < primes.length; j++) {
				printInt(bigs[i].compareTo(primes[j]));
				printStr("\n");
			}
		}
		for (int i = 0; i < primes.length; i++) {
			for (int j = 0; j < bigs.length; j++) {
				printInt(primes[i].compareTo(bigs[j]));
				printStr("\n");
			}
			for (int j = 0; j < primes.length; j++) {
				printInt(primes[i].compareTo(primes[j]));
				printStr("\n");
			}
		}
	}
	public String helpDiv(BigInt a, BigInt b) {
		String rtnVal = "**DIV-BY=ZERO**";
		if (!b.equals(zero)) {
			rtnVal = a.divide(b).toString();
		}
		return rtnVal;		
	}
	
	public void divide() {
		for (int i = 0; i < bigs.length; i++) {
			for (int j = 0; j < bigs.length; j++) {
				printStr(helpDiv(bigs[i], bigs[j]).concat("\n"));
			}
			for (int j = 0; j < primes.length; j++) {
				printStr(helpDiv(bigs[i], primes[j]).concat("\n"));
			}
		}
		for (int i = 0; i < primes.length; i++) {
			for (int j = 0; j < bigs.length; j++) {
				printStr(helpDiv(primes[i], bigs[j]).concat("\n"));
			}
			for (int j = 0; j < primes.length; j++) {
				printStr(helpDiv(primes[i], primes[j]).concat("\n"));
			}
		}
	}
	

	public String helpDivRem(BigInt a, BigInt b) {
		String rtnVal = "**DIV-BY=ZERO**";
		if (!b.equals(zero)) {
			BigInt[] temp = a.divideAndRemainder(b);
			rtnVal = temp[0].toString().concat(",").concat(temp[1].toString());
		}
		return rtnVal;		
	}
	
	public void divideAndRemainder() {
		for (int i = 0; i < bigs.length; i++) {
			for (int j = 0; j < bigs.length; j++) {
				printStr(helpDivRem(bigs[i], bigs[j]).concat("\n"));
			}
			for (int j = 0; j < primes.length; j++) {
				printStr(helpDivRem(bigs[i], primes[j]).concat("\n"));
			}
		}
		for (int i = 0; i < primes.length; i++) {
			for (int j = 0; j < bigs.length; j++) {
				printStr(helpDivRem(primes[i], bigs[j]).concat("\n"));
			}
			for (int j = 0; j < primes.length; j++) {
				printStr(helpDivRem(primes[i], primes[j]).concat("\n"));
			}
		}
	}
	
	public void equalsTest() {
		for (int i = 0; i < bigs.length; i++) {
			for (int j = 0; j < bigs.length; j++) {
				printBool(bigs[i].equals(bigs[j]));
				printStr("\n");
			}
			for (int j = 0; j < primes.length; j++) {
				printBool(bigs[i].equals(primes[j]));
				printStr("\n");
			}
		}
		for (int i = 0; i < primes.length; i++) {
			for (int j = 0; j < bigs.length; j++) {
				printBool(primes[i].equals(bigs[j]));
				printStr("\n");
			}
			for (int j = 0; j < primes.length; j++) {
				printBool(primes[i].equals(primes[j]));
				printStr("\n");
			}
		}
	}
	
	public void hashCodeTest() {
		for (int i = 0; i < bigs.length; i++) {
			printInt(bigs[i].hashCode());
			printStr("\n");
		}
		for (int i = 0; i < primes.length; i++) {
			printInt(primes[i].hashCode());
			printStr("\n");
		}		
	}
	
	public void intValue() {
		for (int i = 0; i < bigs.length; i++) {
			printInt(bigs[i].intValue());
			printStr("\n");
		}
		for (int i = 0; i < primes.length; i++) {
			printInt(primes[i].intValue());
			printStr("\n");
		}		
	}
	
	public void max() {
		for (int i = 0; i < bigs.length; i++) {
			for (int j = 0; j < bigs.length; j++) {
				printStr(bigs[i].max(bigs[j]).toString().concat("\n"));
			}
			for (int j = 0; j < primes.length; j++) {
				printStr(bigs[i].max(primes[j]).toString().concat("\n"));
			}
		}
		for (int i = 0; i < primes.length; i++) {
			for (int j = 0; j < bigs.length; j++) {
				printStr(primes[i].max(bigs[j]).toString().concat("\n"));
			}
			for (int j = 0; j < primes.length; j++) {
				printStr(primes[i].max(primes[j]).toString().concat("\n"));
			}
		}
	}
	
	public void min() {
		for (int i = 0; i < bigs.length; i++) {
			for (int j = 0; j < bigs.length; j++) {
				printStr(bigs[i].min(bigs[j]).toString().concat("\n"));
			}
			for (int j = 0; j < primes.length; j++) {
				printStr(bigs[i].min(primes[j]).toString().concat("\n"));
			}
		}
		for (int i = 0; i < primes.length; i++) {
			for (int j = 0; j < bigs.length; j++) {
				printStr(primes[i].min(bigs[j]).toString().concat("\n"));
			}
			for (int j = 0; j < primes.length; j++) {
				printStr(primes[i].min(primes[j]).toString().concat("\n"));
			}
		}
	}
	
	public void multiply() {
		for (int i = 0; i < bigs.length; i++) {
			for (int j = 0; j < bigs.length; j++) {
				printStr(bigs[i].multiply(bigs[j]).toString().concat("\n"));
			}
			for (int j = 0; j < primes.length; j++) {
				printStr(bigs[i].multiply(primes[j]).toString().concat("\n"));
			}
		}
		for (int i = 0; i < primes.length; i++) {
			for (int j = 0; j < bigs.length; j++) {
				printStr(primes[i].multiply(bigs[j]).toString().concat("\n"));
			}
			for (int j = 0; j < primes.length; j++) {
				printStr(primes[i].multiply(primes[j]).toString().concat("\n"));
			}
		}
	}
	
	public void negate() {
		for (int i = 0; i < bigs.length; i++) {
			printStr(bigs[i].negate().toString().concat("\n"));
		}
		for (int i = 0; i < primes.length; i++) {
			printStr(primes[i].negate().toString().concat("\n"));
		}
	}

	public String helpRem(BigInt a, BigInt b) {
		String rtnVal = "**DIV-BY=ZERO**";
		if (!b.equals(zero)) {
			rtnVal = a.remainder(b).toString();
		}
		return rtnVal;		
	}
	
	public void remainder() {
		for (int i = 0; i < bigs.length; i++) {
			for (int j = 0; j < bigs.length; j++) {
				printStr(helpRem(bigs[i], bigs[j]).concat("\n"));
			}
			for (int j = 0; j < primes.length; j++) {
				printStr(helpRem(bigs[i], primes[j]).concat("\n"));
			}
		}
		for (int i = 0; i < primes.length; i++) {
			for (int j = 0; j < bigs.length; j++) {
				printStr(helpRem(primes[i], bigs[j]).concat("\n"));
			}
			for (int j = 0; j < primes.length; j++) {
				printStr(helpRem(primes[i], primes[j]).concat("\n"));
			}
		}
	}
	public void signum() {
		for (int i = 0; i < bigs.length; i++) {
			printInt(bigs[i].signum());
			printStr("\n");
		}
		for (int i = 0; i < primes.length; i++) {
			printInt(primes[i].signum());
			printStr("\n");
		}		
	}
	
	public void subtract() {
		for (int i = 0; i < bigs.length; i++) {
			for (int j = 0; j < bigs.length; j++) {
				printStr(bigs[i].subtract(bigs[j]).toString().concat("\n"));
			}
			for (int j = 0; j < primes.length; j++) {
				printStr(bigs[i].subtract(primes[j]).toString().concat("\n"));
			}
		}
		for (int i = 0; i < primes.length; i++) {
			for (int j = 0; j < bigs.length; j++) {
				printStr(primes[i].subtract(bigs[j]).toString().concat("\n"));
			}
			for (int j = 0; j < primes.length; j++) {
				printStr(primes[i].subtract(primes[j]).toString().concat("\n"));
			}
		}
	}
	
	public void toStringTest() {
		for (int i = 0; i < bigs.length; i++) {
			printStr(bigs[i].toString());
			printStr("\n");
		}
		for (int i = 0; i < primes.length; i++) {
			printStr(primes[i].toString());
			printStr("\n");
		}		
	}
	
}


class BigInt {
	String data;
	boolean isNeg;
	
	// pseudo-constructor
	public BigInt init(int n) {
		isNeg = n < 0;
		if (isNeg) {
			n = -n;
		}
		data = new Lib().intToString(n);
		if (n < 0) { // MIN_INT case: remove '-' sign
			data = data.substring(1,data.length());
		}
		return this;
	}
	
	// this version is intended to be private
	public BigInt init2(String data, boolean isNeg) {
		this.data = data;
		this.isNeg = isNeg;
		return this;
	}
	
	public String toString() {
		String rtnVal = data;
		if (isNeg) {
			rtnVal = "-".concat(data);
		}
		return rtnVal;
	}
	
	public BigInt add(BigInt other) {
		BigInt rtnVal = null;
		String orig1 = this.data;
		String orig2 = other.data;
		if (this.isNeg == other.isNeg) {
			rtnVal = primitiveAdd(orig1, orig2, false);
			rtnVal.isNeg = this.isNeg;
		}
		else if (this.isNeg) {
			rtnVal = primitiveAdd(orig2, orig1, true);
		}
		else {
			rtnVal = primitiveAdd(orig1, orig2, true);
		}
		return rtnVal;		
	}
	
	public BigInt subtract(BigInt other) {
		BigInt rtnVal = null;
		String orig1 = this.data;
		String orig2 = other.data;
		if (this.isNeg != other.isNeg) {
			rtnVal = primitiveAdd(orig1, orig2, false);
			rtnVal.isNeg = this.isNeg;
		}
		else if (this.isNeg) {
			rtnVal = primitiveAdd(orig2, orig1, true);
		}
		else {
			rtnVal = primitiveAdd(orig1, orig2, true);
		}
		return rtnVal;		
	}
	
	public BigInt negate() {
		BigInt rtnVal = this;
		if (!this.data.equals("0")) {
			rtnVal = new BigInt().init2(this.data,!this.isNeg);
		}
		return rtnVal;
	}
	
	public BigInt multiply(BigInt other) {
		BigInt rtnVal = null;
		if (this.data.equals("0")) {
			rtnVal = this;
		}
		else if (other.data.equals("0")) {
			rtnVal = other;
		}
		else {
			rtnVal =
					new BigInt().
					init2(primitiveMult(this.data, other.data),
							this.isNeg != other.isNeg);
		}
		return rtnVal;
	}
	
	public BigInt divide(BigInt other) {
		// rules (from Java):
		// - if divisor is zero, Java throws an exception; so we will also
		// - otherwise, rounding is towards 0, with the sign being:
		//   - zero if the |numerator| < |denominator|
		//   - otherwise:
		//     - if numerator and denominator have the same sign, positive
		//     - if numerator and denominator have different signs, negative
		// So this can be implemented by:
		// - first convert both operands to that their signs are positive
		// - then flip sign if numerator and denominator have different signs
		BigInt rtnVal = null;
		if (other.data.equals("0")) {
			if (1/0 == 0); // throws an exception
		}
		else {
			String prim = primitiveDivide(this.data, other.data);
			rtnVal = new BigInt()
					.init2(prim, !prim.equals("0") && this.isNeg != other.isNeg);
		}
		return rtnVal;
	}
	
	public BigInt abs() {
		BigInt rtnVal = this;
		if (this.isNeg) {
			rtnVal = new BigInt().init2(this.data,false);
		}
		return rtnVal;
	}
	
	public int compareTo(BigInt other) {
		int rtnVal = 0;
		if (this.isNeg != other.isNeg) {
			if (this.isNeg) rtnVal = -1;
			else rtnVal = 1;
		}
		else {
			rtnVal = primitiveCompare(this.data, other.data);
			if (rtnVal > 0) rtnVal = 1;
			else if (rtnVal < 0) rtnVal = -1;
			if (this.isNeg) {
				rtnVal = -rtnVal; 
			}
		}
		return rtnVal;
	}
	
	public boolean equals(Object other) {
		boolean rtnVal = false;
		if (other instanceof BigInt) {
			BigInt other2 = (BigInt)other;
			rtnVal = this.data.equals(other2.data) && this.isNeg == other2.isNeg;
		}
		return rtnVal;
	}
	
	public int hashCode() {
		// We are trying to be consistent with BigInteger. So we treat the operand
		// as a radix-2^32 number, and multiply the digit in each position, k, by
		// 31^k, and then sum all the values together. For negative numbers, the hash
		// value is simply the negation of the hash value of the corresponding positive number.
		int rtnVal = this.abs().helpHashCode(new BigInt().init2("4294967296", false));
		if (this.isNeg) {
			rtnVal = -rtnVal;
		}
		return rtnVal;
	}
	
	public int helpHashCode(BigInt twoToThe32) {
		int rtnVal = 0;
		if (!this.data.equals("0")) {
			BigInt[] divRem = this.divideAndRemainder(twoToThe32);
			rtnVal = 31*divRem[0].helpHashCode(twoToThe32) + divRem[1].intValue();
		}
		return rtnVal;
	}
	
	public String primitiveMult(String s1, String s2) {
		String result = "0";
		while (!s1.equals("0")) {
			boolean s1Even = s1.charAt(s1.length()-1) % 2 == 0;
			if (!s1Even) {
				result = primitiveAdd(result,s2,false).data;
			}
			s2 = primitiveAdd(s2,s2,false).data;
			s1 = div2(s1);
		}
		return result;
	}
	
	public int intValue() {
		int rtnVal = 0;
		for (int i = 0; i < data.length(); i++) {
			rtnVal = rtnVal * 10;
			rtnVal = rtnVal + data.charAt(i)-'0';
		}
		if (isNeg) {
			rtnVal = -rtnVal;
		}
		return rtnVal;
	}
	
	public BigInt max(BigInt other) {
		BigInt rtnVal = this;
		if (this.compareTo(other) < 0) {
			rtnVal = other;
		}
		return rtnVal;
	}
	
	public BigInt min(BigInt other) {
		BigInt rtnVal = this;
		if (this.compareTo(other) > 0) {
			rtnVal = other;
		}
		return rtnVal;
	}
	
	public int signum() {
		int rtnVal = -1;
		if (!isNeg) {
			if (this.data.equals("0")) {
				rtnVal = 0;
			}
			else {
				rtnVal = 1;
			}
		}
		return rtnVal;
	}
	
	public BigInt remainder(BigInt other) {
		BigInt quotient = this.divide(other);
		return this.subtract(quotient.multiply(other));
	}
	
	public BigInt[] divideAndRemainder(BigInt other) {
		BigInt[] rtnVal = new BigInt[2];
		BigInt quotient = this.divide(other);
		BigInt remainder = this.subtract(quotient.multiply(other));
		rtnVal[0] = quotient;
		rtnVal[1] = remainder;
		return rtnVal;
	}	
	
	// requires s1 to be non-negative, s2 to be positive
	public String primitiveDivide(String s1, String s2) {
		String recip = primitiveReciprocal(s2, s1.length());
		String prod = primitiveMult(s1,recip);
		int limit = prod.length()-s1.length();
		if (limit <= 0) {
			prod = "0";
		}
		else {
			prod = prod.substring(0, prod.length()-s1.length());
		}
		String mulBack = primitiveMult(prod, s2);
		int compareResult = primitiveCompare(mulBack, s1);
		if (compareResult > 0) {
			do {
				mulBack = primitiveAdd(mulBack,s2,true).data; // decrement divistor
				prod = primitiveAdd(prod,"1",true).data; // decrement by 1
				// what if the sign flips, but we only use the absolute value (because
				// we are throwing away the sign). Can this happen? might this cause
				// trouble.
			} while (primitiveCompare(mulBack, s1) > 0);
		}
		else if (compareResult < 0){
			for (;;) {
				mulBack = primitiveAdd(mulBack,s2,false).data;
				if (primitiveCompare(mulBack, s1) > 0) {
					break;
				}
				prod = primitiveAdd(prod,"1",false).data;
			}
		}
		return prod;
	}
	
	public String div2(String s) {
		String rtnVal = primitiveAdd(s,s,false).data; // times 2
		rtnVal = primitiveAdd(rtnVal,rtnVal,false).data; // times 4
		rtnVal = primitiveAdd(rtnVal,s,false).data; // times 5
		rtnVal = rtnVal.substring(0,rtnVal.length()-1); // div by 10, so effect is div by 2;
		if (rtnVal.length() == 0) {
			rtnVal = "0";
		}
		return rtnVal;
	}
	
	// returns neg/zero/pos number, depending comparison of s1 and s2,
	// treated as positive decimal integers
	public int primitiveCompare(String s1, String s2) {
		int rtnVal = 0;
		int len1 = s1.length();
		int len2 = s2.length();
		if (len1 < len2) {
			rtnVal = -1;
		}
		else if (len1 > len2) {
			rtnVal = 1;
		}
		else {
			rtnVal = s1.compareTo(s2);
		}
		return rtnVal;
	}
	
	// assumes both operands represent non-negative numbers
	public BigInt primitiveAdd(String s1, String s2, boolean negateS2) {
		String dataResult = "";
		BigInt result = null;
		int len1 = s1.length();
		int len2 = s2.length();
		if (len1 < len2) {
			s1 = repeatString("0",len2-len1).concat(s1);
		}
		else {
			s2 = repeatString("0",len1-len2).concat(s2);
		}
		int carryOut = 0;
		for (int i = s1.length()-1; i >= 0; i--) {
			int val1 = s1.charAt(i) - '0';
			int val2 = s2.charAt(i) - '0';
			if (negateS2) {
				val2 = -val2;
			}
			int sum = val1+val2+carryOut;
			if (sum >= 10) {
				sum = sum - 10;
				carryOut = 1;
			}
			else if (sum < 0) {
				sum = sum + 10;
				carryOut = -1;
			}
			else {
				carryOut = 0;
			}
			String digit = "";
			switch (sum) {
			case 0: digit = "0"; break;
			case 1: digit = "1"; break;
			case 2: digit = "2"; break;
			case 3: digit = "3"; break;
			case 4: digit = "4"; break;
			case 5: digit = "5"; break;
			case 6: digit = "6"; break;
			case 7: digit = "7"; break;
			case 8: digit = "8"; break;
			case 9: digit = "9"; break;
			}
			dataResult = digit.concat(dataResult);
		}
		if (carryOut >= 0) {
			result = new BigInt().init2(dataResult,false);
			if (carryOut == 1) {
				result.data = "1".concat(dataResult);
			}
		}
		else if (carryOut == -1) {
			String base = "1".concat(repeatString("0",dataResult.length()));
			result = primitiveAdd(base,dataResult,true);
			result.isNeg = true;
		}
		result.data = truncateLeadingZeros(result.data);
		return result;
	}
	
	public String primitiveReciprocal(String s, int digits) {
		// we have an integer x, with length N, and we want a scaled
		// reciprocal, r, such that x*r ~ 10^2N. So we want r = 10^2N/x.
		// Let's try the newton-formula f(x) = C - 10^2N*x^-1
		// So f'(x) = -10^2N/x^-2
		// So x1 = x0 - f(x0)/f'(x0)
		//   = x0 - (C-10^2N*x0^-1 / -10^2N/x0^-2)
		//   = x0 - C*x0^2/10^2N + x0
		//   = 2x0 - C*x0^2/10^2N

		String x0 = "1";

		if (!s.equals("0")) {

			if (s.length() > digits) {
				x0 = "0";
			}
			else {

				// initial approx.

				int startLen = digits - s.length();
				if (startLen < 0) {
					startLen = 0;
				}
				x0 = "1".concat(repeatString("1", startLen));

				String old = "";
				for (;;) {
					String old2 = old;
					old = x0;
					String temp1 = primitiveAdd(x0,x0,false).data;
					String temp = primitiveMult(s,x0);
					temp = primitiveMult(temp,x0);
					temp = temp.substring(0,temp.length()-digits);
					x0 = primitiveAdd(temp1,temp,true).data;
					if (x0.equals(old) || x0.equals(old2)) break;
				}
			}
		}


		return x0;
	}
	
	// count should be non-negative
	public String repeatString(String s, int count) {
		String rtnVal = "";
		if (count == 1) {
			rtnVal = s;
		}
		else if (count > 1) {
			String half = repeatString(s, count/2);
			rtnVal = repeatString(s,count%2).concat(half).concat(half);
		}
		return rtnVal;
	}
	
	public String truncateLeadingZeros(String s) {
		int idx = 0;
		while (idx < s.length()-1 && s.charAt(idx) == '0') {
			idx++;
		}
		return s.substring(idx,s.length());
	}

}

//class BigInt2 extends BigInt {
//	BigInteger val;
//	
//	public BigInt2 init(int n) {
//		val = new BigInteger(""+n);
//		return this;
//	}
//	
//	private BigInt2 init2(BigInteger val) {
//		this.val = val;
//		return this;
//	}
//
//	public String toString() {
//		return val.toString();
//	}
//	
//	public BigInt add(BigInt other) {
//		return new BigInt2().init2(val.add(((BigInt2)other).val));	
//	}
//	
//	public BigInt subtract(BigInt other) {
//		return new BigInt2().init2(val.subtract(((BigInt2)other).val));	
//
//	}
//	
//	public BigInt negate() {
//		return new BigInt2().init2(val.negate());	
//	}
//	
//	public BigInt multiply(BigInt other) {
//		return new BigInt2().init2(val.multiply(((BigInt2)other).val));
//	}
//	
//	public BigInt divide(BigInt other) {
//		return new BigInt2().init2(val.divide(((BigInt2)other).val));
//	}
//	
//	public BigInt abs() {
//		return new BigInt2().init2(val.abs());
//
//	}
//	
//	public int compareTo(BigInt other) {
//		return val.compareTo(((BigInt2)other).val);
//
//	}
//	
//	public boolean equals(Object other) {
//		return val.equals(((BigInt2)other).val);
//	}
//	
//	public int hashCode() {
//		return val.hashCode();
//	}
//	
//	public int intValue() {
//		return val.intValue();
//	}
//	
//	public BigInt max(BigInt other) {
//		return new BigInt2().init2(val.max(((BigInt2)other).val));
//	}
//	
//	public BigInt min(BigInt other) {
//		return new BigInt2().init2(val.min(((BigInt2)other).val));
//	}
//	
//	public int signum() {
//		return val.signum();
//	}
//	
//	public BigInt remainder(BigInt other) {
//		return new BigInt2().init2(val.remainder(((BigInt2)other).val));
//	}
//	
//	public BigInt[] divideAndRemainder(BigInt other) {
//		BigInteger[] tempResult = val.divideAndRemainder(((BigInt2)other).val);
//		return new BigInt[]{new BigInt2().init2(tempResult[0]), new BigInt2().init2(tempResult[1])};
//	}
//
//}
