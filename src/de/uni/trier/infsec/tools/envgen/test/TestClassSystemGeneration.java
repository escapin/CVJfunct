package de.uni.trier.infsec.tools.envgen.test;


public class TestClassSystemGeneration {
	
	public int anint02;
	public int anint06;
	public byte[] abyte02;
	public byte[] abyte06;
	
	public static TestClass02 getTestClass02() {
		return null;
	}
	
	public static TestClass03 getTestClass03(int a, int b, byte[] c, int d) {
		return null;
	}
	
	public static class TestClass02 {
		public int anint02;
		public int anint06;
		public byte[] abyte02;
		public byte[] abyte06;
	}
	
	public static class TestClass03 extends TestClass02 {
		public int anint02;
		public int anint07;
		public byte[] abyte02;
		public byte[] abyte07;
		
		public static TestClass02 getTestClass02(int a, int b, byte[] c, int d) {
			return null;
		}
		
		public static TestClass03 getTestClass03() {
			return null;
		}
	}
	
}
