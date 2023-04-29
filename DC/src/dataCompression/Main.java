package dataCompression;

// 211RDB330 Kārlis Jurgens
// 221RDB134 Artis Bergs
// 221RDB076 Kamilla Saleniece

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Scanner;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.BufferedWriter;
import java.io.FileWriter;

class Huffman {
	
	public static List<Integer> myFile = new ArrayList<Integer>();
	
	public static List<Integer> charArr = new ArrayList<Integer>();
	public static List<Integer> freqArr = new ArrayList<Integer>();
	
	public static List<Node> nodeArr = new ArrayList<Node>();
	
	public static Map<Integer, String> dictArr = new TreeMap<Integer, String>();
	
	
	public static void reset() {
		charArr.clear();
		freqArr.clear();
		nodeArr.clear();
		dictArr.clear();
	}
	
	public static boolean encode(String sourceFile, String resultFile) {
		// encode a file
		
		// init
		reset();
		long startTime, endTime, duration;
		
		// read a file
		System.out.println(Files.read(sourceFile, 1));
		if(myFile.isEmpty())
			return false;
		
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
		
		// recreate data
		String codeStr = Files.read(sourceFile, 2);
		
		// output3 for testing purposes only
		System.out.println("Code string:");
		//System.out.println(codeStr);
		
		// divide binary string into 8-bit array
		String[] arr = codeStr.split("(?<=\\G.{8})");
		
		// convert bytes to integers and finish
		startTime = System.nanoTime();
		boolean res = Files.write(resultFile, arr, nodeArr);
		endTime = System.nanoTime();
		duration = (endTime - startTime);
		System.out.println(duration/1000000);
		
		return res;
	}
	
	public static boolean decode(String sourceFile, String resultFile) {
		// decode a file
		StringBuilder sb = new StringBuilder();
		
		String bin = Files.read(sourceFile, 3);
		//System.out.println(bin);
		Node curr = nodeArr.get(0);
		
		int temp = 0;
		byte[] res = new byte[myFile.size()];
		
        int n = bin.length();
        for (int i = 0; i < n; i++) {
            if (bin.charAt(i) == '0') {
                curr = curr.left;
            } else {
                curr = curr.right;
            }
            if (curr.left == null && curr.right == null) {
            	//System.out.print(curr.ch + " " + (byte)curr.ch + " ");
                //sb.append((char)curr.ch);
                res[temp]=(byte)curr.ch;
                temp++;
                curr = nodeArr.get(0);
            }
        }
        
        String ans = sb.toString();
        System.out.println(ans + " " + temp);
        
		return Files.write3(resultFile, res);
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
		if(n == null)
			return;
		
		if(n.left == null && n.right == null) {
			Huffman.dictArr.put(n.ch, code);
			return;
		}
		
		codeGen(n.left, code + "0");
		codeGen(n.right, code + "1");
	}
}


class Files {
	// InputStream
	// FileInputStream (bytes), DataInputStream (datatypes), ObjectInputStream (objects)
	
	public static String read(String filename, int option) {
		// reading data from a file
		String str = "";
		
		if(option == 1)
			str = Files.file2comp(filename);
		if(option == 2)
			str = Files.compileString(filename);
		if(option == 3)
			str = Files.readSingleBytes(filename);
		
		return str;
	}
	
	// read a file char by char and record frequency of unique characters
	private static String file2comp(String filename) {
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
				return("Success!");
			}
			catch(Exception e) {
				return(e.getMessage());
			}
		} else return("File does not exist!");
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
					
					//Huffman.total++;
					//System.out.print(i + " " + (char)i + " ");
					//System.out.println();
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
		StringBuilder sb = new StringBuilder();
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
					//str += Huffman.dictArr.get(i);
					sb.append(Huffman.dictArr.get(i));

				}
				fis.close();
				str=sb.toString();
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
		StringBuilder sb = new StringBuilder();
		String str = "";
		int lastByte = 8;
		File f = new File(filename);
		if(f.exists()) {
			try {
				FileInputStream fis = new FileInputStream(f);
				ObjectInputStream ois = new ObjectInputStream(fis);
				
				Huffman.nodeArr = (List<Node>)ois.readObject();
				//for(Node n : ln)
					//System.out.println(n.ch);
				
				//Byte[] nl = (Byte[])ois.readObject();
				//for(Byte l : nl)
					//System.out.println(l);
				
				int i;
				while (true) {
					i = ois.read();
					if(i == -1)
						break;
					
					// Huffman string operations
					lastByte = i;
					String s = Integer.toBinaryString(i);
					
					// Huffman eachByte correction
					if(s.length() < 8) {
						for(int c=s.length(); c<8; c++)
							s = "0" + s;
					}else if (s.length() > 8) {
						s = s.substring(s.length()-8, s.length());
					}
					
					//System.out.println(s + " " + i);
					//str += s;
					sb.append(s);
				}
				
				ois.close();
				fis.close();
				str = sb.toString();
				
				// Huffman lastByte correction
				if(lastByte < 8) {
					String tmp = str.substring(str.length()-8-lastByte, str.length()-8);
					str = str.substring(0, str.length()-16) + tmp;
				} else str = str.substring(0, str.length()-8);
				
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
	
	public static boolean write(String filename, String[] data, List<Node> dict) {
		// writing to a dat file
		try {
			Files.writeSingleData(filename, data, dict);
		} catch(Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
		
		return true;
	}
	
	public static boolean write2(String filename, String data) {
		// writing to a string file
		try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writer.write(data);
            writer.close();
            //System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            //System.out.println("An error occurred.");
            e.printStackTrace();
            return false;
        }
		
		return true;
	}
	
	public static boolean write3(String filename, byte[] data) {
		// writing to a string file
		//for(byte d : data)
			//System.out.println(d);
		try {
            FileOutputStream writer = new FileOutputStream(filename);
            writer.write(data);
            writer.close();
            //System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            //System.out.println("An error occurred.");
            e.printStackTrace();
            return false;
        }
		
		return true;
	}
	
	private static void writeSingleData(String filename, String[] data, List<Node> dict) throws IOException {
		System.out.println("Result:");
//		Byte[] toFile = new Byte[data.length+1];
//		int nr;
//		String dt = "";
//		for(int i=0; i<data.length; i++) {
//			dt = data[i].trim();
//			nr = Integer.parseInt(dt, 2);
//			System.out.println(dt + " " + nr);
//			toFile[i] = (byte)nr;
//		}
//		toFile[data.length] = (byte)dt.length();
		
		
		// serialize?
		FileOutputStream fos = new FileOutputStream(filename);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(dict);
		
		//oos.writeObject(toFile);
		//for(Byte b : toFile)
			//oos.writeByte(b);
		int nr;
		String dt = "";
		for(int i=0; i<data.length; i++) {
			dt = data[i].trim();
			nr = Integer.parseInt(dt, 2);
			//System.out.println(dt + " " + nr);
			oos.writeByte(nr);
		}
		oos.writeByte(dt.length());
		
		oos.close();
		fos.close();
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
