 package fpt.capstone.SalesInnovate.iLead.service;

import fpt.capstone.SalesInnovate.iLead.dto.Converter;
import fpt.capstone.SalesInnovate.iLead.dto.request.LeadExportDTO;
import fpt.capstone.SalesInnovate.iLead.dto.request.LeadImportFileDTO;
import fpt.capstone.SalesInnovate.iLead.model.*;
import fpt.capstone.SalesInnovate.iLead.repository.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

 @Slf4j
 @Service
 @AllArgsConstructor
public class ExcelUploadService {
     private Converter leadConverter;
     private AddressInformationRepository addressInformationRepository;
     private final LeadsRepository leadsRepository;
     private final LeadStatusRepository leadStatusRepository;
     private final IndustryRepository industryRepository;
     private final LeadSourceRepository leadSourceRepository;
     private final SearchRepository searchRepository;
     private final LeadRatingRepository leadRatingRepository;
     private final LeadSalutionRepository leadSalutionRepository;
     private static final String[] HEADER = {
             "id","LeadSalutionName", "First Name","Middle Name", "Last Name", "Gender", "Title", "Email",
             "Phone", "Website","Company", "Employee No", "Lead Source Name", "IndustryStatusName",
             "LeadStatusName","LeadRatingName", "Street", "City", "Province", "PostalCode", "Country"
     };
     private static final String SHEET_NAME = "Leads";

    public  boolean isValidExcelFile(MultipartFile file){

        return Objects.equals(file.getContentType(),
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }
     public List<Leads> getLeadDataFromExcel(InputStream inputStream, String userId) {
         List<Leads> listLeads = new ArrayList<>();
         List<AddressInformation> listAddressInformation = new ArrayList<>();
         try {
             XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
             XSSFSheet sheet = workbook.getSheet("leads");
             log.info("Read file excel");
             int rowIndex = 0;
             Set<String> existingPhones = new HashSet<>();
             Set<String> existingEmails = new HashSet<>();

            // Populate sets with existing phones and emails from the database
             leadsRepository.findAll().forEach(lead -> {
                 if (lead.getPhone() != null) {
                     existingPhones.add(lead.getPhone());
                 }
                 if (lead.getEmail() != null) {
                     existingEmails.add(lead.getEmail());
                 }
             });

             for (Row row : sheet) {
                 if (rowIndex == 0) {
                     rowIndex++;
                     continue;
                 }
                 boolean isEmptyRow = true;
                 for (Cell cell : row) {
                     if (cell != null && cell.getCellType() != CellType.BLANK) {
                         isEmptyRow = false;
                         break;
                     }
                 }
                 if (isEmptyRow) {
                     break;
                 }

                 int cellIndex = 0;
                 LeadImportFileDTO leadImportFileDTO = new LeadImportFileDTO();
                 for (Cell cell : row) {
                     log.info("Add data from file excel in column :" + cellIndex );
                     log.info("Add data from file excel in row:"  +  rowIndex);
                     String cellValue = getCellValueAsString(cell);
                     switch (cellIndex) {
                         case 0 -> leadImportFileDTO.setFirstName(cellValue);
                         case 1 -> leadImportFileDTO.setLastName(cellValue);
                         case 2 -> leadImportFileDTO.setGender(cellValue);
                         case 3 -> leadImportFileDTO.setTitle(cellValue);
                         case 4 -> leadImportFileDTO.setEmail(cellValue);
                         case 5 -> leadImportFileDTO.setPhone(cellValue);
                         case 6 -> leadImportFileDTO.setWebsite(cellValue);
                         case 7 -> leadImportFileDTO.setCompany(cellValue);
                         case 8 -> leadImportFileDTO.setNoEmployee(cellValue);
                         case 9 -> leadImportFileDTO.setLeadSourceName(cellValue);
                         case 10 -> leadImportFileDTO.setIndustryStatusName(cellValue);
                         case 11 -> leadImportFileDTO.setLeadStatusName(cellValue);
                         case 12 -> leadImportFileDTO.setLeadRatingName(cellValue);
                         case 13 -> leadImportFileDTO.setStreet(cellValue);
                         case 14 -> leadImportFileDTO.setCity(cellValue);
                         case 15 -> leadImportFileDTO.setProvince(cellValue);
                         case 16 -> leadImportFileDTO.setPostalCode(cellValue);
                         case 17 -> leadImportFileDTO.setCountry(cellValue);
                         case 18 -> leadImportFileDTO.setLeadSalutionName(cellValue);
                         default -> log.warn("Unexpected column index: + row:" + cellIndex + rowIndex);
                     }
                     cellIndex++;
                 }

                 // Check if phone or email already exists
                 if ((leadImportFileDTO.getPhone() != null && existingPhones.contains(leadImportFileDTO.getPhone())) ||
                         (leadImportFileDTO.getEmail() != null && existingEmails.contains(leadImportFileDTO.getEmail()))) {
                     log.warn("Duplicate phone or email found in row: " + rowIndex);
                     throw new RuntimeException("Duplicate phone or email found in row: " + rowIndex);
                 }

                 // Add phone and email to the sets
                 if (leadImportFileDTO.getPhone() != null) {
                     existingPhones.add(leadImportFileDTO.getPhone());
                 }
                 if (leadImportFileDTO.getEmail() != null) {
                     existingEmails.add(leadImportFileDTO.getEmail());
                 }

                 AddressInformation addressInformation =
                         leadConverter.DataToAddressInformation(leadImportFileDTO.getStreet(),
                                 leadImportFileDTO.getCity(), leadImportFileDTO.getProvince(),
                                 leadImportFileDTO.getPostalCode(), leadImportFileDTO.getCountry());
                 AddressInformation savedAddressInformation = addressInformationRepository.save(addressInformation);
                 listAddressInformation.add(addressInformation);
                 Leads lead = leadConverter.
                         convertFileImportToLeads(leadImportFileDTO, savedAddressInformation, userId);
                 listLeads.add(lead);
             }
             log.info("Add data from file excel success");
             return listLeads;
         } catch (Exception e) {
             e.getStackTrace();
             throw new RuntimeException("Fail to parse Excel file: " + e.getMessage());
         }
     }

     private String getCellValueAsString(Cell cell) {
         if (cell == null|| cell.getStringCellValue().isEmpty() || cell.getCellType() == CellType.BLANK) {
             return "";
         }
         return switch (cell.getCellType()) {
             case STRING -> cell.getStringCellValue().trim();
             case NUMERIC -> String.valueOf(cell.getNumericCellValue());
             case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
             default -> null;
         };
     }
     public ByteArrayInputStream dataToExcel(List<Leads> list) throws IOException {
         try (SXSSFWorkbook workbook = new SXSSFWorkbook();  // Sử dụng SXSSFWorkbook cho bộ nhớ hiệu quả hơn
              ByteArrayOutputStream out = new ByteArrayOutputStream()) {

             Sheet sheet = workbook.createSheet(SHEET_NAME);
             Row headerRow = sheet.createRow(0);

             // Tạo tiêu đề
             for (int i = 0; i < HEADER.length; i++) {
                 Cell cell = headerRow.createCell(i);
                 cell.setCellValue(HEADER[i]);
             }

             // Chuyển đổi danh sách Leads sang LeadExportDTO
             List<LeadExportDTO> leadExportDTOs = leadConverter.LeadEntityListToLeadExportDTOList(list);
             int rowIndex = 1;  // Bắt đầu từ hàng thứ hai (hàng đầu tiên là tiêu đề)


// Truy xuất tất cả các giá trị cần thiết trước khi vào vòng lặp
             List<Long> addressInformationIds = leadExportDTOs.stream()
                     .map(LeadExportDTO::getAddressInformationId)
                     .filter(Objects::nonNull)
                     .distinct()
                     .collect(Collectors.toList());
             Map<Long, AddressInformation> addressInformationCache = addressInformationRepository.findAllById(addressInformationIds)
                     .stream()
                     .collect(Collectors.toMap(AddressInformation::getAddressInformationId, Function.identity()));

             List<Long> leadSalutionIds = leadExportDTOs.stream()
                     .map(LeadExportDTO::getLeadSalutionId)
                     .filter(Objects::nonNull)
                     .distinct()
                     .collect(Collectors.toList());
             Map<Long, String> leadSalutionCache = leadSalutionRepository.findAllById(leadSalutionIds)
                     .stream()
                     .collect(Collectors.toMap(LeadSalution::getLeadSalutionId, LeadSalution::getLeadSalutionName));

             List<Long> leadSourceIds = leadExportDTOs.stream()
                     .map(LeadExportDTO::getLeadSourceId)
                     .filter(Objects::nonNull)
                     .distinct()
                     .collect(Collectors.toList());
             Map<Long, String> leadSourceCache = leadSourceRepository.findAllById(leadSourceIds)
                     .stream()
                     .collect(Collectors.toMap(LeadSource::getLeadSourceId, LeadSource::getLeadSourceName));

             List<Long> industryIds = leadExportDTOs.stream()
                     .map(LeadExportDTO::getIndustryId)
                     .filter(Objects::nonNull)
                     .distinct()
                     .collect(Collectors.toList());
             Map<Long, String> industryStatusCache = industryRepository.findAllById(industryIds)
                     .stream()
                     .collect(Collectors.toMap(Industry::getIndustryId, Industry::getIndustryStatusName));

             List<Long> leadStatusIds = leadExportDTOs.stream()
                     .map(LeadExportDTO::getLeadStatusID)
                     .filter(Objects::nonNull)
                     .distinct()
                     .collect(Collectors.toList());
             Map<Long, String> leadStatusCache = leadStatusRepository.findAllById(leadStatusIds)
                     .stream()
                     .collect(Collectors.toMap(LeadStatus::getLeadStatusId, LeadStatus::getLeadStatusName));

// Bắt đầu vòng lặp sau khi đã lấy hết dữ liệu cần thiết
             for (LeadExportDTO lead : leadExportDTOs) {
                 Row dataRow = sheet.createRow(rowIndex++);

                 AddressInformation addressInformation = lead.getAddressInformationId() != null ?
                         addressInformationCache.get(lead.getAddressInformationId()) : null;

                 String leadSalutionName = lead.getLeadSalutionId() != null ?
                         leadSalutionCache.getOrDefault(lead.getLeadSalutionId(), "") : "";

                 String leadSourceName = lead.getLeadSourceId() != null ?
                         leadSourceCache.getOrDefault(lead.getLeadSourceId(), "") : "";

                 String industryStatusName = lead.getIndustryId() != null ?
                         industryStatusCache.getOrDefault(lead.getIndustryId(), "") : "";

                 String leadStatusName = lead.getLeadStatusID() != null ?
                         leadStatusCache.getOrDefault(lead.getLeadStatusID(), "") : "";
                 // Điền dữ liệu vào các ô trong hàng
                 dataRow.createCell(0).setCellValue(Optional.ofNullable(lead.getLeadId()).orElse(0L));
                 dataRow.createCell(1).setCellValue(Optional.ofNullable(leadSalutionName).orElse(""));
                 dataRow.createCell(2).setCellValue(Optional.ofNullable(lead.getFirstName()).orElse(""));
                 dataRow.createCell(3).setCellValue(Optional.ofNullable(lead.getMiddleName()).orElse(""));
                 dataRow.createCell(4).setCellValue(Optional.ofNullable(lead.getLastName()).orElse(""));
                 dataRow.createCell(5).setCellValue(Optional.ofNullable(lead.getGender()).orElse(0));
                 dataRow.createCell(6).setCellValue(Optional.ofNullable(lead.getTitle()).orElse(""));
                 dataRow.createCell(7).setCellValue(Optional.ofNullable(lead.getEmail()).orElse(""));
                 dataRow.createCell(8).setCellValue(Optional.ofNullable(lead.getPhone()).orElse(""));
                 dataRow.createCell(9).setCellValue(Optional.ofNullable(lead.getWebsite()).orElse(""));
                 dataRow.createCell(10).setCellValue(Optional.ofNullable(lead.getCompany()).orElse(""));
                 dataRow.createCell(11).setCellValue(Optional.ofNullable(lead.getNoEmployee()).orElse(0));
                 dataRow.createCell(12).setCellValue(Optional.ofNullable(leadSourceName).orElse(""));
                 dataRow.createCell(13).setCellValue(Optional.ofNullable(industryStatusName).orElse(""));
                 dataRow.createCell(14).setCellValue(Optional.ofNullable(leadStatusName).orElse(""));
                 dataRow.createCell(15).setCellValue(Optional.ofNullable(addressInformation).map(AddressInformation::getStreet).orElse(""));
                 dataRow.createCell(16).setCellValue(Optional.ofNullable(addressInformation).map(AddressInformation::getCity).orElse(""));
                 dataRow.createCell(17).setCellValue(Optional.ofNullable(addressInformation).map(AddressInformation::getProvince).orElse(""));
                 dataRow.createCell(18).setCellValue(Optional.ofNullable(addressInformation).map(AddressInformation::getPostalCode).orElse(""));
                 dataRow.createCell(19).setCellValue(Optional.ofNullable(addressInformation).map(AddressInformation::getCountry).orElse(""));
             }

             // Ghi dữ liệu vào OutputStream
             workbook.write(out);
             return new ByteArrayInputStream(out.toByteArray());

         } catch (IOException e) {
             e.printStackTrace();
             System.out.println("Fail to export file");
             return null;
         }
     }
}
