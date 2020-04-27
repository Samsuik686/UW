package com.jimilab.uwclient.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Xml;

import com.jimilab.uwclient.bean.Material;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class FileUtils {

    private static final String TAG = "FileUtils";
    private static final String SHAREDSTRINGS = "xl/sharedStrings.xml";
    private static final String DIRSHEET = "xl/worksheets/";
    private static final String ENDXML = ".xml";

    private static List<String> listCells;


    //4.4以后的原生系统选择文件路径
    public static String getAboveKITKATPath(Context context, Uri uri) {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                String[] split = docId.split(":");
                String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                String[] split = docId.split(":");
                String type = split[0];

                Uri contentUri = null;
                if ("image".equalsIgnoreCase(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equalsIgnoreCase(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equalsIgnoreCase(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = "_id=?";
                String[] selectionArg = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArg);
            } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                return getDataColumn(context, uri, null, null);
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equalsIgnoreCase(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equalsIgnoreCase(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    //Get the value of the data column for this Uri. This is useful for
    // MediaStore Uris, and other file-based ContentProviders.
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArg) {
        Cursor cursor = null;
        String column = "_data";
        String[] projection = {column};
        cursor = context.getContentResolver().query(uri, projection, selection, selectionArg, null);
        if (cursor != null && cursor.moveToFirst()) {
            int index = cursor.getColumnIndexOrThrow(column);
            String path = cursor.getString(index);
            cursor.close();
            return path;
        }
        return null;
    }


    //4.4系统以下的原生系统选择文件路径
    public static String getBelowKITKATPath(Context context, Uri uri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        res = cursor.getString(column_index);
        cursor.close();
        return res;
    }

    /**
     * 解析 xls 格式的Excel表格
     *
     * @param fileName
     * @return
     */
    public static Map<String, List<List<String>>> analyzeXls(String fileName) {
        Log.d(TAG, "analyzeXls - " + fileName);
        File file = new File(fileName);
        if (!file.exists()) {
            Log.d(TAG, "analyzeXls - file not exists");
            return null;
        }
        Map<String, List<List<String>>> map = new HashMap<>();
        List<List<String>> rows;
        List<String> columns = null;
        try {
            Workbook workbook = Workbook.getWorkbook(file);
            Sheet[] sheets = workbook.getSheets();
            for (Sheet sheet : sheets) {
                rows = new ArrayList<>();
                String sheetName = sheet.getName();
                //遍历表格行
                for (int i = 0; i < sheet.getRows(); i++) {
                    //某一行内容
                    Cell[] sheetRow = sheet.getRow(i);
                    //表格列数
                    int column = sheet.getColumns();
                    //遍历行内容
                    for (int j = 0; j < sheetRow.length; j++) {
                        if (j % column == 0) {
                            columns = new ArrayList<>();
                        }
                        columns.add(sheetRow[j].getContents());
                    }
                    rows.add(columns);
                }
                map.put(sheetName, rows);
            }

            Iterator<Map.Entry<String, List<List<String>>>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, List<List<String>>> next = iterator.next();
                Iterator<List<String>> iterator1 = next.getValue().iterator();
                while (iterator1.hasNext()) {
                    Log.d(TAG, "sheet - " + next.getKey() + " - row - " + iterator1.next());
                }
            }

        } catch (IOException e) {
            Log.d(TAG, "analyzeXls - " + e.toString());
            e.printStackTrace();
        } catch (BiffException e) {
            Log.d(TAG, "analyzeXls - " + e.toString());
            e.printStackTrace();
        }
        return map;
    }


    public static Map<String, List<List<String>>> analyzeXlsx(String fileName) {
        Map<String, List<List<String>>> map = new HashMap<>();
        InputStream isShareStrings = null;
        InputStream isXlsx = null;
        ZipInputStream zipInputStream = null;
        listCells = new ArrayList<>();
        try {
            ZipFile zipFile = new ZipFile(new File(fileName));
            ZipEntry sharedStringXML = zipFile.getEntry(SHAREDSTRINGS);
            isShareStrings = zipFile.getInputStream(sharedStringXML);
            //
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setInput(isShareStrings, "utf-8");
            int eventType = xmlPullParser.getEventType();
            while (eventType != xmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        String tag = xmlPullParser.getName();
                        if ("t".equals(tag)) {
                            listCells.add(xmlPullParser.nextText());
                        }
                        break;
                    default:
                        break;
                }
                eventType = xmlPullParser.next();
            }
            Log.i(TAG, "analyzeXlsx: listCells --> " + listCells);
            //
            isXlsx = new BufferedInputStream(new FileInputStream(fileName));
            zipInputStream = new ZipInputStream(isXlsx);
            ZipEntry zipDir;
            while ((zipDir = zipInputStream.getNextEntry()) != null) {
                String dirName = zipDir.getName();
                if (!zipDir.isDirectory() && dirName.endsWith(ENDXML)) {
                    if (dirName.contains(DIRSHEET)) {
                        parseSheet(zipFile, dirName, map);
                    }
                }
            }
            Log.i(TAG, "analyzeXlsx -->  - 227");
        } catch (IOException e) {
            Log.i(TAG, "analyzeXlsx --> " + e.toString() + " - 227");
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            Log.i(TAG, "analyzeXlsx --> " + e.toString() + " - 231");
        } finally {
            try {
                zipInputStream.close();
            } catch (IOException e) {
                Log.i(TAG, "analyzeXlsx --> " + e.toString() + " - 233");
                e.printStackTrace();
            }
            try {
                isXlsx.close();
            } catch (IOException e) {
                Log.i(TAG, "analyzeXlsx --> " + e.toString() + " - 239");
                e.printStackTrace();
            }
            try {
                isShareStrings.close();
            } catch (IOException e) {
                Log.i(TAG, "analyzeXlsx --> " + e.toString() + " - 245");
                e.printStackTrace();
            }
        }
        Iterator<Map.Entry<String, List<List<String>>>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<List<String>>> next = iterator.next();
            Iterator<List<String>> iterator1 = next.getValue().iterator();
            while (iterator1.hasNext()) {
                Log.i(TAG, "analyzeXlsx: sheet --> " + next.getKey() + " row --> " + iterator1.next());
            }
        }
        return map;
    }

    private static void parseSheet(ZipFile zipFile, String entryName, Map<String, List<List<String>>> map) {
        int lastIndexOf = entryName.lastIndexOf(File.separator);
        String sheetName = entryName.substring(lastIndexOf + 1, entryName.length() - 4);
        String v = null;
        List<String> colums = null;
        List<List<String>> rows = new ArrayList<>();
        InputStream inputStreamSheet = null;
        try {
            ZipEntry sheet = zipFile.getEntry(entryName);
            inputStreamSheet = zipFile.getInputStream(sheet);
            XmlPullParser xmlPullParserSheet = Xml.newPullParser();
            xmlPullParserSheet.setInput(inputStreamSheet, "utf-8");
            int evenTypeSheet = xmlPullParserSheet.getEventType();
            while (xmlPullParserSheet.END_DOCUMENT != evenTypeSheet) {
                switch (evenTypeSheet) {
                    case XmlPullParser.START_TAG:
                        String tag = xmlPullParserSheet.getName();
                        if ("row".equalsIgnoreCase(tag)) {
                            colums = new ArrayList<>();
                        } else if ("v".equalsIgnoreCase(tag)) {
                            v = xmlPullParserSheet.nextText();
                            if (v != null) {
                                colums.add(listCells.get(Integer.parseInt(v)));
                            } else {
                                colums.add(v);
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if ("row".equalsIgnoreCase(xmlPullParserSheet.getName()) && v != null) {
                            rows.add(colums);
                        }
                        break;
                }
                evenTypeSheet = xmlPullParserSheet.next();
            }
            if (rows != null && rows.size() > 0) {
                map.put(sheetName, rows);
            }
        } catch (IOException e) {
            Log.i(TAG, "parseSheet --> " + e.toString() + "- 301");
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            Log.i(TAG, "parseSheet --> " + e.toString() + "- 305");
        } finally {
            try {
                inputStreamSheet.close();
            } catch (IOException e) {
                Log.i(TAG, "parseSheet --> " + e.toString() + "- 307");
                e.printStackTrace();
            }
        }
    }

    /**
     * poi库解析xlsx或xls
     *
     * @param fileName
     */
    public static List<Material> poiAnalyze(String fileName, HashSet<String> materialNoSet) throws Throwable {
        File file = new File(fileName);
        if (file.length() > 512000) {//文件大于 500kb即提示
            throw new OutOfMemoryError("导入的文件过大!");
        }
        List<Material> materialList = new ArrayList<>();
        //try {
        List<List<String>> rows = new ArrayList<>();
        List<String> columns = null;

        org.apache.poi.ss.usermodel.Workbook wb = WorkbookFactory.create(file);
        FormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();
        org.apache.poi.ss.usermodel.Sheet st = wb.getSheetAt(0);
        String stName = st.getSheetName();

        int rowSize = st.getPhysicalNumberOfRows();

        for (int i = 0; i < rowSize; i++) {
            Row row = st.getRow(i);
            //java.lang.NullPointerException: Attempt to invoke interface method 'int org.apache.poi.ss.usermodel.Row.getPhysicalNumberOfCells()' on a null object reference
            int col = row.getPhysicalNumberOfCells();
            columns = new ArrayList<>();

            for (int c = 0; c < col; c++) {

                columns.add(getCellAsString(row, c, formulaEvaluator));
            }
            if (i > 0) {
                //保存料信息
                Material material = Material.getMaterial(columns);
                materialList.add(material);
                materialNoSet.add(material.getMaterialNo());
                Log.d(TAG, "material - " + Material.materialToString(material));
            }

            rows.add(columns);
        }


//            Iterator<List<String>> iterator = rows.iterator();
//            while (iterator.hasNext()) {
//                Log.d(TAG, "row - " + iterator.next());
//            }


        //} catch (Exception e) {
        //    Log.d(TAG, e.toString());
        //    materialList = null;
        //}
        return materialList;
    }

    private static String getCellAsString(Row row, int c, FormulaEvaluator formulaEvaluator) {
        String value = "";
        try {
            org.apache.poi.ss.usermodel.Cell cell = row.getCell(c);

            CellValue cellValue = formulaEvaluator.evaluate(cell);

            if (cellValue == null) {
                return value;
            }

//            value = "" + cellValue.getStringValue();

            int cellType = cellValue.getCellType();

//            Log.d(TAG, "cellType - " + cellType);

            switch (cellType) {

                case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BOOLEAN:
                    value = "" + cellValue.getBooleanValue();

                    break;
                case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC:
                    double numericValue = cellValue.getNumberValue();
                    if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        double date = cellValue.getNumberValue();
                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
                        value = formatter.format(HSSFDateUtil.getJavaDate(date));
                    } else {
                        value = "" + (long) numericValue;
                    }
                    break;
                case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING:
                    value = "" + cellValue.getStringValue();
                    break;
                case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BLANK:
                default:
                    break;
            }


        } catch (NullPointerException e) {
            /* proper error handling should be here */
            Log.d(TAG, e.toString());
        }
        return value;
    }


}
