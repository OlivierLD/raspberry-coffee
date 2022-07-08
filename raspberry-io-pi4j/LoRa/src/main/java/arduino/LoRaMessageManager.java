package arduino;

public class LoRaMessageManager {

	public enum MessageType {
		INFO,
		ERROR,
		DATA,
		ACK
	};

	public enum Messages {
		LORA_0001("LORA-0001", MessageType.INFO,  "Started"),
		LORA_0002("LORA-0002", MessageType.ERROR, "LoRa radio init failed"),
		LORA_0003("LORA-0003", MessageType.INFO,  "LoRa radio init OK"),
		LORA_0004("LORA-0004", MessageType.ERROR, "setFrequency failed"),
		LORA_0005("LORA-0005", MessageType.INFO,  "Set Freq to: ..."),
		LORA_0006("LORA-0006", MessageType.INFO,  "Now waiting for messages (send or receive)"),
		LORA_0007("LORA-0007", MessageType.DATA,  "Data Received"),
		LORA_0008("LORA-0008", MessageType.ACK,   "TX_ACK [after sending]"),
		LORA_0009("LORA-0009", MessageType.ERROR, "Receive failed (From Rx)"),
		LORA_0010("LORA-0010", MessageType.INFO,  "Transmitting..."),
		LORA_0011("LORA-0011", MessageType.INFO,  "Sending data [...]"),
		LORA_0012("LORA-0012", MessageType.INFO,  "Waiting for packet (send) to complete..."),
		LORA_0013("LORA-0013", MessageType.INFO,  "Waiting for reply..."),
		LORA_0014("LORA-0014", MessageType.INFO,  "Got reply: ... (From Tx)"), // TODO Make sure this exists (and is required)
		LORA_0015("LORA-0015", MessageType.ERROR, "Receive failed (From Tx)"),
		LORA_0016("LORA-0016", MessageType.ERROR, "No reply..., is there a listener around? (From Tx)");

		private String id;
		private MessageType messType;
		private String description;

		Messages(String id, MessageType messType, String description) {
			this.id = id;
			this.messType = messType;
			this.description = description;
		}

		public String id() { return this.id; }
		public MessageType messType() { return this.messType; }
		public String description() { return this.description; }
	}

	public static void throwIfError(String message) {
		for (Messages mess : Messages.values()) {
			if (message.startsWith(mess.id())) {
				if (mess.messType == MessageType.ERROR) {
					throw new RuntimeException(message);
				}
				break;
			}
		}
	}
}
