package main.java.tukano.impl.data;

public class FeedResponse {
		public String shortId;
		public String timestamp;

		public String getShortId() {
			return shortId;
		}

		public void setShortId(String shortId) {
			this.shortId = shortId;
		}

		public String getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(String timestamp) {
			this.timestamp = timestamp;
		}

		@Override
		public String toString() {
			return "{ shortId: " + shortId + ", timestamp: " + timestamp + "}";
		}
	}