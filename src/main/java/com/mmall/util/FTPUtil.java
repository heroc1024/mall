package com.mmall.util;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class FTPUtil {
    private static final Logger logger = LoggerFactory.getLogger(FTPUtil.class);

    private static String ftpIp       = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpUser     = PropertiesUtil.getProperty("ftp.user");
    private static String ftpPassword = PropertiesUtil.getProperty("ftp.pass");

    private String    ip;
    private Integer   port;
    private String    user;
    private String    password;
    private FTPClient ftpClient;

    public static boolean uploadFile(List<File> fileList) throws IOException {
        FTPUtil ftpUtil = new FTPUtil(ftpIp, 21, ftpUser, ftpPassword);
        logger.info("开始连接FTP服务器");
        boolean result = ftpUtil.uploadFile("img", fileList);
        logger.info("结束上传，结果:{}",result);
        return result;
    }

    private boolean uploadFile(String remotePath, List<File> fileList) throws IOException {
        boolean uploaded = true;
        FileInputStream fis = null;
        //连接FTP服务器
        if (connectServer(ip, port, user, password)) {
            try {
                ftpClient.changeWorkingDirectory(remotePath);
                ftpClient.setBufferSize(1024);
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();
                for (File f : fileList) {
                    fis = new FileInputStream(f);
                    ftpClient.storeFile(f.getName(), fis);
                }
            } catch (IOException e) {
                uploaded = false;
                logger.error("文件上传失败", e.getMessage());
            } finally {
                fis.close();
                ftpClient.disconnect();
            }
        }
        return uploaded;
    }

    private boolean connectServer(String ip, Integer port, String user, String password) {
        boolean isSussess = false;
        ftpClient = new FTPClient();
        try {
            ftpClient.connect("127.0.0.1",21);
            isSussess = ftpClient.login("root", "root");
        } catch (IOException e) {
            logger.error("FTP服务器连接失败", e.getMessage());
        }
        return isSussess;
    }

    public FTPUtil(String ip, Integer port, String user, String password) {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    public static String getFtpIp() {
        return ftpIp;
    }

    public static void setFtpIp(String ftpIp) {
        FTPUtil.ftpIp = ftpIp;
    }

    public static String getFtpUser() {
        return ftpUser;
    }

    public static void setFtpUser(String ftpUser) {
        FTPUtil.ftpUser = ftpUser;
    }

    public static String getFtpPassword() {
        return ftpPassword;
    }

    public static void setFtpPassword(String ftpPassword) {
        FTPUtil.ftpPassword = ftpPassword;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }

    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }
}
