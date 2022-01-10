package com.tmax.log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class CustomLogFormatter extends Formatter {
	
	public String getHead(Handler h) {
		return "START LOG\n";
	}

	public String format(LogRecord rec) {
		StringBuffer buf = new StringBuffer(1000);
		buf.append(calcDate(rec.getMillis()));

		buf.append(" [");
		buf.append(rec.getLevel());
		buf.append("] ");

		buf.append("[");
		buf.append(rec.getSourceMethodName());
		buf.append("] ");

		buf.append(rec.getMessage());
		buf.append("\n");

		return buf.toString();
	}

	public String getTail(Handler h) {
		return "END LOG\n";
	}

	private String calcDate(long millisecs) {
		SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date resultdate = new Date(millisecs);
		return date_format.format(resultdate);
	}
}