package com.tmax.test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.tmax.tibero.jdbc.ext.TbXADataSource;
import com.tmax.tibero.jdbc.ext.TbXid;

//@Controller
public class TiberoXARun {

	// DB Connection Info1
	final static String DB_FROM_ADDR = "xxx.xxx.xxx.xxx";
	final static int DB_FROM_PORT = 8629;
	final static String DB_FROM_SID = "tibero";
	final static String DB_FROM_USER = "po7dev_sj";
	final static String DB_FROM_PASSWORD = "po7dev_sj";

	// DB Connection Info2
	final static String DB_TO_ADDR = "xxx.xxx.xxx.xxx;
	final static int DB_TO_PORT = 8629;
	final static String DB_TO_SID = "tibero";
	final static String DB_TO_USER = "po7devdb_ce";
	final static String DB_TO_PASSWORD = "po7devdb_ce";

	// main  
	//@GetMapping("/")
	//@RequestMapping(value = "/", method = RequestMethod.GET)
	public String main() {
		TiberoXARun tbxa = new TiberoXARun();

		try {
			tbxa.XARunning();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "hello";

	}

	void XARunning() throws XAException, SQLException {

		// step 1. open connection

		// step 1-1. create XA Data source (ResourceFactory)
		XADataSource xds1 = getXADataSource(DB_FROM_ADDR, DB_FROM_PORT, DB_FROM_SID, DB_FROM_USER, DB_FROM_PASSWORD);
		XADataSource xds2 = getXADataSource(DB_TO_ADDR, DB_TO_PORT, DB_TO_SID, DB_TO_USER, DB_TO_PASSWORD);

		// step 1-2. make XA connection (Transactiona Resource)
		XAConnection xaconn1 = xds1.getXAConnection();
		XAConnection xaconn2 = xds2.getXAConnection();

		// step 1-3. make connection (DB Connecction)
		Connection conn1 = xaconn1.getConnection();
		Connection conn2 = xaconn2.getConnection();

		// step 2. get XA Resource (XAResource)
		XAResource xar1 = xaconn1.getXAResource();
		XAResource xar2 = xaconn2.getXAResource();

		// step 3. generate XID (Transaction ID)

		// 같은 Global Transaction ID를 가지고, 다른 Branch Transaction ID 를 갖는 Transaction ID 를
		// 생성한다.

		Xid xid1 = createXid(1);
		Xid xid2 = createXid(2);

		// step 4. xa start (send XA_START message to XAResource)

		xar1.start(xid1, XAResource.TMNOFLAGS);
		xar2.start(xid2, XAResource.TMNOFLAGS);

		String sql;

		Statement stmt1 = conn1.createStatement();

		 sql = "INSERT INTO PO7DEV_SJ.EMP  (\n" +
	                "    EMPNO,\n" +
	                "    ENAME,\n" +
	                "    HIREDATE\n" +
	                ") VALUES (\n" +
	                "    (SELECT NVL(MAX(EMPNO)+1,0) FROM PO7DEV_SJ.EMP),\n" +
	                //"    1000, \n" +
	                "\t'TRANSACTION_TEST',\n" +
	                "\tCURRENT_TIMESTAMP    \n" +
	                ")";

		stmt1.executeUpdate(sql);

		sql = "UPDATE PO7DEVDB_CE.EMP SET ENAME = 'TRANSACTION_UPDATE', HIREDATE = CURRENT_TIMESTAMP WHERE EMPNO = 1000";
				//+ " (SELECT NVL(MIN(EMPNO),0) FROM PO7DEVDB_CE.EMP)";

		Statement stmt2 = conn2.createStatement();

		stmt2.executeUpdate(sql);

		// step 6. xa end (Transaction이 종료되었음을 알린다.)

		xar1.end(xid1, XAResource.TMSUCCESS);
		xar2.end(xid2, XAResource.TMSUCCESS);

		// step 7. xa prepare (xa prepare를 한다.)

		int prep1 = xar1.prepare(xid1);
		int prep2 = xar2.prepare(xid2);

		// step 8-1. check prepare stat 양쪽 다 prepare가 성공 하였으면 commit할 준비를 한다.
		// ※ XA_RDONLY는 update가 없이 select등의 Read Only 만 있는 Transaction 이 성공하였을때,

		boolean docommit = false;

		if ((prep1 == XAResource.XA_OK || prep1 == XAResource.XA_RDONLY)
				&& (prep2 == XAResource.XA_OK || prep2 == XAResource.XA_RDONLY))

		{
			docommit = true;
		}

		if (docommit) {

			// XA_RDONLY는 이미 commit이 되어 있기 때문에 따로 commit하지 않는다.

			if (prep1 == XAResource.XA_OK)
				xar1.commit(xid1, false);

			if (prep2 == XAResource.XA_OK)
				xar2.commit(xid2, false);

		} else {

			// rollback 하는 부분
			if (prep1 != XAResource.XA_RDONLY)
				xar1.rollback(xid1);

			if (prep2 != XAResource.XA_RDONLY)
				xar2.rollback(xid2);

		}

		// step 9. close connection
		conn1.close();
		conn2.close();

		xaconn1.close();
		xaconn2.close();

		conn1 = null;
		conn2 = null;

		xaconn1 = null;
		xaconn2 = null;

	}// XARun

	Xid createXid(int bids) throws XAException {

		byte[] gid = new byte[1];
		gid[0] = (byte) 9;

		byte[] bid = new byte[1];
		bid[0] = (byte) bids;

		byte[] gtrid = new byte[64];

		byte[] bqual = new byte[64];

		System.arraycopy(gid, 0, gtrid, 0, 1);

		System.arraycopy(bid, 0, bqual, 0, 1);

		Xid xid = new TbXid(0x1234, gtrid, bqual);

		return xid;

	}// createXid

	XADataSource getXADataSource(String dbAddr, int port, String sid, String userId, String password)
			throws SQLException, XAException

	{
		TbXADataSource tbds = new TbXADataSource();

		String url = "jdbc:tibero:thin:@xxx.xxx.xxx.xxx:8629:tibero";

		tbds.setURL(url);
		tbds.setUser(userId);
		tbds.setPassword(password);
		System.out.println("test DB 연결 성공");

		return (XADataSource) tbds;

	}// getXADataSource

}
