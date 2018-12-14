import java.io.Serializable;
import java.util.Random;
import java.math.BigInteger;

public class LargeInteger implements Serializable {

	private final byte[] ZERO = {(byte) 0};
	private final byte[] ONE = {(byte) 1};

	private byte[] val;

	/**
	 * Construct the LargeInteger from a given byte array
	 * @param b the byte array that this LargeInteger should represent
	 */
	public LargeInteger(byte[] b) {
		val = b;
	}

	public LargeInteger(LargeInteger b) {
		val = b.getVal();
	}

	/**
	 * Construct the LargeInteger by generatin a random n-bit number that is
	 * probably prime (2^-100 chance of being composite).
	 * @param n the bitlength of the requested integer
	 * @param rnd instance of java.util.Random to use in prime generation
	 */
	public LargeInteger(int n, Random rnd) {
		val = BigInteger.probablePrime(n, rnd).toByteArray();
	}
	
	/**
	 * Return this LargeInteger's val
	 * @return val
	 */
	public byte[] getVal() {
		return val;
	}

	/**
	 * Return the number of bytes in val
	 * @return length of the val byte array
	 */
	public int length() {
		return val.length;
	}

	/** 
	 * Add a new byte as the most significant in this
	 * @param extension the byte to place as most significant
	 */
	public void extend(byte extension) {
		byte[] newv = new byte[val.length + 1];
		newv[0] = extension;
		for (int i = 0; i < val.length; i++) {
			newv[i + 1] = val[i];
		}
		val = newv;
	}

	public LargeInteger trimLeadingBytes() {
		int padding = getPaddingAmount()-1;
		int leading = padding / 8;
		if(padding > 8) {
			byte[] newb = new byte[val.length-leading];
			System.arraycopy(val, leading, newb, 0, val.length-leading);
			return new LargeInteger(newb);
		}
		return this;
	}

	/**
	 * If this is negative, most significant bit will be 1 meaning most 
	 * significant byte will be a negative signed number
	 * @return true if this is negative, false if positive
	 */
	public boolean isNegative() {
		return (val[0] < 0);
	}

	public boolean isZero() {
		for(byte b : val) if(b != 0) return false;
		return true;
	}
	/**
	 * Computes the sum of this and other
	 * @param other the other LargeInteger to sum with this
	 */
	public LargeInteger add(LargeInteger other) {
		byte[] a, b;
		// If operands are of different sizes, put larger first ...
		if (val.length < other.length()) {
			a = other.getVal();
			b = val;
		}
		else {
			a = val;
			b = other.getVal();
		}

		// ... and normalize size for convenience
		if (b.length < a.length) {
			int diff = a.length - b.length;

			byte pad = (byte) 0;
			if (b[0] < 0) {
				pad = (byte) 0xFF;
			}

			byte[] newb = new byte[a.length];
			for (int i = 0; i < diff; i++) {
				newb[i] = pad;
			}

			for (int i = 0; i < b.length; i++) {
				newb[i + diff] = b[i];
			}

			b = newb;
		}

		// Actually compute the add
		int carry = 0;
		byte[] res = new byte[a.length];
		for (int i = a.length - 1; i >= 0; i--) {
			// Be sure to bitmask so that cast of negative bytes does not
			//  introduce spurious 1 bits into result of cast
			carry = ((int) a[i] & 0xFF) + ((int) b[i] & 0xFF) + carry;

			// Assign to next byte
			res[i] = (byte) (carry & 0xFF);

			// Carry remainder over to next byte (always want to shift in 0s)
			carry = carry >>> 8;
		}

		LargeInteger res_li = new LargeInteger(res);
	
		// If both operands are positive, magnitude could increase as a result
		//  of addition
		if (!this.isNegative() && !other.isNegative()) {
			// If we have either a leftover carry value or we used the last
			//  bit in the most significant byte, we need to extend the result
			if (res_li.isNegative()) {
				res_li.extend((byte) carry);
			}
		}
		// Magnitude could also increase if both operands are negative
		else if (this.isNegative() && other.isNegative()) {
			if (!res_li.isNegative()) {
				res_li.extend((byte) 0xFF);
			}
		}

		// Note that result will always be the same size as biggest input
		//  (e.g., -127 + 128 will use 2 bytes to store the result value 1)
		return res_li;
	}

	/**
	 * Negate val using two's complement representation
	 * @return negation of this
	 */
	public LargeInteger negate() {
		byte[] neg = new byte[val.length];
		int offset = 0;

		// Check to ensure we can represent negation in same length
		//  (e.g., -128 can be represented in 8 bits using two's 
		//  complement, +128 requires 9)
		if (val[0] == (byte) 0x80) { // 0x80 is 10000000
			boolean needs_ex = true;
			for (int i = 1; i < val.length; i++) {
				if (val[i] != (byte) 0) {
					needs_ex = false;
					break;
				}
			}
			// if first byte is 0x80 and all others are 0, must extend
			if (needs_ex) {
				neg = new byte[val.length + 1];
				neg[0] = (byte) 0;
				offset = 1;
			}
		}

		// flip all bits
		for (int i  = 0; i < val.length; i++) {
			neg[i + offset] = (byte) ~val[i];
		}

		LargeInteger neg_li = new LargeInteger(neg);
	
		// add 1 to complete two's complement negation
		return neg_li.add(new LargeInteger(ONE));
	}

	/**
	 * Implement subtraction as simply negation and addition
	 * @param other LargeInteger to subtract from this
	 * @return difference of this and other
	 */
	public LargeInteger subtract(LargeInteger other) {
		return this.add(other.negate());
	}


	/**
		Finds the bit amount of additional padding
	 	Ex
	 	11111111 11000001 00000010 -> 10
	 	11111000 11000001 00000010 -> 6
	    00111000 11000001 00000010 -> 2
	 */
	public int getPaddingAmount() {
		int i = 0;
		byte current = val[i];
		int first_bit = current >> 7 & 1;
		byte padded = (byte)(first_bit == 1 ? 0xFF : 0);
		int padding_amount = 0;
		// Add 8 bits for every byte that is padding
		while(current == padded && i+1 < val.length) {
			current = val[++i];
			padding_amount += 8;
		}
		int current_bit = 7;
		while((current >> current_bit & 0x01) == first_bit && current_bit>=0) {
			current_bit--;
			padding_amount++;
		}
		return padding_amount;
	}

	/**
	 * Complicated method to shift in O(n) time
	 * It is really confusing, because it does bitmasking and ORing to combine bytes when a shift causes a break in a byte.
	 *
	 * For example, 001000011 001000011
	 *
	 * is done that requires the bytes to be split
	 *
	 * For bytes, arithmetic shift and logical shift do the same thing which
	 * requires more bitmasking.
	 */
	private LargeInteger leftShift(int amount, boolean shiftWithOne) {
		if(amount == 0) return this;

		int padding_amount = getPaddingAmount() % 8;
		int left_over_shift = amount % 8;
		int num_of_shifted_bytes = amount/8 + ((amount%8 ==0 || left_over_shift < padding_amount) ? 0: 1);
		if(left_over_shift == 0) left_over_shift = 8;
		int right_shift_amount = 8 - left_over_shift;

		byte[] shifted = new byte[val.length + num_of_shifted_bytes];

		byte[] onesShiftMask = {0, 0b00000001, 0b00000011,0b00000111,0b00001111,0b00011111,0b00111111,0b01111111,-1};
		byte [] leftBitMask = {-1,  -2, -4, -8, -16, -32, -64, -128, 0};
		byte [] rightBitMask = {-1, 127, 63, 31, 15, 7, 3, 1};
		if(left_over_shift < padding_amount) {
			for(int i=0; i<val.length-1; i++) {
				byte left_piece = (byte)((val[i] << left_over_shift) & leftBitMask[left_over_shift]);
				byte right_piece = (byte)((val[i+1] >> right_shift_amount) & rightBitMask[right_shift_amount]);
				shifted[i] = (byte)(left_piece | right_piece);
			}
			shifted[val.length-1] =  (byte)(val[val.length-1] << left_over_shift);
			if(shiftWithOne) {
				shifted[val.length-1] = (byte)(shifted[val.length-1] | onesShiftMask[left_over_shift]);
				for(int i=val.length; i<shifted.length; i++) shifted[i] = (byte)(0xFF);
			}

		} else {
			shifted[0] = (byte) (val[0] >>> right_shift_amount);
			for(int i=1; i<val.length; i++) {
				byte left_piece = (byte)((val[i-1] << left_over_shift) & leftBitMask[left_over_shift]);
				byte right_piece = ((byte)((val[i] >>> right_shift_amount) & rightBitMask[right_shift_amount]));
				shifted[i] = (byte) (left_piece | right_piece);
			}
			shifted[val.length] = (byte)(val[val.length-1] << left_over_shift);
			if(shiftWithOne) {
				shifted[val.length] = (byte)(shifted[val.length] | onesShiftMask[left_over_shift]);
				for(int i=val.length+1; i<shifted.length; i++) shifted[i] = (byte)(0xFF);
			}
		}

		return new LargeInteger(shifted);
	}

	public LargeInteger leftShift(int amount) {
		return leftShift(amount, false);
	}

	public LargeInteger leftShiftWithZero(int amount) {
		return leftShift(amount, false);
	}

	public LargeInteger leftShiftWithOne(int amount) {
		return leftShift(amount, true);
	}

	/**
	 * Right shift (only by 1 bit)
	 * Used in division
	 */
	public LargeInteger rightShiftByOne() {
		byte[] shifted = new byte[val.length];
		int mem = val[0] & 1;
		shifted[0] = (byte) (val[0] >> 1);
		for(int i=1; i<val.length; i++) {
			byte current = val[i];
			int lsb = current & 1;
			byte mem_mask = (byte)(mem == 1 ? 0b10000000 : 0);
			shifted[i] = (byte)(current >> 1 & 0b01111111 | mem_mask);
			mem = lsb;
		}
		return new LargeInteger(shifted);
	}

	/**
	 * Returns a new LargeInteger sign extended to double the length.
	 * Might be useful for multiplication
	 */
	public LargeInteger signExtend() {
		byte[] extended = new byte[val.length * 2];
		if(this.isNegative())
			for(int i=0; i<val.length; i++) extended[i] = (byte) (0xFF);
		System.arraycopy(val, 0, extended, val.length, val.length);
		return new LargeInteger(extended);
	}

	/**
	 * Compute the product of this and other
	 * @param other LargeInteger to multiply by this
	 * @return product of this and other
	 */
	public LargeInteger multiply(LargeInteger other) {
		LargeInteger a, b;

		if(this.isZero() || other.isZero()) return new LargeInteger(ZERO);

		// If operands are of different sizes, put larger first ...
		if (val.length < other.length()) {
			a = other;
			b = this;
		}
		else {
			a = this;
			b = other;
		}

		boolean res_is_neg = (a.isNegative() && !b.isNegative()) || (!a.isNegative() && b.isNegative());

		if(a.isNegative() && b.isNegative()) {
			a = a.negate();
			b = b.negate();
		} else if(a.isNegative() && !b.isNegative()) {
			a = a.negate();
		} else if(!a.isNegative() && b.isNegative()) {
			b = b.negate();
		}

		// Double sign extend both
//		a = a.signExtend();
//		b = b.signExtend();

		int shift = 0;
		LargeInteger res = new LargeInteger(ZERO);
		byte[] vals = b.getVal();

		// do grade school algorithm
		for(int current=vals.length-1; current>=0; current--) {
			byte b_digit = vals[current];
			for(int i=0; i<8; i++) {
				int current_bit = (b_digit >> i) & 1;
				if(current_bit == 1) {
					res = res.add(a.leftShift(shift));
				}
				shift++;
			}
		}

		if(res_is_neg) {
			res = res.negate();
		}

		return res.trimLeadingBytes();
	}
	
	/**
	 * Run the extended Euclidean algorithm on this and other
	 * @param other another LargeInteger
	 * @return an array structured as follows:
	 *   0:  the GCD of this and other
	 *   1:  a valid x value
	 *   2:  a valid y value
	 * such that this * x + other * y == GCD in index 0
	 * Algorithm taken from Sedgewick.
	 * https://introcs.cs.princeton.edu/java/99crypto/ExtendedEuclid.java.html
	 */
	 public LargeInteger[] XGCD(LargeInteger other) {
		 if(other.isZero()) return new LargeInteger[]{ this, new LargeInteger(ONE), new LargeInteger(ZERO)};

		 LargeInteger[] vals = other.XGCD(this.mod(other));
		 LargeInteger d = vals[0];
		 LargeInteger a = vals[2];
		 LargeInteger div = this.divide(other)[0];
		 LargeInteger b = vals[1].subtract(div.multiply(a));
		 return new LargeInteger[] {d, a, b};
	 }

	 /**
	  * Modulo, only runs with positve numbers
	  */
	 public LargeInteger mod(LargeInteger divisor) {
	 	return this.divide(divisor)[1];
	 }

	 /**
	  * Compute the result of raising this to the power of y mod n
	  * @param y exponent to raise this to
	  * @param n modulus value to use
	  * @return this^y mod n
	  * Test with https://www.omnicalculator.com/math/exponent
	  */
	 public LargeInteger modularExp(LargeInteger y, LargeInteger n) {
	 	byte[] exponent = y.getVal();
	 	LargeInteger ans = new LargeInteger(ONE);
	 	for(byte digit : exponent) {
	 		for(int i = 7; i >= 0; i--) {
				ans = ans.multiply(ans).mod(n);
				int current_bit = (digit >> i) & 1;
				if(current_bit == 1) {
					ans = ans.multiply(this).mod(n);
				}
			}
		}
		return ans;
	 }

	/**
	 *  Division algorithm,
	 *  Thanks to help from CS 447 Slides
	 *  Returns [Quotient, Remainder]
	 */

	public LargeInteger[] divide(LargeInteger divisor) {
		LargeInteger dividend = this;
		boolean quotient_is_neg = (this.isNegative() && !divisor.isNegative()) || (!this.isNegative() && divisor.isNegative());
		boolean remainder_is_neg = dividend.isNegative();
		if(divisor.isNegative()) divisor = divisor.negate();
		if(dividend.isNegative()) dividend = dividend.negate();

		// Dividend and Remainder have the same sign
		// Quotient is negative if signs different


		// Handle edge cases

		if(dividend.equals(divisor)) {
			LargeInteger one = new LargeInteger(ONE);
			if(quotient_is_neg) one = one.negate();
			return new LargeInteger[] { one, new LargeInteger(ZERO)};
		}

		if(dividend.lt(divisor)) {
			if(remainder_is_neg) dividend = dividend.negate();
			return new LargeInteger[] { new LargeInteger(ZERO), dividend};
		}

		// Perform 447 Algorithm:

		int shift_amt = dividend.length() * 8 - 1;
		divisor = divisor.leftShift(shift_amt);
		int i = 0;
		LargeInteger remainder = dividend;
		LargeInteger quotient = new LargeInteger(ZERO);

		while(i <= shift_amt) {
			LargeInteger difference = remainder.subtract(divisor);
			if(difference.isNegative()) {
				quotient = quotient.leftShiftWithZero(1);
			} else {
				quotient = quotient.leftShiftWithOne(1);
				remainder = difference;
			}
			divisor = divisor.rightShiftByOne();
			i++;
		}

		quotient = quotient.trimLeadingBytes();
		remainder = remainder.trimLeadingBytes();
		if(remainder_is_neg && !remainder.isZero()) remainder = remainder.negate();
		if(quotient_is_neg && !quotient.isZero()) quotient = quotient.negate();

		return new LargeInteger[]{quotient, remainder};
	}


	/**
		Java compareTo method, useful for comparing needed for division.
		this < other : -1
		this > other : 1
		this == other : 0
	 */
	public int compareTo(LargeInteger other) {
		LargeInteger a_li = this.trimLeadingBytes();
		LargeInteger b_li = other.trimLeadingBytes();
		if(!a_li.isNegative() && b_li.isNegative()) return 1;
		if(a_li.isNegative() && !b_li.isNegative()) return -1;

		// when comparing negative numbers, the result is opposite
		// of when positive
		// For example, 11111011 11101111 10000000 < 11111101 10000000
		// ( -266368 < - 640)
		// because they are negative, even tho the length of a is longer.
		// For positive numbers, the opposite is true.

		// If 645 > 234, then -645 < -234.

		int if_negative_flipper = a_li.isNegative() ? -1 : 1;

		if(a_li.length() > b_li.length()) return 1 * if_negative_flipper;
		if(a_li.length() < b_li.length()) return -1 * if_negative_flipper;

		byte[] a = a_li.getVal();
		byte[] b = b_li.getVal();

		if(a_li.isNegative()) {
			a = a_li.negate().getVal();
			b = b_li.negate().getVal();
		}

		int ans = 0;

		for(int i=0; i<a.length; i++) {
			// we wish to compare to magnitude of the bytes, so & 0xFF will
			// convert byte to a unsigned integer
			int current_a_byte = a[i] & 0xFF;
			int current_b_byte = b[i] & 0xFF;
			if(current_a_byte < current_b_byte) {
				ans = -1;
				break;
			} else if(current_a_byte > current_b_byte) {
				ans = 1;
				break;
			}
		}

		return ans * if_negative_flipper;
	}

	public boolean equals(LargeInteger other) { return this.compareTo(other) == 0; }
	public boolean lt(LargeInteger other) { return this.compareTo(other) < 0; }
	public boolean gt(LargeInteger other) { return this.compareTo(other) > 0; }

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(byte b : val) {
			String s1 = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
			sb.append(s1);
		}
		return sb.toString();
	}
}
