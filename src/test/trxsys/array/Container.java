package trxsys.array;

import org.junit.internal.runners.TestMethod;


public class Container {
	int[] arr;
	String[][] matrx;
	
	void test() {
		int[][][] a = new int[3][][];
		a[0] = new int[2][];
	}
	
	void testArrParam(int[] arr) {}
	
	void testArrParam(String[] arr) {}
	
	int[] testMatrxParamArrRet(int[][] matrix) {
		return new int[] { 1 };
	}
	
	String[][] testMatrxRet() {
		String[][] m = new String[2][1];
		m[0] = new String[1];
		m[0][0] = "Hello";
		m[1] = new String[1];
		m[1][0] = "World";
		return m;
	}
	
	void testArrLoad() {
		int i = arr[0];
	}
	
	void testMatrxLoad() {
		String[] a = matrx[0];
		String s = matrx[0][0];
	}
	
	void testArrStore() {
		arr[0] = 1;
	}
	
	void testMatrxStore(String[] a) {
		matrx[0] = a;
		matrx[0][1] = "";
	}
	
	public static void main(String[] args) {
		new Container().testMatrxRet();
	}
}
