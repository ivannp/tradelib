package net.tradelib.r;

import java.io.File;

public class Script {
   public void execute(String script) throws Exception {
      // Execute the script
      Process process = new ProcessBuilder(execPath.toString(), "-e", script).directory(workDir).start();
      process.waitFor();
   }
   
   public Script() {
      execPath = new File("Rscript.exe");
   }
   
   public Script(File rscript) {
      this.execPath = rscript;
   }
   
   public Script(File rscript, File workDir) {
      this.execPath = rscript;
      this.workDir = workDir;
   }
   
   public void setWorkDir(File workDir) {
      this.workDir = workDir;
   }
   
   public void setWorkdir(String s) {
      this.workDir = new File(s);
   }
   
   public void setExecPath(File execPath) {
      this.execPath = execPath;
   }
   
   public void setExecPath(String execPath) {
      this.execPath = new File(execPath);
   }
   
   private File execPath;
   private File workDir;
}
