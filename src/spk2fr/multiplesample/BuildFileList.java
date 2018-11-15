/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr.multiplesample;

import java.io.File;
import java.util.LinkedList;

/**
 *
 * @author zx
 */
public class BuildFileList {

    public LinkedList<String[]> build(String rootPath) {
        LinkedList<String[]> fileList = new LinkedList<>();
        if (rootPath == null) {
            return null;
        }
        File root = new File(rootPath);
        if (!root.exists()) {
            return null;
        }
        File[] list = root.listFiles();

        if (list != null) {
            for (File f : list) {
                if (f.isDirectory()) {
                    fileList.addAll(build(f.getAbsolutePath()));
                } else {
                    String fPath = f.getAbsolutePath();
                    if (fPath.contains("_Event.mat")) {
                        String spktsPath = fPath.replaceAll("_\\d{6}_\\d{6}_Event.mat", "_Spk.mat");
                        if (spktsPath.equals(fPath)){
                            spktsPath = fPath.replaceAll("_Event.mat", "_Spk.mat");
                        }
//                        System.out.println(fPath + ", " + spktsPath);
                        if(new File(spktsPath).exists()){
                            fileList.add(new String[]{spktsPath,fPath});
                        }
                        
                    }
                }
            }
        }

        return fileList;
    }
    
    public String[][] toString(LinkedList<String[]> fileList){
        return fileList.toArray(new String[fileList.size()][]);
    }


}
