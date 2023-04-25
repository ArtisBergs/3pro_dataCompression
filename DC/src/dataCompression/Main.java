package dataCompression;

// 211RDB330 Kārlis Jurgens
// 221RDB134 Artis Bergs

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.Scanner;


class Huffman {
	public static List<Integer> charArr = new ArrayList<Integer>();
	public static List<Integer> freqArr = new ArrayList<Integer>();
	public static List<Node> nodeArr = new ArrayList<Node>();
	public static Map<Integer, String> dictArr = new TreeMap<Integer, String>();
	
	public static boolean encode(String sourceFile, String resultFile) {
		// encode a file
		
		// read a file and fill arrays
		Files.read(sourceFile, 1);
		if(charArr.isEmpty())
			return false;
		
		// sort arrays
		boolean flag = false;
		while(flag == false) {
			flag = true;
			for(int i=0; i<freqArr.size()-1 ; i++) {
				if(freqArr.get(i) > freqArr.get(i+1)) {
					flag = false;
					
					int temp1 = freqArr.get(i);
					int temp2 = charArr.get(i);
					
					freqArr.set(i, freqArr.get(i+1));
					charArr.set(i, charArr.get(i+1));
					
					freqArr.set(i+1, temp1);
					charArr.set(i+1, temp2);
				}
			}
		}
		
		// output for testing purposes only
		System.out.println("Count:");
		for(int i=0; i<charArr.size(); i++) {
			if(freqArr.get(i) != 0)
				System.out.println(charArr.get(i) + " " + freqArr.get(i));
		}
		
		// digging the tree
		Tree.build();
		
		// output2 for testing purposes only
		System.out.println("Codes:");
		for(int num : dictArr.keySet())
			System.out.println(num + " " + dictArr.get(num));
		
		// recreate data
		String codeStr = Files.read(sourceFile, 2);
		
		// output3 for testing purposes only
		System.out.println("Code string:");
		System.out.println(codeStr);
		
		// divide binary string and.. convert to integers
		String[] arr = codeStr.split("(?<=\\G.{8})");
		
		Files.write(resultFile, arr);
		return true;
	}
	
	public static boolean decode(String sourceFile, String resultFile) {
		// decode a file
		
		System.out.println(Files.read(sourceFile, 3));
		
		//Files.write(resultFile);
		return true;
	}
}


class Tree {
	public static void build() {
		for(int i=0; i<Huffman.charArr.size(); i++) {
			Node n = new Node();
			n.ch = Huffman.charArr.get(i);
			n.fr = Huffman.freqArr.get(i);
			Huffman.nodeArr.add(n);
		}
		
		sort();
		
		read(Huffman.nodeArr.get(0), "");
	}
	
	private static void sort() {
		// sort a tree in ascending order based on frequencies
		while(Huffman.nodeArr.size() > 1) {
			Node x = Huffman.nodeArr.get(0);
			Node y = Huffman.nodeArr.get(1);
			
			Node z = new Node();
			z.left = x;
			z.right = y;
			z.fr = x.fr + y.fr;
			z.ch = -1;

			Huffman.nodeArr.remove(0);
			Huffman.nodeArr.remove(0);

			Huffman.nodeArr.add(z);
			
			// sort
			for(int i=Huffman.nodeArr.size()-1; i>0; i--) {
				if (Huffman.nodeArr.get(i).fr < Huffman.nodeArr.get(i-1).fr) {
					Node temp = Huffman.nodeArr.get(i);
					Huffman.nodeArr.set(i, Huffman.nodeArr.get(i-1));
					Huffman.nodeArr.set(i-1, temp);
				} else break;
			}
		}
		
		// output for testing purposes only
		//System.out.println();
		//for(Node n : Huffman.nodeArr)
			//System.out.println(n.ch + " " + n.fr);
	}
	
	private static void read(Node n, String code) {
		// assign binary codes to each character
		// output character and corresponding code array

		if(n == null)
			return;
		
		if(n.left == null && n.right == null) {
			Huffman.dictArr.put(n.ch, code);
			return;
		}
		
		read(n.left, code + "0");
		read(n.right, code + "1");
	}
}


class Node {
	public int ch;
	public int fr;
	public Node left;
	public Node right;
	
	public int getChar(){
		// nolasa virsotnes vērtību
		return this.ch;
	}
	public int getFreq(){
		// nolasa virsotnes biežumu
		return this.fr;
	}
	public Node getLeft(){
		// nolasa virsotnes kreiso bērnu
		return this.left;
	}
	public Node getRight(){
		// nolasa virsotnes labo bērnu
		return this.right;
	}
}


class Files {
	// InputStream
	// FileInputStream (bytes), DataInputStream (datatypes), ObjectInputStream (objects)
	
	public static String read(String filename, int option) {
		// reading data from a file
		String str = "";
		
		if(option == 1)
			str = Files.fillArrays(filename);
		if(option == 2)
			str = Files.compileString(filename);
		if(option == 3)
			str = Files.readSingleBytes(filename);
		
		return str;
	}
	
	// read a file char by char and record frequency of unique characters
	private static String fillArrays(String filename) {
		File f = new File(filename);
		if(f.exists()) {
			try {
				FileInputStream fis = new FileInputStream(f);
				int i;
				while (true) {
					i = fis.read();
					if(i == -1)
						break;
					
					// Huffman array operations
					if(Huffman.charArr.contains(i)) {
						int ind = Huffman.charArr.indexOf(i);
						int val = Huffman.freqArr.get(ind);
						Huffman.freqArr.set(ind, ++val);
					} else {
						Huffman.charArr.add(i);
						Huffman.freqArr.add(1);
					}
				}
				fis.close();
			}
			catch(Exception e) {
				// System.out.println(e.getMessage());
				return "";
			}
		} // else System.out.println("File does not exist!");
		return "";
	}
	
	// read a file char by char and translate into new codes
	private static String compileString(String filename) {
		String str = "";
		File f = new File(filename);
		if(f.exists()) {
			try {
				FileInputStream fis = new FileInputStream(f);
				int i;
				while (true) {
					i = fis.read();
					if(i == -1)
						break;
					
					// Huffman string operations
					str += Huffman.dictArr.get(i);

				}
				fis.close();
			}
			catch(Exception e) {
				// System.out.println(e.getMessage());
				return "";
			}
		} // else System.out.println("File does not exist!");
		return str;
	}
	
	// read a file char by char and output result plus generate original string
	private static String readSingleBytes(String filename) {
		String str = "";
		File f = new File(filename);
		if(f.exists()) {
			try {
				FileInputStream fis = new FileInputStream(f);
				int i;
				while (true) {
					i = fis.read();
					if(i == -1)
						break;
					
					// Huffman string operations
					String s = Integer.toBinaryString(i);
					if(s.length() < 8) {
						for(int c=s.length(); c<8; c++)
							s = "0" + s;
					}
					System.out.println(s + " " + i);
					str += s;

				}
				fis.close();
			}
			catch(Exception e) {
				// System.out.println(e.getMessage());
				return "";
			}
		} // else System.out.println("File does not exist!");
		return str;
	}
	
	private static void readMultipleBytes(String filename) {
		try {
			FileInputStream fis = new FileInputStream(filename);
			int i, k;
			byte mas[] = new byte[20];
			do {
				k = fis.read(mas);
				for(i=0; i<k; i++)
					System.out.println(mas[i]);
			} while (k != 0);
			fis.close();
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
			return;
		}
	}
	
	private static void readSmallBytes(String filename) {
		try {
			FileInputStream fis = new FileInputStream(filename);
			int i;
			byte mas[] = new byte[fis.available()];
			fis.read(mas);
			fis.close();
			for(i=0; i<mas.length; i++)
				System.out.println(mas[i]);
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
			return;
		}
	}
	
	// OutputStream
	// FileOutputStream (bytes), DataOutputStream (datatypes), ObjectOutputStream (objects)
	
	public static boolean write(String filename, String[] data) {
		// writing data to a file
		try {
			Files.writeSingleData(filename, data);
		} catch(Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
		
		return true;
	}
	
	private static void writeSingleData(String filename, String[] data) throws IOException {
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(filename));
		System.out.println("Result:");
		int nr;
		for(int i=0; i<data.length; i++) {
			nr = Integer.parseInt(data[i].trim(), 2);
			System.out.println(data[i] + " " + nr);
			dos.writeByte(nr);
		}
		dos.close();
	}
	
	private static void writeSingleBytes(String filename, String text) throws IOException {
		FileOutputStream fos = new FileOutputStream(filename);
		// String text = "to be or not to be";
		byte buf[] = text.getBytes();
		//for(byte b : buf)
			//System.out.println(b);
		for(int i=0; i<buf.length; i++)
			fos.write(buf[i]);
		fos.close();
	}
	
	private static void writeMultipleBytes(String filename) throws IOException {
		FileOutputStream fos = new FileOutputStream(filename);
		String text = "to be or not to be";
		byte buf[] = text.getBytes();
		fos.write(buf);
		fos.close();
	}
}


public class Main {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		String choiseStr;
		String sourceFile, resultFile, firstFile, secondFile;
		
		loop: while (true) {
			
			choiseStr = sc.next();
								
			switch (choiseStr) {
			case "comp":
				System.out.print("source file name: ");
				sourceFile = sc.next();
				System.out.print("archive name: ");
				resultFile = sc.next();
				comp(sourceFile, resultFile);
				break;
			case "decomp":
				System.out.print("archive name: ");
				sourceFile = sc.next();
				System.out.print("file name: ");
				resultFile = sc.next();
				decomp(sourceFile, resultFile);
				break;
			case "size":
				System.out.print("file name: ");
				sourceFile = sc.next();
				size(sourceFile);
				break;
			case "equal":
				System.out.print("first file name: ");
				firstFile = sc.next();
				System.out.print("second file name: ");
				secondFile = sc.next();
				System.out.println(equal(firstFile, secondFile));
				break;
			case "about":
				about();
				break;
			case "exit":
				break loop;
			}
		}

		sc.close();
	}

	public static void comp(String sourceFile, String resultFile) {
		// TODO: implement this method
		Huffman.encode(sourceFile, resultFile);
	}

	public static void decomp(String sourceFile, String resultFile) {
		// TODO: implement this method
		Huffman.decode(sourceFile, resultFile);
	}
	
	public static void size(String sourceFile) {
		try {
			FileInputStream f = new FileInputStream(sourceFile);
			System.out.println("size: " + f.available());
			f.close();
		}
		catch (IOException ex) {
			System.out.println(ex.getMessage());
		}
		
	}
	
	public static boolean equal(String firstFile, String secondFile) {
		try {
			FileInputStream f1 = new FileInputStream(firstFile);
			FileInputStream f2 = new FileInputStream(secondFile);
			int k1, k2;
			byte[] buf1 = new byte[1000];
			byte[] buf2 = new byte[1000];
			do {
				k1 = f1.read(buf1);
				k2 = f2.read(buf2);
				if (k1 != k2) {
					f1.close();
					f2.close();
					return false;
				}
				for (int i=0; i<k1; i++) {
					if (buf1[i] != buf2[i]) {
						f1.close();
						f2.close();
						return false;
					}
						
				}
			} while (k1 == 0 && k2 == 0);
			f1.close();
			f2.close();
			return true;
		}
		catch (IOException ex) {
			System.out.println(ex.getMessage());
			return false;
		}
	}
	
	public static void about() {
		// TODO insert information about authors
		System.out.println("211RDB330 Kaarlis Jurgens");
		System.out.println("221RDB134 Artis Bergs");
	}
}
