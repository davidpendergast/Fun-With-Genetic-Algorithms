import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;


public class Alphabet {
	
	private char[] chars;
	private Random random = new Random();
	
	public Alphabet(char ... chars) {
		this.chars = chars;
	}
	
	public Alphabet(List<Character> chars) {
		this.chars = new char[chars.size()];
		for(int i = 0; i < chars.size(); i++) {
			this.chars[i] = chars.get(i).charValue();
		}
	}
	
	public char[] getChars() {
		return Arrays.copyOf(chars, chars.length);
	}
	
	public char getRandomChar() {
		return chars[random.nextInt(chars.length)];
	}	
	
	public static Alphabet getLowerCaseLetters() {
		char[] c = new char[27];
		for(int i = 0; i < 26; i++) {
			c[i] = (char)('a'+i);
		}
		c[26] = ' ';
		return new Alphabet(c);
	}
	
	public static Alphabet getUpperCaseLetters() {
		char[] c = new char[27];
		for(int i = 0; i < 26; i++) {
			c[i] = (char)('A'+i);
		}
		c[26] = ' ';
		return new Alphabet(c);
	}
	
	public static Alphabet getNumbers() {
		return new Alphabet('0', '1', '2', '3', '4', '5', '6', '7', '8', '9');
	}
	
	public static Alphabet getPunctuation() {
		return new Alphabet('.', ',', '?', '!', '\'', '"', '(', ')');
	}
	
	public static Alphabet getBits() {
		return new Alphabet('0', '1');
	}
	
	public static Alphabet combine(char[] bonus, Alphabet...alphabets) {
		Set<Character> characters = new HashSet<Character>();
		if(bonus != null) {
			for(char c : bonus) {
				characters.add(new Character(c));
			}
		}
		
		for(Alphabet a : alphabets) {
			char[] chars = a.getChars();
			for(char c : chars) {
				characters.add(new Character(c));
			}
		}
		
		List<Character> char_list = new ArrayList<Character>();
		char_list.addAll(characters);
		return new Alphabet(char_list);
	}

}
