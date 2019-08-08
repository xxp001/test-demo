package com.supervision.document.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/*
 * @Project:SupervisionSystem
 * @Description:zip tools
 * @Author:TjSanshao
 * @Create:2019-03-22 01:44
 *
 **/
public class ZipUtils {

    private static final int BUFFER_SIZE = 1024 * 2;

    public static void main(String[] args) throws Exception {
        File file = new File("d:/test.zip");
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(file);
        ZipUtils.toZip("D:\\supervision\\测试用例01", fos, true);
    }

    public static void toZip(String dir, OutputStream out, boolean keepDirs) throws Exception {
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(out);
            File sourceFile = new File(dir);
            compress(sourceFile, zos, sourceFile.getName(), keepDirs);
            System.out.println("zip success!");
        } catch (Exception e) {
            throw new Exception("zip error!", e);
        } finally {
            if (zos != null) {
                zos.close();
            }
        }
    }

    /**
     * @param sourceFile 源文件
     * @param zos zip输出流
     * @param name 压缩后的名称
     * @param keepDirs 是否保留原来的目录结构
     **/
    private static void compress(File sourceFile, ZipOutputStream zos, String name, boolean keepDirs) throws Exception {
        byte[] buf = new byte[BUFFER_SIZE];

        if (sourceFile.isFile()) {
            // 向zip输出流中添加一个zip实体，构造器中name为zip实体的文件的名字
            zos.putNextEntry(new ZipEntry(name));
            // copy文件到zip输出流中
            int len;
            FileInputStream in = new FileInputStream(sourceFile);
            while ((len = in.read(buf)) != -1) {
                zos.write(buf, 0, len);
            }
            // Complete the entry
            zos.closeEntry();
            in.close();
        } else {
            //如果是文件夹
            File[] listFiles = sourceFile.listFiles();

            //如果是空文件夹
            if(listFiles == null || listFiles.length == 0){
                // 需要保留原来的文件结构时,需要对空文件夹进行处理
                if(keepDirs){
                    // 空文件夹的处理
                    zos.putNextEntry(new ZipEntry(name + "/"));
                    // 没有文件，不需要文件的copy
                    zos.closeEntry();
                }
            } else {
                for (File file : listFiles) {
                    //是否需要保留文件结构
                    if (keepDirs) {
                        compress(file, zos, name + "/" + file.getName(),keepDirs);
                    } else {
                        compress(file, zos, file.getName(),keepDirs);
                    }
                }
            }
        }
    }

}
