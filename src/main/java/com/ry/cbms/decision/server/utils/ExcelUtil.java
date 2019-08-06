package com.ry.cbms.decision.server.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.common.collect.Maps;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * excel工具类
 *
 * @author maoyang
 */
public class ExcelUtil {

    public static int BUFFER_SIZE = 5;

    public static void excelLocal(String path, String fileName, String[] headers, List<Object[]> datas) {
        Workbook workbook = getWorkbook (headers, datas);
        if (workbook != null) {
            ByteArrayOutputStream byteArrayOutputStream = null;
            FileOutputStream fileOutputStream = null;
            try {
                byteArrayOutputStream = new ByteArrayOutputStream ();
                workbook.write (byteArrayOutputStream);

                String suffix = ".xlsx";
                File file = new File (path + File.separator + fileName + suffix);
                if (!file.getParentFile ().exists ()) {
                    file.getParentFile ().mkdirs ();
                }

                fileOutputStream = new FileOutputStream (file);
                fileOutputStream.write (byteArrayOutputStream.toByteArray ());
            } catch (Exception e) {
                e.printStackTrace ();
            } finally {
                try {
                    if (fileOutputStream != null) {
                        fileOutputStream.close ();
                    }
                } catch (IOException e) {
                    e.printStackTrace ();
                }
                try {
                    if (byteArrayOutputStream != null) {
                        byteArrayOutputStream.close ();
                    }
                } catch (IOException e) {
                    e.printStackTrace ();
                }

                try {
                    workbook.close ();
                } catch (IOException e) {
                    e.printStackTrace ();
                }
            }
        }
    }

    /**
     * 导出excel
     *
     * @param fileName
     * @param headers
     * @param datas
     * @param response
     */
    public static void excelExport(String fileName, String[] headers, List<Object[]> datas,
                                   HttpServletResponse response) {
        Workbook workbook = getWorkbook (headers, datas);
        if (workbook != null) {
            ByteArrayOutputStream byteArrayOutputStream = null;
            try {
                byteArrayOutputStream = new ByteArrayOutputStream ();
                workbook.write (byteArrayOutputStream);

                String suffix = ".xls";
                response.setContentType ("application/vnd.ms-excel;charset=utf-8");
                response.setHeader ("Content-Disposition",
                        "attachment;filename=" + new String ((fileName + suffix).getBytes (), "iso-8859-1"));

                OutputStream outputStream = response.getOutputStream ();
                outputStream.write (byteArrayOutputStream.toByteArray ());
                outputStream.close ();
            } catch (Exception e) {
                e.printStackTrace ();
            } finally {
                try {
                    if (byteArrayOutputStream != null) {
                        byteArrayOutputStream.close ();
                    }
                } catch (IOException e) {
                    e.printStackTrace ();
                }

                try {
                    workbook.close ();
                } catch (IOException e) {
                    e.printStackTrace ();
                }
            }
        }
    }

    /**
     * @param headers 列头
     * @param datas   数据
     * @return
     */
    public static Workbook getWorkbook(String[] headers, List<Object[]> datas) {
        Workbook workbook = new HSSFWorkbook ();

        Sheet sheet = workbook.createSheet ();
        Row row = null;
        Cell cell = null;
        CellStyle style = workbook.createCellStyle ();
        style.setAlignment (HorizontalAlignment.CENTER_SELECTION);

        Font font = workbook.createFont ();

        int line = 0, maxColumn = 0;
        if (headers != null && headers.length > 0) {// 设置列头
            row = sheet.createRow (line++);
            row.setHeightInPoints (23);
            font.setBold (true);
            font.setFontHeightInPoints ((short) 13);
            style.setFont (font);

            maxColumn = headers.length;
            for (int i = 0; i < maxColumn; i++) {
                cell = row.createCell (i);
                cell.setCellValue (headers[i]);
                cell.setCellStyle (style);
            }
        }

        if (datas != null && datas.size () > 0) {// 渲染数据
            for (int index = 0, size = datas.size (); index < size; index++) {
                Object[] data = datas.get (index);
                if (data != null && data.length > 0) {
                    row = sheet.createRow (line++);
                    row.setHeightInPoints (20);

                    int length = data.length;
                    if (length > maxColumn) {
                        maxColumn = length;
                    }

                    for (int i = 0; i < length; i++) {
                        cell = row.createCell (i);
                        cell.setCellValue (data[i] == null ? null : data[i].toString ());
                    }
                }
            }
        }

        for (int i = 0; i < maxColumn; i++) {
            sheet.autoSizeColumn (i);
        }

        return workbook;
    }

    /**
     * 读取Excel的内容，第一维数组存储的是一行中格列的值，二维数组存储的是多少个行
     *
     * @param file 读取数据的源Excel
     * @return 读出的Excel中数据的内容
     */

    public static Map<Integer, List<String>> getData(File file) {
        Map dataMap = Maps.newConcurrentMap ();
        try {
            if (file.isFile () && file.exists ()) {   //判断文件是否存在
                String[] split = file.getName ().split ("\\.");  //.是特殊字符，需要转义！！！！！
                Workbook wb;
                //根据文件后缀（xls/xlsx）进行判断
                if ("xls".equals (split[1])) {
                    FileInputStream fis = new FileInputStream (file);   //文件流对象
                    wb = new HSSFWorkbook (fis);
                } else if ("xlsx".equals (split[1])) {
                    wb = new XSSFWorkbook (file);
                } else {
                    throw new RuntimeException ("文件类型错误");
                }
                //开始解析
                Sheet sheet = wb.getSheetAt (0);     //读取sheet 0
                int firstRowIndex = sheet.getFirstRowNum () + 1;   //第一行是列名，所以不读
                int lastRowIndex = sheet.getLastRowNum ();
                for (int rIndex = firstRowIndex; rIndex <= lastRowIndex; rIndex++) {   //遍历行
                    Row row = sheet.getRow (rIndex);
                    List dataList = new ArrayList ();
                    if (row != null) {
                        int firstCellIndex = row.getFirstCellNum ();
                        int lastCellIndex = row.getLastCellNum ();
                        for (int cIndex = firstCellIndex; cIndex < lastCellIndex; cIndex++) {   //遍历列
                            Cell cell = row.getCell (cIndex);
                            if (cell != null) {
                                dataList.add (cell.toString ());
                            }
                        }
                    }
                    dataMap.put (rIndex, dataList);
                }
            } else {
                return dataMap;
            }
        } catch (Exception e) {
            return dataMap;
        }
        return dataMap;
    }

    /**
     * 去掉字符串右边的空格
     *
     * @param str 要处理的字符串
     * @return 处理后的字符串
     */

    public static String rightTrim(String str) {
        if (str == null) {
            return "";
        }
        int length = str.length ();
        for (int i = length - 1; i >= 0; i--) {
            if (str.charAt (i) != 0x20) {
                break;
            }
            length--;
        }
        return str.substring (0, length);

    }
    public static void download(HttpServletRequest request, HttpServletResponse response, String filePath){
        File file = new File(filePath);
        // 取得文件名。
        String fileName = file.getName();
        InputStream fis = null;
        try {
            fis = new FileInputStream(file);
            request.setCharacterEncoding("UTF-8");
            String agent = request.getHeader("User-Agent").toUpperCase();
            if ((agent.indexOf("MSIE") > 0) || ((agent.indexOf("RV") != -1) && (agent.indexOf("FIREFOX") == -1)))
                fileName = URLEncoder.encode(fileName, "UTF-8");
            else {
                fileName = new String(fileName.getBytes("UTF-8"), "ISO8859-1");
            }
            response.reset();
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/force-download");// 设置强制下载不打开
            response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
            response.setHeader("Content-Length", String.valueOf(file.length()));

            byte[] b = new byte[1024];
            int len;
            while ((len = fis.read(b)) != -1) {
                response.getOutputStream().write(b, 0, len);
            }
            response.flushBuffer();
            fis.close();
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        //excel文件路径
//        String excelPath = "C:\\Users\\Administrator\\Desktop\\对账单模版.xlsx";
//        System.out.println(getData(new File(excelPath)));
    }


}


