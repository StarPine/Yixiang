package util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

import android.content.ContentValues;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

public class FileUtils {
    private static String SDCardRoot;
    private static boolean isCardExist;

    public FileUtils() throws NoSdcardException {
        getSDCardRoot();
    }

    public String getSDCardRoot() throws NoSdcardException {
        if (isCardExist()) {
            SDCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
        } else {
            throw new NoSdcardException();
        }
        return SDCardRoot;
    }

    public static boolean isCardExist() {
        isCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ? true : false;
        return isCardExist;
    }

    public File createFileInSDCard(String fileName, String dir)
            throws IOException {
        File file = new File(SDCardRoot + dir + File.separator + fileName);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        return file;
    }

    public File creatSDDir(String dir) {
        File dirFile = new File(SDCardRoot + dir + File.separator);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        return dirFile;
    }

    public boolean filterFileExist(String path, String filter) {
        File file = new File(SDCardRoot + path + File.separator);
        if (file.exists() && file.isDirectory()) {

            String[] fileNames = file.list(new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                    return filename.endsWith(".png");
                }
            });
            if (fileNames.length > 0) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     *
     */
    public boolean isFileExist(String fileName, String path) {
        File file = new File(SDCardRoot + path + File.separator + fileName);
        return file.exists();
    }

    public File getFile(String fileName, String path) {
        File file = new File(SDCardRoot + path + File.separator + fileName);
        return file;
    }

    public static void deleteFile(String fileName, String path) {
        File file = new File(SDCardRoot + path + File.separator + fileName);
        boolean result = file.delete();

        Log.e("deleteFile", fileName + " delete " + result);
    }

    public void closeInputStream(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                Log.e("error", "close failed");
                e.printStackTrace();
            }
        }
    }

    public class NoSdcardException extends Exception {

    }

    public static ContentValues getVideoContentValues(Context paramContext, File paramFile, long paramLong) {
        ContentValues localContentValues = new ContentValues();
        localContentValues.put("title", paramFile.getName());
        localContentValues.put("_display_name", paramFile.getName());
        localContentValues.put("mime_type", "video/3gp");
        localContentValues.put("datetaken", Long.valueOf(paramLong));
        localContentValues.put("date_modified", Long.valueOf(paramLong));
        localContentValues.put("date_added", Long.valueOf(paramLong));
        localContentValues.put("_data", paramFile.getAbsolutePath());
        localContentValues.put("_size", Long.valueOf(paramFile.length()));
        return localContentValues;
    }

    /**
     * 删除文件夹中的内部文件,不删除文件夹本身
     *
     * @param path
     */
    public static void deleteDirectoryContent(String path) {
        Log.w("test", "deleteDirectory.." + path);
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        String fPath = file.getAbsolutePath();
        if (file.isDirectory()) {
            String[] files = getDirectoryFiles(path);
            if (files == null) {
                deleteFile(path);
                return;
            }
            for (String str : files) {
                str = fPath + "/" + str;
                file = new File(str);
                if (file.isDirectory()) {
                    deleteDirectory(str);
                } else if (file.isFile()) {
                    deleteFile(str);
                }
            }
//			deleteFile(path);
        } else if (file.isFile()) {
            deleteFile(path);
        }
    }

    /**
     * 删除文件夹中的内部文件
     *
     * @param path
     */
    public static void deleteDirectory(String path) {
        Log.w("test", "deleteDirectory.." + path);
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        String fPath = file.getAbsolutePath();
        if (file.isDirectory()) {
            String[] files = getDirectoryFiles(path);
            if (files == null) {
                deleteFile(path);
                return;
            }
            for (String str : files) {
                str = fPath + "/" + str;
                file = new File(str);
                if (file.isDirectory()) {
                    deleteDirectory(str);
                } else if (file.isFile()) {
                    deleteFile(str);
                }
            }
            deleteFile(path);
        } else if (file.isFile()) {
            deleteFile(path);
        }
    }

    /**
     * 删除指定路径的文件
     *
     * @param filePath 文件路径
     */
    public static void deleteFile(String filePath) {
        Log.w("test", "deleteFile:filePath=" + filePath);
        if (filePath == null) {
            return;
        }
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }


    /**
     * 获取文件夹下面的图片文件
     *
     * @param path
     * @return
     */
    public static String[] getDirectoryFiles(String path) {
        if (path == null) {
            return null;
        }
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        String[] files = file.list();
        if (files == null || files.length <= 0) {
            return null;
        }
        return files;
    }
}
