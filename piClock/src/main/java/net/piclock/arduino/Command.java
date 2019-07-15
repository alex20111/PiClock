package net.piclock.arduino;

public enum Command{
		LDR("l"), BTN("a"), TIME("t"),TIME_OFF("o"), BUZZER("b"),TIME_BRIGHTNESS("c"), MOSFET("m"), NONE(""), READY("ready");
		
		private String cmd;
		private Command(String cmd){
			this.cmd = cmd;
		}
		public String getCmd(){
			return cmd;
		}
		public static Command value(String cmd){
			for (Command c : Command.values()){
				if (c.getCmd().equalsIgnoreCase(cmd)){
					return c;
				}
			}
			
			return Command.NONE;
		}
	}
