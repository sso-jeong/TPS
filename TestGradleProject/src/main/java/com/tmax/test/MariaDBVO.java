package com.tmax.test;

import java.util.Date;

public class MariaDBVO {
	int EMPNO;
	int cnt;
	String ENAME;
	Date UPDATETIME;

	public int getEMPNO() {
		return EMPNO;
	}

	public void setEMPNO(int eMPNO) {
		EMPNO = eMPNO;
	}

	public int getCnt() {
		return cnt;
	}

	public void setCnt(int cnt) {
		this.cnt = cnt;
	}

	public String getENAME() {
		return ENAME;
	}

	public void setENAME(String eNAME) {
		ENAME = eNAME;
	}

	public Date getUPDATETIME() {
		return UPDATETIME;
	}

	public void setUPDATETIME(Date uPDATETIME) {
		UPDATETIME = uPDATETIME;
	}

}
