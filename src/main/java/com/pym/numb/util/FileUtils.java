package com.pym.numb.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2017/8/24.
 */
public class FileUtils {

    public static List<File> getFiles(String dir, String endWiths) {
        List<File> list = new ArrayList<File> ();
        File file = new File(dir);
        if (!file.exists()) {
            return list;
        }
        if (file.isFile() && file.getName().endsWith(endWiths)) {
            list.add(file);
        } else {
            Arrays.asList(file.listFiles()).forEach(f -> {
                list.addAll(getFiles(f.getAbsolutePath(),endWiths));
            });
        }
        return list;
    }


}
