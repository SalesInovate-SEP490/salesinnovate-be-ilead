package fpt.capstone.SalesInnovate.iLead.service;


import fpt.capstone.SalesInnovate.iLead.dto.request.LeadStatusDTO;
import fpt.capstone.SalesInnovate.iLead.dto.response.PageResponse;

public interface LeadStatusService {
    boolean createLeadStatus(LeadStatusDTO leadStatusDTO);
    PageResponse<?> getAllLeadStatus(int page, int size);
    boolean updateLeadStatus(LeadStatusDTO leadStatusDTO);
    boolean deleteLeadStatus();
}
