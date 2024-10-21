package fpt.capstone.SalesInnovate.iLead.controller;

import fpt.capstone.SalesInnovate.iLead.dto.request.LeadStatusDTO;
import fpt.capstone.SalesInnovate.iLead.dto.response.ResponseData;
import fpt.capstone.SalesInnovate.iLead.dto.response.ResponseError;
import fpt.capstone.SalesInnovate.iLead.service.LeadStatusService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/config-lead")
public class LeadStatusController {
    private final LeadStatusService leadStatusService;

    @GetMapping("/all")
    public ResponseData<?> getAllLeadStatus(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    leadStatusService.getAllLeadStatus(page, size));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "System error! Please try again later.");
        }
    }

    @PostMapping("/create")
    public ResponseData<?> createLeadStatus(
            @RequestBody LeadStatusDTO leadStatusDTO
    ) {
        try {
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    leadStatusService.createLeadStatus(leadStatusDTO));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "System error! Please try again later.");
        }
    }

    @PatchMapping("/update")
    public ResponseData<?> updateLeadStatus(
            @RequestBody LeadStatusDTO leadStatusDTO
    ) {
        try {
            return leadStatusService.updateLeadStatus(leadStatusDTO) ?
            new ResponseData<>(1, HttpStatus.OK.value(), "Add Lead Status successfully") :
            new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "Lead Status not found! Please try again later.");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "System error! Please try again later.");
        }
    }
}
