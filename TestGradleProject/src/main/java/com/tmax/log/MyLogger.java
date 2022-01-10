package com.tmax.log;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class MyLogger {
	// String을 기준으로 Logger 클래스 인스턴스 할당
	Logger logger = Logger.getLogger("mylogger");
	private static MyLogger instance = new MyLogger();

	// Level별 Log를 생성할 파일 지정
	public static final String errorLog = "log.txt";
	public static final String warningLog = "warning.txt";
	public static final String fineLog = "fine.txt";
	public static final String infoLog = "info.txt";


	private FileHandler logFile = null;
	private FileHandler warningFile = null;
	private FileHandler fineFile = null;
	private FileHandler infoFile = null;

	private MyLogger() {
		try {
			// path, append 방식으로 생성
			logFile = new FileHandler(errorLog, true);
			warningFile = new FileHandler(warningLog, true);
			fineFile = new FileHandler(fineLog, true);
			infoFile = new FileHandler(infoLog, true);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		logFile.setFormatter(new SimpleFormatter());
		warningFile.setFormatter(new SimpleFormatter());
		fineFile.setFormatter(new SimpleFormatter());
		infoFile.setFormatter(new SimpleFormatter());

		logFile.setLevel(Level.ALL);
		fineFile.setLevel(Level.FINE);
		infoFile.setLevel(Level.INFO);
		warningFile.setLevel(Level.WARNING);

		logger.addHandler(logFile);
		logger.addHandler(fineFile);
		logger.addHandler(infoFile);
		logger.addHandler(warningFile);
	}

	public static MyLogger getLogger() {
		return instance;
	}

	public void log(String msg) {
		logger.finest(msg);
		logger.finer(msg);
		logger.fine(msg);
		logger.config(msg);
		logger.info(msg);
		logger.warning(msg);
		logger.severe(msg);
	}
	
	public void info(String msg) {
		logger.info(msg);
	}

	public void fine(String msg) {
		logger.fine(msg);
	}

	public void warning(String msg) {
		logger.warning(msg);
	}
}