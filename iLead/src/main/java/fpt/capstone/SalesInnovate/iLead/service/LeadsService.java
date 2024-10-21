package fpt.capstone.SalesInnovate.iLead.service;


import fpt.capstone.SalesInnovate.iLead.dto.request.*;
import fpt.capstone.SalesInnovate.iLead.dto.response.LeadResponse;
import fpt.capstone.SalesInnovate.iLead.dto.response.PageResponse;
import fpt.capstone.SalesInnovate.iLead.dto.response.UserResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public interface LeadsService {
    void saveLeadsToDatabase(MultipartFile file, String userId);

    long createLead(String userId,LeadDTO leadDTO);

    boolean patchLead(String userId,LeadDTO leadDTO, long id);

    boolean deleteLeadById(String userId,Long id);

    LeadResponse getLeadDetail(Long leadId);

    LeadResponse getLeadDetailWithUser(String userId,Long leadId);

    PageResponse<?> getAllLeadsWithSortByDefault(String userId, int pageNo, int pageSize);

    List<IndustryDTO> getAllIndustry();

    List<LeadSourceDTO> getAllLeadSource();

    List<LeadStatusDTO> geLeadStatus();

    PageResponse<?> filterLeadsWithSpecifications(String userId, Pageable pageable,  String[] search);

    List<LeadResponse> getLeadsByStatus(long id);

    boolean patchListLead(String userId, Long[] id, LeadDTO leadDTO);

    AddressInformationDTO getAddressInformationById(long id);

    LeadSalutionDTO getLeadSalutionById(long id);

    List<LeadSalutionDTO> geLeadSalution();

    ByteArrayInputStream getExportFileData(String userId) throws IOException;

    boolean addUserToLead(String userId,Long leadId,List<LeadsUserDTO> leadsUserDTOS);

    List<UserResponse> getListUserInLead(Long leadId);

    boolean addUserToListLead(String userId,AssignUserDTO assignUserDTO);

}
