package fpt.capstone.SalesInnovate.iLead.service.impl;

import fpt.capstone.SalesInnovate.iLead.dto.Converter;
import fpt.capstone.SalesInnovate.iLead.dto.request.LeadStatusDTO;
import fpt.capstone.SalesInnovate.iLead.dto.response.PageResponse;
import fpt.capstone.SalesInnovate.iLead.model.LeadStatus;
import fpt.capstone.SalesInnovate.iLead.repository.LeadStatusRepository;
import fpt.capstone.SalesInnovate.iLead.service.LeadStatusService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class LeadStatusServiceImpl implements LeadStatusService {
    private final LeadStatusRepository leadStatusRepository;
    private final Converter converter;
    @Override
    public boolean createLeadStatus(LeadStatusDTO leadStatusDTO) {
        try {
            LeadStatus leadStatus = converter.LeadStatusDTOToEntity(leadStatusDTO);
            leadStatusRepository.save(leadStatus);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public PageResponse<?> getAllLeadStatus(int page, int size) {
        try {
            List<Sort.Order> sorts = new ArrayList<>();
            sorts.add(new Sort.Order(Sort.Direction.ASC, "leadStatusIndex"));
            Pageable pageable = PageRequest.of(page, size, Sort.by(sorts));
            Page<LeadStatus> leadStatusList = leadStatusRepository.findAll(pageable);
            return converter.convertToPageResponseLeadStatus(leadStatusList, pageable);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    public boolean updateLeadStatus(LeadStatusDTO leadStatusDTO) {
        try {
            LeadStatus leadStatus = leadStatusRepository.findById(leadStatusDTO.getLeadStatusId()).orElse(null);
            if (leadStatus == null) {
                return false;
            }
            if (leadStatusDTO.getLeadStatusIndex() != null)
                leadStatus.setLeadStatusIndex(leadStatusDTO.getLeadStatusIndex());
            if (leadStatusDTO.getLeadStatusName() != null && !leadStatusDTO.getLeadStatusName().isEmpty())
                leadStatus.setLeadStatusName(leadStatusDTO.getLeadStatusName());
            leadStatus.setLeadStatusName(leadStatusDTO.getLeadStatusName());
            leadStatus.setLeadStatusIndex(leadStatusDTO.getLeadStatusIndex());
            leadStatusRepository.save(leadStatus);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public boolean deleteLeadStatus() {
        return false;
    }
}
