package trxsys.bootstrap;

import org.deuce.distribution.replication.full.Bootstrap;

public class BootstrapAnnotation {
	@Bootstrap(id=1)
	static List list;
	
	public static void main(String[] args) {
		list = new List();
	}
}
