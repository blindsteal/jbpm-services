package de.livinglab;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class MappingEntry {

	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	private String remoteHost;
	private int localId1, localId2, localId3;
	private String localDef1, localDef2, localDef3;
	private int remoteId1, remoteId2, remoteId3;
	private String remoteDef1, remoteDef2, remoteDef3;

	protected MappingEntry(){}

	public MappingEntry( String remoteHost, 
			String localDef1, int localId1, String localDef2, int localId2, String localDef3, int localId3,
			String remoteDef1, int remoteId1, String remoteDef2, int remoteId2, String remoteDef3, int remoteId3) {
		super();
		this.remoteHost = remoteHost;
		this.localId1 = localId1;
		this.localId2 = localId2;
		this.localId3 = localId3;
		this.localDef1 = localDef1;
		this.localDef2 = localDef2;
		this.localDef3 = localDef3;
		this.remoteId1 = remoteId1;
		this.remoteId2 = remoteId2;
		this.remoteId3 = remoteId3;
		this.remoteDef1 = remoteDef1;
		this.remoteDef2 = remoteDef2;
		this.remoteDef3 = remoteDef3;
	}

	public String getRemoteHost() {
		return remoteHost;
	}

	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}

	public int getLocalId1() {
		return localId1;
	}

	public void setLocalId1(int localId1) {
		this.localId1 = localId1;
	}

	public int getLocalId2() {
		return localId2;
	}

	public void setLocalId2(int localId2) {
		this.localId2 = localId2;
	}

	public int getLocalId3() {
		return localId3;
	}

	public void setLocalId3(int localId3) {
		this.localId3 = localId3;
	}

	public String getLocalDef1() {
		return localDef1;
	}

	public void setLocalDef1(String localDef1) {
		this.localDef1 = localDef1;
	}

	public String getLocalDef2() {
		return localDef2;
	}

	public void setLocalDef2(String localDef2) {
		this.localDef2 = localDef2;
	}

	public String getLocalDef3() {
		return localDef3;
	}

	public void setLocalDef3(String localDef3) {
		this.localDef3 = localDef3;
	}

	public int getRemoteId1() {
		return remoteId1;
	}

	public void setRemoteId1(int remoteId1) {
		this.remoteId1 = remoteId1;
	}

	public int getRemoteId2() {
		return remoteId2;
	}

	public void setRemoteId2(int remoteId2) {
		this.remoteId2 = remoteId2;
	}

	public int getRemoteId3() {
		return remoteId3;
	}

	public void setRemoteId3(int remoteId3) {
		this.remoteId3 = remoteId3;
	}

	public String getRemoteDef1() {
		return remoteDef1;
	}

	public void setRemoteDef1(String remoteDef1) {
		this.remoteDef1 = remoteDef1;
	}

	public String getRemoteDef2() {
		return remoteDef2;
	}

	public void setRemoteDef2(String remoteDef2) {
		this.remoteDef2 = remoteDef2;
	}

	public String getRemoteDef3() {
		return remoteDef3;
	}

	public void setRemoteDef3(String remoteDef3) {
		this.remoteDef3 = remoteDef3;
	}

	@Override
	public String toString() {
		return "MappingEntry [id=" + id + ", remoteHost=" + remoteHost
				+ ", localId1=" + localId1 + ", localId2=" + localId2
				+ ", localId3=" + localId3 + ", localDef1=" + localDef1
				+ ", localDef2=" + localDef2 + ", localDef3=" + localDef3
				+ ", remoteId1=" + remoteId1 + ", remoteId2=" + remoteId2
				+ ", remoteId3=" + remoteId3 + ", remoteDef1=" + remoteDef1
				+ ", remoteDef2=" + remoteDef2 + ", remoteDef3=" + remoteDef3
				+ "]";
	}

	
}
