package network;

import java.io.Serializable;

public class ProtocolHelper {
	private static class Ping implements Serializable {
		private static final long serialVersionUID = 2544201995473409008L;
		
		@Override
		public boolean equals(Object o) {
			return o.getClass().equals(this.getClass());
		}
	}
	
	private static class MoveRequest implements Serializable {
		private static final long serialVersionUID = 454099640188938399L;
		
		@Override
		public boolean equals(Object o) {
			return o.getClass().equals(this.getClass());
		}
	}
	
	public final static Ping PING = new Ping();
	public final static MoveRequest MOVE_REQUEST = new MoveRequest();
}
