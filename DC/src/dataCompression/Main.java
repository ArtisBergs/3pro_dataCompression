package dataCompression;

// 211RDB330 Kārlis Jurgens
// 221RDB134 Artis Bergs
// 221RDB076 Kamilla Saleniece

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Scanner;
import java.io.Serializable;


class Huffman {
	public static int total = 0; // original filebytes
	public static List<Integer> myFile = new ArrayList<Integer>();
	
	public static List<Integer> charArr = new ArrayList<Integer>();
	public static List<Integer> freqArr = new ArrayList<Integer>();
	
	public static List<Node> nodeArr = new ArrayList<Node>();
	
	public static Map<Integer, String> dictArr = new TreeMap<Integer, String>();
	
	public static void reset() {
		total=0;
		myFile.clear();
		charArr.clear();
		freqArr.clear();
		nodeArr.clear();
		dictArr.clear();
	}
	
	public static boolean encode(String sourceFile, String resultFile) {
		// encode a file
		
		// init
		reset();
		
		// read a file
		System.out.println(Files.read(sourceFile));
		if(myFile.isEmpty()) {
			System.out.print("Outcome: ");
			return false;
		}
		
		// Huffman frequency calculations
		int ind, val;
		for(int in : myFile) {
			if(charArr.contains(in)) {
				ind = charArr.indexOf(in);
				val = freqArr.get(ind);
				freqArr.set(ind, ++val);
			} else {
				charArr.add(in);
				freqArr.add(1);
			}
		}
		
		// sort arrays
		boolean flag = false;
		while(flag == false) {
			flag = true;
			for(int i=0; i<freqArr.size()-1; i++) {
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
		
		// digging the Huffman tree
		Node.arrInit();
		Node.buildTree();
		Node.codeGen(Huffman.nodeArr.get(0), "");
		
		// generate binary data string
		StringBuilder sb = new StringBuilder();
		for(int in : myFile)
			sb.append(Huffman.dictArr.get(in));
		String codeStr = sb.toString();
		
		// divide binary string into 8-bit array
		String[] arr = codeStr.split("(?<=\\G.{8})");
		
		// convert text bytes to integers and finish to file
		System.out.print("Outcome: ");
		return Files.write(resultFile, myFile.size(), nodeArr, arr);
	}
	
	public static boolean decode(String sourceFile, String resultFile) {
		// decode a file
		reset();
		
		// read from a dat file
		// 1. binary string
		// 2. and Node array
		// 3. plus original filesize
		System.out.println(Files.read2(sourceFile));
		if(nodeArr.isEmpty()) {
			System.out.print("Outcome: ");
			return false;
		}
		
		// string manipulation
		String str = ""; // new string
		StringBuilder sb = new StringBuilder();
		
		int lastByte = 8; // length of a byte in bits
		String s = ""; // tmp
		for(int in : myFile) {
			lastByte = in; // switch until the end
			s = Integer.toBinaryString(in);
			
			// eachByte correction
			if(s.length() < 8) {
				for(int c=s.length(); c<8; c++)
					s = "0" + s;
			}else if(s.length() > 8) {
				s = s.substring(s.length()-8, s.length());
			}
			
			sb.append(s);
		}
		
		str = sb.toString();
		
		// and lastByte correction
		if(lastByte < 8) {
			String tmp = str.substring(str.length()-8-lastByte, str.length()-8);
			str = str.substring(0, str.length()-16) + tmp;
		} else str = str.substring(0, str.length()-8);
		
		// finally generate original data by traversing the tree
        byte[] res = Node.byteGen(Huffman.nodeArr.get(0), str);
        
        // write original content in the form of bytes to avoid encoding issues
        System.out.print("Outcome: ");
		return Files.write2(resultFile, res);
	}
}


class Node implements Serializable {
	public int ch;
	public int fr;
	public Node left;
	public Node right;
	
	// initialize Huffman node array
	public static void arrInit() {
		for(int i=0; i<Huffman.charArr.size(); i++) {
			Node n = new Node();
			n.ch = Huffman.charArr.get(i);
			n.fr = Huffman.freqArr.get(i);
			Huffman.nodeArr.add(n);
		}
	}
	
	// build a Huffman tree
	public static void buildTree() {
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
			
			// sort OR move newly added element into place to maintain asc order
			for(int i=Huffman.nodeArr.size()-1; i>0; i--) {
				if (Huffman.nodeArr.get(i).fr < Huffman.nodeArr.get(i-1).fr) {
					Node temp = Huffman.nodeArr.get(i);
					Huffman.nodeArr.set(i, Huffman.nodeArr.get(i-1));
					Huffman.nodeArr.set(i-1, temp);
				} else break;
			}
		}
	}
	
	// assign binary codes to each character and put into dictionary
	public static void codeGen(Node n, String code) {
		// encoding
		if(n == null)
			return;
		
		if(n.left == null && n.right == null) {
			Huffman.dictArr.put(n.ch, code);
			return;
		}
		
		codeGen(n.left, code + "0");
		codeGen(n.right, code + "1");
		
		// return dic;
	}
	
	// translate the code into original bytes
	public static byte[] byteGen(Node n, String code) {
		// decoding
		Node curr = n; // Huffman.nodeArr.get(0)
		
		byte[] output = new byte[Huffman.total];

		int temp = 0;
        for (int i=0; i<code.length(); i++) {
        	// steer the tree
            if (code.charAt(i) == '0') {
                curr = curr.left;
            } else {
                curr = curr.right;
            }
            // discover a leaf and reset
            if (curr.left == null && curr.right == null) {
                output[temp++] = (byte)curr.ch;
                curr = n;
            }
        }
        
        return output;
	}
}


class Files {
	// InputStream
	// FileInputStream (bytes), DataInputStream (datatypes), ObjectInputStream (objects)

	// read a file char by char and record frequency of unique characters
	public static String read(String filename) {
		File f = new File(filename);
		if(f.exists()) {
			try {
				FileInputStream fis = new FileInputStream(f);
				int i;
				while (true) {
					i = fis.read();
					if(i == -1)
						break;
					// add bytes to array
					Huffman.myFile.add(i);
				}
				fis.close();
				return("Read success!");
			}
			catch(Exception e) {
				return(e.getMessage());
			}
		} else return("File does not exist!");
	}
	
	// read a file char by char and output result plus generate original string
	public static String read2(String filename) {
		File f = new File(filename);
		if(f.exists()) {
			try {
				FileInputStream fis = new FileInputStream(f);
				ObjectInputStream ois = new ObjectInputStream(fis);
				
				Huffman.total = ois.readInt(); //part I
				Huffman.nodeArr = (List<Node>)ois.readObject(); //part II
				
				int i; //part III
				while (true) {
					i = ois.read();
					if(i == -1)
						break;
					// add bytes to array
					Huffman.myFile.add(i);
				}
				
				ois.close();
				fis.close();
				return("Read success!");
			}
			catch(Exception e) {
				return(e.getMessage());
			}
		} else return("File does not exist!");
	}
	
	
	// OutputStream
	// FileOutputStream (bytes), DataOutputStream (datatypes), ObjectOutputStream (objects)
	
	// writing to a dat file
	public static boolean write(String filename, int size, List<Node> dict, String[] data) {
		try {
			FileOutputStream fos = new FileOutputStream(filename);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			
			oos.writeInt(size); // original filesize
			oos.writeObject(dict); // dictionary

			// databytes (conversion)
			int nr;
			String dt = "";
			for(int i=0; i<data.length; i++) {
				dt = data[i]; // no need for trim
				nr = Integer.parseInt(dt, 2);
				oos.writeByte(nr);
			}
			// last num represents significant digits of the last byte
			oos.writeByte(dt.length());
			
			oos.close();
			fos.close();
		} catch(Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
		return true;
	}
	
	// re-creating original
	public static boolean write2(String filename, byte[] data) {
		// simply writing byte array to a file
		try {
            FileOutputStream writer = new FileOutputStream(filename);
            writer.write(data);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
		return true;
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
		System.out.println(Huffman.encode(sourceFile, resultFile));
	}

	public static void decomp(String sourceFile, String resultFile) {
		// TODO: implement this method
		System.out.println(Huffman.decode(sourceFile, resultFile));
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
		System.out.println("221RDB076 Kamilla Saleniece");
	}
}
