package introdb.heap;

public class ToHex {
	
	final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
	final static char BUNDLE_SEP = ' ';

	public static String bytesToHexString(byte[] bytes, int bundleSize) {
	        char[] hexChars = new char[(bytes.length * 2) + (bytes.length / bundleSize)];
	        for (int j = 0, k = 1; j < bytes.length; j++, k++) {
	                int v = bytes[j] & 0xFF;
	                int start = (j * 2) + j/bundleSize;

	                hexChars[start] = HEX_ARRAY[v >>> 4];
	                hexChars[start + 1] = HEX_ARRAY[v & 0x0F];

	                if ((k % bundleSize) == 0) {
	                        hexChars[start + 2] = BUNDLE_SEP;
	                }   
	        }   
	        return new String(hexChars).trim();    
	}
}
