// Copyright 2015 Ivan Popivanov
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
