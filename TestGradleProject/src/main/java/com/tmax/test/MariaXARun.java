package com.tmax.test;

import java.net.Inet4Address;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.mariadb.jdbc.MariaDbDataSource;
import org.mariadb.jdbc.MariaDbXid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class MariaXARun {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	// Returns a globally unique transaction id.
    static byte[] localIP = null;
    static int txnUniqueID = 0;

	// DB Connection Info1
	final static String DB_FROM_ADDR = "192.168.103.204";
	final static int DB_FROM_PORT = 30244;
	final static String DB_FROM_SID = "mariadb";
	final static String DB_FROM_USER = "root";
	final static String DB_FROM_PASSWORD = "root";

	// DB Connection Info2
	final static String DB_TO_ADDR = "192.168.103.13";
	final static int DB_TO_PORT = 31197;
	final static String DB_TO_SID = "mariadb";
	final static String DB_TO_USER = "root";
	final static String DB_TO_PASSWORD = "root";

	// main
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String main() {
		MariaXARun tbxa = new MariaXARun();

		try {
			long start = System.currentTimeMillis(); // 시작하는 시점 계산
			logger.info("\n\n====================== XARunning Start ======================\n\n");

			tbxa.XARunning();

			long end = System.currentTimeMillis(); // 프로그램이 끝나는 시점 계산
			logger.info("총 실행 시간 : " + (end - start) / 1000.0 + "초"); // 실행 시간 계산 및 출력
			logger.info("\n\n====================== XARunning END ======================");

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "hello";

	}

	void XARunning() throws XAException, SQLException {

		// step 1. open connection

		// step 1-1. create XA Data source (ResourceFactory)

		XADataSource xds1 = getXADataSource_sj(DB_FROM_ADDR, DB_FROM_PORT, DB_FROM_SID, DB_FROM_USER, DB_FROM_PASSWORD);
		XADataSource xds2 = getXADataSource_ce(DB_TO_ADDR, DB_TO_PORT, DB_TO_SID, DB_TO_USER, DB_TO_PASSWORD);
		//logger.info("create XA Data source success");

		// step 1-2. make XA connection (Transactiona Resource)
		XAConnection xaconn1 = xds1.getXAConnection();
		XAConnection xaconn2 = xds2.getXAConnection();
		//logger.info("make XA connection success");

		// step 1-3. make connection (DB Connecction)
		Connection conn1 = xaconn1.getConnection();
		Connection conn2 = xaconn2.getConnection();
		//logger.info("DB Connecction success");

		// step 2. get XA Resource (XAResource)
		XAResource xar1 = xaconn1.getXAResource();
		XAResource xar2 = xaconn2.getXAResource();
		//logger.info("XAResource success");

		// step 3. generate XID (Transaction ID)
		// 같은 Global Transaction ID를 가지고, 다른 Branch Transaction ID 를 갖는 Transaction ID 를
		// 생성한다.
		Xid xid1 = createXid(1);
		Xid xid2 = createXid(2);

		// step 4. xa start (send XA_START message to XAResource)
		xar1.start(xid1, XAResource.TMNOFLAGS);
		xar2.start(xid2, XAResource.TMNOFLAGS);

		String sql;

		try {
				long start4 = System.currentTimeMillis(); // 시작하는 시점 계산
				logger.info("====================== Query START ======================\n");

				logger.info("====================== INSERT START ======================");
				Statement stmt1 = conn1.createStatement();

				sql = "INSERT INTO EMP  (\n" + "    EMPNO,\n" + "    ENAME,\n" + "    HIREDATE\n" + ") VALUES (\n"
						+ "    1000, \n" + "\t'TRANSACTION_TEST',\n" + "\tCURRENT_TIMESTAMP    \n" + ")";

				stmt1.executeUpdate(sql);
				logger.info("====================== INSER END ======================\n\n");

				logger.info("====================== UPDATE START ======================");
				Statement stmt2 = conn2.createStatement();
				sql = "UPDATE EMP SET ENAME = 'TRANSACTION_UPDATE', HIREDATE = CURRENT_TIMESTAMP WHERE EMPNO = 1000";
				stmt2.executeUpdate(sql);

				logger.info("====================== UPDATE END ======================\n\n");

				long end4 = System.currentTimeMillis(); // 프로그램이 끝나는 시점 계산
				logger.info("All Query 실행 시간 >> " + (end4 - start4) / 1000.0 + "초");
				logger.info("======================  Query END ======================\n\n");
				
		

		} catch (Exception e) {
			e.printStackTrace();
		}

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
				&& (prep2 == XAResource.XA_OK || prep2 == XAResource.XA_RDONLY)) {
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
			logger.info("sj rollback !! ");

			if (prep2 != XAResource.XA_RDONLY)
				xar2.rollback(xid2);
			logger.info("ce rollback !! ");

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
			    
	    Random rnd = new Random(System.currentTimeMillis());
        txnUniqueID++;
        int txnUID = txnUniqueID;
        int tidID = bids;
        int randID = rnd.nextInt();
        byte[] gtrid = new byte[64];
        byte[] bqual = new byte[64];
        if (null == localIP) {
            try {
                localIP = Inet4Address.getLocalHost().getAddress();
            } catch (Exception ex) {
                localIP = new byte[] {0x01, 0x02, 0x03, 0x04};
            }
        }
        System.arraycopy(localIP, 0, gtrid, 0, 4);
        System.arraycopy(localIP, 0, bqual, 0, 4);

        // Bytes 4 -> 7 - unique transaction id.
        // Bytes 8 ->11 - thread id.
        // Bytes 12->15 - random number generated by using seed from current time in milliseconds.
        for (int i = 0; i <= 3; i++) {
            gtrid[i + 4] = (byte) (txnUID % 0x100);
            bqual[i + 4] = (byte) (txnUID % 0x100);
            txnUID >>= 8;
            gtrid[i + 8] = (byte) (tidID % 0x100);
            bqual[i + 8] = (byte) (tidID % 0x100);
            tidID >>= 8;
            gtrid[i + 12] = (byte) (randID % 0x100);
            bqual[i + 12] = (byte) (randID % 0x100);
            randID >>= 8;
        }
        
        Xid xid = new MariaDbXid(0x1234, gtrid, bqual);
        
        logger.info("====================== XID ======================");
		logger.info("xid >>>> " + xid);
		
        return xid;
		
	}
	  
		

	XADataSource getXADataSource_sj(String dbAddr, int port, String sid, String userId, String password)
			throws SQLException, XAException {
		MariaDbDataSource mads_sj = new MariaDbDataSource();

		String url = "jdbc:mariadb://192.168.103.204:30244/test";

		mads_sj.setUrl(url);
		mads_sj.setUser(userId);
		mads_sj.setPassword(password);
		logger.info("SJ DB 연결 성공");

		return (XADataSource) mads_sj;

	}// getXADataSource

	XADataSource getXADataSource_ce(String dbAddr, int port, String sid, String userId, String password)
			throws SQLException, XAException {
		MariaDbDataSource mads_ce = new MariaDbDataSource();

		String url = "jdbc:mariadb://192.168.103.204:31197/test";

		mads_ce.setUrl(url);
		mads_ce.setUser(userId);
		mads_ce.setPassword(password);
		logger.info("CE DB 연결 성공");

		return (XADataSource) mads_ce;

	}// getXADataSource

}
