package net.tradelib.misc;

import java.util.Map;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SftpUploader {
   public void upload(Map<String,String> files) throws Exception {
      JSch jsch = new JSch();
      Session session = null;
      
      session = jsch.getSession(user, host, 22);
      session.setConfig("StrictHostKeyChecking", "no");
      session.setPassword(password);
      session.connect();
      
      Channel channel = session.openChannel("sftp");
      channel.connect();
      ChannelSftp sftp = (ChannelSftp)channel;
      for(Map.Entry<String, String> ff : files.entrySet()) {
         // key - source, value - destination
         sftp.put(ff.getKey(), ff.getValue());
      }
      sftp.exit();
      session.disconnect();
   }
   
   public SftpUploader(String host, String user, String password) {
      this.host = host;
      this.user = user;
      this.password = password;
   }
   
   private String host;
   private String user;
   private String password;
}
