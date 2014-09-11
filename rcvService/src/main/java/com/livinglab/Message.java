package com.livinglab;

public class Message {

	private String def1, def2, def3;
	private int id1, id2, id3;
	private String v2;
	private String msg;
	private String source2;
	
	private int targetId2;

	public Message(){
		super();
		this.def1 = "";
		this.def2 = "";
		this.def3 = "";
		this.id1 = -1;
		this.id2 = -1;
		this.id3 = -1;
		this.v2 = "";
		this.msg = "";
		this.targetId2 = -1;
		this.source2 = "";
	}
	
	public Message(String def1, String def2, String def3, int id1, int id2,
			int id3, String v2, String msg, int targetId2, String source2) {
		super();
		this.def1 = def1;
		this.def2 = def2;
		this.def3 = def3;
		this.id1 = id1;
		this.id2 = id2;
		this.id3 = id3;
		this.v2 = v2;
		this.msg = msg;
		this.targetId2 = targetId2;
		this.source2 = source2;
		
	}

	public String getDef1() {
		return def1;
	}

	public void setDef1(String def1) {
		this.def1 = def1;
	}

	public String getDef2() {
		return def2;
	}

	public void setDef2(String def2) {
		this.def2 = def2;
	}

	public String getDef3() {
		return def3;
	}

	public void setDef3(String def3) {
		this.def3 = def3;
	}

	public int getId1() {
		return id1;
	}

	public void setId1(int id1) {
		this.id1 = id1;
	}

	public int getId2() {
		return id2;
	}

	public void setId2(int id2) {
		this.id2 = id2;
	}

	public int getId3() {
		return id3;
	}

	public void setId3(int id3) {
		this.id3 = id3;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public int getTargetId2() {
		return targetId2;
	}

	public void setTargetId2(int targetId2) {
		this.targetId2 = targetId2;
	}

	public String getV2() {
		return v2;
	}

	public void setV2(String v2) {
		this.v2 = v2;
	}

	public String getSource2() {
		return source2;
	}

	public void setSource2(String source2) {
		this.source2 = source2;
	}

	@Override
	public String toString() {
		return "Message [def1=" + def1 + ", def2=" + def2 + ", def3=" + def3
				+ ", id1=" + id1 + ", id2=" + id2 + ", id3=" + id3 + ", v2="
				+ v2 + ", msg=" + msg + ", targetId2=" + targetId2 + "]";
	}
	
}
