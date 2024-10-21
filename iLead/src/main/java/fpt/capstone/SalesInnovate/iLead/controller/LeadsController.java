package fpt.capstone.SalesInnovate.iLead.controller;


import fpt.capstone.SalesInnovate.iLead.dto.request.AssignUserDTO;
import fpt.capstone.SalesInnovate.iLead.dto.request.LeadDTO;
import fpt.capstone.SalesInnovate.iLead.dto.request.LeadsUserDTO;
import fpt.capstone.SalesInnovate.iLead.dto.response.ResponseData;
import fpt.capstone.SalesInnovate.iLead.dto.response.ResponseError;
import fpt.capstone.SalesInnovate.iLead.service.LeadsService;
import fpt.capstone.SalesInnovate.iLead.util.KeycloakSecurityUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/leads")
public class LeadsController {
    //    private LeadServiceImpl leadsService;
    private final LeadsService leadsService;
    @Autowired
    KeycloakSecurityUtil keycloakUtil ;

    @PostMapping("/upload-leads-data")
    public ResponseData<?> uploadCustomersData(@RequestParam("file") MultipartFile file) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId1 = authentication.getName();
            leadsService.saveLeadsToDatabase(file, userId1);
            return new ResponseData<>(HttpStatus.CREATED.value(), "Import file success", 1);
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(),"Các cột như : First Name , Last Name, Gender, Title, Email, Phone, Website, COmpany, Employee No Không được để trống Và Email + Phone không  được trùng nhau. : ");
        }
    }

    @GetMapping("/leads-list")
    public ResponseData<?> getLeads(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                    @RequestParam(defaultValue = "20", required = false) int pageSize) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return new ResponseData<>(1, HttpStatus.OK.value(), leadsService.getAllLeadsWithSortByDefault(userId,pageNo, pageSize));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/{leadId}")
    public ResponseData<?> getLeadDetail(@PathVariable Long leadId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return new ResponseData<>(1, HttpStatus.OK.value(), leadsService.getLeadDetail(leadId));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "list Leads fail");
        }
    }

    @PostMapping("/create-leads")
    public ResponseData<?> createLead(@RequestBody LeadDTO leadDTO) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            long leadId = leadsService.createLead(userId,leadDTO);
            return new ResponseData<>(HttpStatus.CREATED.value(), "Create Leads success", leadId, 1);
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PatchMapping("/patch-leads/{id}")
    public ResponseData<?> patchLead(@RequestBody LeadDTO leadDTO, @PathVariable(name = "id") long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        return leadsService.patchLead(userId,leadDTO, id) ?
                new ResponseData<>(HttpStatus.OK.value(), "Update Leads success", 1)
                : new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "Update Leads fail");

    }

    @DeleteMapping("/delete-leads/{id}")
    public ResponseData<?> deleteLead(@PathVariable(name = "id") Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return leadsService.deleteLeadById(userId,id) ?
                    new ResponseData<>(HttpStatus.OK.value(), "Delete Leads success", 1) :
                    new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "Delete Leads fail");
        }catch (Exception e){
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }


    }

    @GetMapping("/industry-list")
    public ResponseData<?> getAllIndustry() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return new ResponseData<>(1, HttpStatus.OK.value(), leadsService.getAllIndustry());
        } catch (Exception e) {

            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "list industry fail");
        }
    }

    @GetMapping("/status-list")
    public ResponseData<?> getAllLeadStatus() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return new ResponseData<>(1, HttpStatus.OK.value(), leadsService.geLeadStatus());
        } catch (Exception e) {

            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "list status fail");
        }
    }

    @GetMapping("/salution-list")
    public ResponseData<?> getAllLeadSalution() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return new ResponseData<>(1, HttpStatus.OK.value(), leadsService.geLeadSalution());
        } catch (Exception e) {

            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "list status fail");
        }
    }

    @GetMapping("/source-list")
    public ResponseData<?> getAllLeadSource() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return new ResponseData<>(1, HttpStatus.OK.value(), leadsService.getAllLeadSource());
        } catch (Exception e) {

            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "list source fail");
        }
    }

    @GetMapping("/filterSearch")
    public ResponseData<?> filterLeadsWithSpecifications(Pageable pageable,
                                                         @RequestParam(required = false) String[] search) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    leadsService.filterLeadsWithSpecifications(userId,pageable, search));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "list source fail");
        }
    }

    @GetMapping("/leads-by-status")
    public ResponseData<?> getLeadsByStatus(@RequestParam long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    leadsService.getLeadsByStatus(id));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PatchMapping("/patch-list-lead")
    public ResponseData<?> patchListLead(@RequestParam Long[] id, @RequestBody LeadDTO leadDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        return leadsService.patchListLead(userId,id, leadDTO) ?
                new ResponseData<>(1, HttpStatus.OK.value(), "Update Lead success") :
                new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "Update leads fail");
    }

    @GetMapping("/export-file/{userId}")
    public ResponseEntity<byte[]> getFileExport(
    ) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        String filename = "leads.xlsx";
        ByteArrayInputStream fileExport = leadsService.getExportFileData(userId);

        if (fileExport == null || fileExport.available() == 0) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        byte[] fileBytes = fileExport.readAllBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        // Debugging headers and file size
        System.out.println("Content-Disposition: " + headers.get(HttpHeaders.CONTENT_DISPOSITION));
        System.out.println("Content-Type: " + headers.get(HttpHeaders.CONTENT_TYPE));
        System.out.println("File size: " + fileBytes.length);

        return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
    }

    @PostMapping("/add-users/{leadId}")
    public ResponseData<?> addUserToLead(@PathVariable Long leadId,@RequestBody List<LeadsUserDTO> leadsUserDTOS) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return leadsService.addUserToLead(userId,leadId,leadsUserDTOS) ?
                    new ResponseData<>(1, HttpStatus.OK.value(), "add users success") :
                    new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "add users fail");
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PostMapping("/add-users-to-leads")
    public ResponseData<?> addUserToListLead(@RequestBody AssignUserDTO leadsUserDTOS) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return leadsService.addUserToListLead(userId,leadsUserDTOS) ?
                    new ResponseData<>(1, HttpStatus.OK.value(), "add users success") :
                    new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "add users fail");
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/get-user/{leadId}")
    public ResponseData<?> getListUserInLead(@PathVariable Long leadId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    leadsService.getListUserInLead(leadId));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

}