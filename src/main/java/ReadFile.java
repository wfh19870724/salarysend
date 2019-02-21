
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ReadFile {
    private Logger logger = Logger.getLogger(ReadFile.class);
    private Workbook getReadWorkBookType(String filePath) {
        //xls-2003, xlsx-2007
        FileInputStream is = null;

        try {
            is = new FileInputStream(filePath);
            if (filePath.toLowerCase().endsWith("xlsx")) {
                return new XSSFWorkbook(is);
            } else if (filePath.toLowerCase().endsWith("xls")) {
                return new HSSFWorkbook(is);
            } else {
                //  抛出自定义的业务异常
                throw new RuntimeException("excel格式文件错误");
            }
        } catch (IOException e) {
            //  抛出自定义的业务异常
            logger.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private String getCellStringVal(Cell cell) {
        CellType cellType = cell.getCellTypeEnum();
        switch (cellType) {
            case NUMERIC:
                cell.setCellType(CellType.STRING);
                return cell.getStringCellValue();
            case STRING:
                return cell.getStringCellValue();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            case ERROR:
                return String.valueOf(cell.getErrorCellValue());
            default:
                return " ";
        }
    }

    public List<String> readExcel(String sourceFilePath) throws Exception {
        Workbook workbook = null;

        try {
            try {
                workbook = getReadWorkBookType(sourceFilePath);

            List<String> contents = new ArrayList<String>();

            //获取第一个sheet
            Sheet sheet = workbook.getSheetAt(0);
            //第0行是表名，忽略，从第二行开始读取
            for (int rowNum = 0; rowNum <= sheet.getLastRowNum(); rowNum++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(rowNum);
                StringBuilder sb = new StringBuilder();
                for (Iterator<Cell> it = row.cellIterator(); it.hasNext(); ) {
                    Cell e = it.next();
                    sb.append(String.valueOf(getCellStringVal(e).trim()));
                    if(it.hasNext()){
                        sb.append(",");
                    }
                }
                contents.add(sb.toString());
            }
                return contents;
            } catch (Exception e) {
                logger.error("文件读取失败"+e.getMessage());
                throw e;
            }

        } finally {
            IOUtils.closeQuietly(workbook);
        }

    }

}
