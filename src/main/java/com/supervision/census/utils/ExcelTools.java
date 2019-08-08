package com.supervision.census.utils;

import com.supervision.project.ViewModel.ViewProject;
import org.apache.poi.hssf.usermodel.*;

import java.util.List;

import static java.math.BigDecimal.ROUND_HALF_DOWN;

/*
 * @Project:SupervisionSystem
 * @Description:excel transfer
 * @Author:TjSanshao
 * @Create:2019-05-21 20:39
 *
 **/
public class ExcelTools {

    public static final String[] PROJECT_HEADERS = {
            "项目编号",
            "项目名称",
            "委托方",
            "委托方联系方式",
            "商务代表",
            "合同签订时间",
            "建设工期",
            "服务周期",
            "是否常驻",
            "非常驻每周天数",
            "工程预算",
            "监理费",
            "付款比例",
            "已付款",
            "剩余款",
            "总监",
            "总监代表",
            "当前进度"
    };

    public static HSSFWorkbook exportProjectsExcel(String[] headers, List<ViewProject> projects) {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("Projects");
        HSSFRow row = sheet.createRow(0);
        HSSFCellStyle style = wb.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);

        for (int i = 0; i < headers.length; i++) {
            HSSFCell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
            sheet.autoSizeColumn(i);
        }

        for (int i = 0; i < projects.size(); i++) {
            row = sheet.createRow(i + 1);
            ViewProject project = projects.get(i);
            row.createCell(0).setCellValue(project.getId());
            row.createCell(1).setCellValue(project.getProjectName());
            row.createCell(2).setCellValue(project.getClient());
            row.createCell(3).setCellValue(project.getClientContact());
            row.createCell(4).setCellValue(project.getPersonCharge());
            row.createCell(5).setCellValue(project.getAgreementTime());
            row.createCell(6).setCellValue(project.getBuildDuration() + "个月");
            row.createCell(7).setCellValue(project.getServiceDuration() + "个月");
            row.createCell(8).setCellValue(project.getIsResident() == 1 ? "是" : "否");
            row.createCell(9).setCellValue(project.getNotResidentWeekDays());
            row.createCell(10).setCellValue(project.getProjectBudget().toString());
            row.createCell(11).setCellValue(project.getSupervisionAmount().toString());
            row.createCell(12).setCellValue(project.getSupervisionAmount().intValue() == 0 ? Integer.toString(0) : project.getSupervisionPaid().divide(project.getSupervisionAmount(), 2, ROUND_HALF_DOWN).toString());
            row.createCell(13).setCellValue(project.getSupervisionPaid().toString());
            row.createCell(14).setCellValue(project.getSupervisionAmount().subtract(project.getSupervisionPaid()).toString());
            row.createCell(15).setCellValue(project.getProjectDirector().getUserRealname());
            row.createCell(16).setCellValue(project.getDelegate().getUserRealname());
            row.createCell(17).setCellValue(project.getProcess());
        }

        return wb;
    }

}
