package fpt.capstone.SalesInnovate.iLead.service.impl;

import fpt.capstone.SalesInnovate.iLead.dto.Converter;
import fpt.capstone.SalesInnovate.iLead.dto.request.*;
import fpt.capstone.SalesInnovate.iLead.dto.response.LeadResponse;
import fpt.capstone.SalesInnovate.iLead.dto.response.PageResponse;
import fpt.capstone.SalesInnovate.iLead.dto.response.UserResponse;
import fpt.capstone.SalesInnovate.iLead.model.*;
import fpt.capstone.SalesInnovate.iLead.repository.*;
import fpt.capstone.SalesInnovate.iLead.repository.specification.LeadSpecificationsBuilder;
import fpt.capstone.SalesInnovate.iLead.service.ExcelUploadService;
import fpt.capstone.SalesInnovate.iLead.service.LeadsClientService;
import fpt.capstone.SalesInnovate.iLead.service.LeadsService;
import fpt.capstone.proto.user.UserDtoProto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import lombok.experimental.Helper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static fpt.capstone.SalesInnovate.iLead.util.AppConst.SEARCH_SPEC_OPERATOR;

@Slf4j
@Service
@AllArgsConstructor
public class LeadServiceImpl implements LeadsService {
    private final LeadsRepository leadsRepository;
    private final Converter leadConverter;
    private final LeadStatusRepository leadStatusRepository;
    private final IndustryRepository industryRepository;
    private final LeadSourceRepository leadSourceRepository;
    private final SearchRepository searchRepository;
    private final LeadRatingRepository leadRatingRepository;
    private final LeadSalutionRepository leadSalutionRepository;
    private final AddressInformationRepository addressInformationRepository;
    private final ExcelUploadService excelUploadService;
    private final LeadsClientService leadsClientService;
    private final LeadsUserRepository leadsUserRepository;

    public void saveLeadsToDatabase(MultipartFile file, String userId) {
        if (excelUploadService.isValidExcelFile(file)) {
            try {
                List<Leads> leads =
                        excelUploadService.getLeadDataFromExcel(file.getInputStream(), userId);
                this.leadsRepository.saveAll(leads);
                //Thêm user vào lead
                for(Leads lead : leads){
                    Specification<Leads> spec = new Specification<Leads>() {
                        @Override
                        public Predicate toPredicate(Root<Leads> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                            Join<Leads, Users> join = root.join("users", JoinType.INNER);
                            List<Predicate> predicates = new ArrayList<>();
                            predicates.add(criteriaBuilder.equal(join.get("userId"), userId));
                            predicates.add(criteriaBuilder.equal(root.get("leadId"), lead.getLeadId()));
                            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                        }
                    };
                    boolean existed = leadsRepository.exists(spec);
                    if(existed) continue;
                    LeadsUser leadsUser = LeadsUser.builder()
                            .userId(userId)
                            .leadId(lead.getLeadId())
                            .build();
                    leadsUserRepository.save(leadsUser);
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("The file is not a valid excel file");
            }
        }
    }
//    @Override
//    public List<LeadResponse> getAllLeads(){
//        List<Leads> leads = this.leadsRepository.findAll();
//        return leads.stream().map(lead -> this.leadConverter.entityToLeadResponse(lead)).collect(Collectors.toList());
//    }

    @Override
    @Transactional
    public boolean patchLead(String userId,LeadDTO leadDTOOld, long id) {

        //Check xem user co quyen sua hay khong
        Specification<LeadsUser> spec = new Specification<LeadsUser>() {
            @Override
            public Predicate toPredicate(Root<LeadsUser> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("leadId"), id));
                predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
        boolean exists = leadsUserRepository.exists(spec);
        if(!exists) return false ;

        Map<String, Object> patchMap = getPatchData(leadDTOOld);
        if (patchMap.isEmpty()) {
            return true;
        }

        Leads lead = leadsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find Leads with id: " + id));

        if (lead != null) {
            for (Map.Entry<String, Object> entry : patchMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                Field fieldDTO = ReflectionUtils.findField(LeadDTO.class, key);

                if (fieldDTO == null) {
                    continue;
                }

                fieldDTO.setAccessible(true);
                Class<?> type = fieldDTO.getType();

                try {
                    if (type == long.class && value instanceof String) {
                        value = Long.parseLong((String) value);
                    } else if (type == Long.class && value instanceof String) {
                        value = Long.valueOf((String) value);
                    }
                } catch (NumberFormatException e) {
                    return false;
                }

                switch (key) {
                    case "leadSourceId":
                        lead.setLeadSource(leadSourceRepository.findById((Long) value).orElse(null));
                        break;
                    case "industryId":
                        lead.setIndustry(industryRepository.findById((Long) value).orElse(null));
                        break;
                    case "leadStatusID":
                        lead.setLeadStatus(leadStatusRepository.findById((Long) value).orElse(null));
                        break;
                    case "leadRatingId":
                        lead.setLeadRating(leadRatingRepository.findById((Long) value).orElse(null));
                        break;
                    case "addressInformation":
                        AddressInformationDTO dto = (AddressInformationDTO) value;
                        if(!Objects.equals(dto.getStreet(),lead.getAddressInformation().getStreet()))
                            lead.getAddressInformation().setStreet(dto.getStreet());
                        if(!Objects.equals(dto.getCity(),lead.getAddressInformation().getCity()))
                            lead.getAddressInformation().setCity(dto.getCity());
                        if(!Objects.equals(dto.getProvince(),lead.getAddressInformation().getProvince()))
                            lead.getAddressInformation().setProvince(dto.getProvince());
                        if(!Objects.equals(dto.getPostalCode(),lead.getAddressInformation().getPostalCode()))
                            lead.getAddressInformation().setPostalCode(dto.getPostalCode());
                        if(!Objects.equals(dto.getCountry(),lead.getAddressInformation().getCountry()))
                            lead.getAddressInformation().setCountry(dto.getCountry());
                        break;
                    case "leadSalutionId":
                        lead.setLeadSalution(leadSalutionRepository.findById((Long) value).orElse(null));
                        break;
                    default:
                        if (fieldDTO.getType().isAssignableFrom(value.getClass())) {
                            Field field = ReflectionUtils.findField(Leads.class, fieldDTO.getName());
                            assert field != null;
                            field.setAccessible(true);
                            ReflectionUtils.setField(field, lead, value);
                        } else {
                            return false;
                        }
                }
            }
            lead.setEditDate(LocalDateTime.now());
            lead.setEditBy(userId);
            leadsRepository.save(lead);

            //Thêm xử lý notification cho patch lead
            List<String> listUser = new ArrayList<>();
            Specification<LeadsUser> spec1 = new Specification<LeadsUser>() {
                @Override
                public Predicate toPredicate(Root<LeadsUser> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("leadId"), id));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<LeadsUser> leadUsers = leadsUserRepository.findAll(spec1);
            for(LeadsUser user : leadUsers){
                listUser.add(user.getUserId());
            }
            leadsClientService.createNotification(userId,"The lead you were assigned has been updated."
                    ,id,1L,listUser);

            return true;
        }

        return false;
    }



    @Override
    @Transactional
    public boolean deleteLeadById(String userId,Long id) {
        Optional<Leads> leads = this.leadsRepository.findById(id);
        if (leads.isPresent()) {
            Leads deleteLead = leads.get();
            Specification<LeadsUser> spec = new Specification<LeadsUser>() {
                @Override
                public Predicate toPredicate(Root<LeadsUser> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("leadId"), id));
                    predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            boolean exists = leadsUserRepository.exists(spec);
            if(!exists) throw new RuntimeException("Can not delete the lead that you did not been assigned");
            deleteLead.setIsDelete(1);
            leadsRepository.save(deleteLead);
            boolean check = leadsClientService.deleteRelationLeadCampaign(id);
            if (!check) throw new RuntimeException("Can not delete relation between lead ang campaign");
            //Xóa quan hệ giữa lead và user
            List<LeadsUser> users = leadsUserRepository.findAll(spec);
            leadsUserRepository.deleteAll(users);
            return true ;
        } else {
            log.info("There are not exists a lead containing that ID: " + id);
            return false ;
        }
    }

    @Override
    public PageResponse<?> getAllLeadsWithSortByDefault(String userId,int pageNo, int pageSize) {

        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(new Sort.Order(Sort.Direction.DESC, "createDate"));

        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(sorts));

        Specification<Leads> spec = new Specification<Leads>() {
            @Override
            public Predicate toPredicate(Root<Leads> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                Join<Leads, Users> join = root.join("users", JoinType.INNER);
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(join.get("userId"), userId));
                predicates.add(criteriaBuilder.notEqual(root.get("isDelete"), 1));
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };

        Page<Leads> leads = leadsRepository.findAll(spec, pageable);
        return leadConverter.convertToPageResponse(leads, pageable);
    }

    @Override
    public List<IndustryDTO> getAllIndustry() {
        List<Industry> industries = industryRepository.findAll();

        return industries.stream().map(leadConverter::entityToIndustryDTO).toList();
    }

    @Override
    public List<LeadSourceDTO> getAllLeadSource() {
        List<LeadSource> leadSources = leadSourceRepository.findAll();

        return leadSources.stream().map(leadConverter::entityToLeadSourceDTO).toList();
    }

    @Override
    public List<LeadStatusDTO> geLeadStatus() {
        List<LeadStatus> leadStatuses = leadStatusRepository.findAll(Sort.by(Sort.Order.asc("leadStatusIndex")));
        return leadStatuses.stream().map(leadConverter::entityToLeadStatusDTO).toList();
    }

    @Override
    public PageResponse<?> filterLeadsWithSpecifications(String userId,Pageable pageable, String[] search) {
        LeadSpecificationsBuilder builder = new LeadSpecificationsBuilder();

        if (search != null) {
            Pattern pattern = Pattern.compile(SEARCH_SPEC_OPERATOR);
            for (String l : search) {
                Matcher matcher = pattern.matcher(l);
                if (matcher.find()) {
                    builder.with(matcher.group(1), matcher.group(2),matcher.group(3));
                }
            }

            Page<Leads> leadPage = searchRepository.searchUserByCriteriaWithJoin(userId,builder.params, pageable);
            return leadConverter.convertToPageResponse(leadPage, pageable);
        }
        return getAllLeadsWithSortByDefault(userId,pageable.getPageNumber(),pageable.getPageSize());
    }

    @Override
    public List<LeadResponse> getLeadsByStatus(long id) {
        List<Leads> leadsList = leadsRepository.findByStatus(id);
        return leadsList.stream().map(leadConverter::entityToLeadResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean patchListLead(String userId,Long[] id, LeadDTO leadDTO) {
        if (id != null){
            List<Leads> leadsList = new ArrayList<>();
            try {
                for (long i : id){
                    Leads lead = leadsRepository.findById(i).orElse(null);
                    if(lead != null) leadsList.add(lead);
                }
                boolean checked ;
                for (Leads l : leadsList) {
                    checked =patchLead(userId,leadDTO, l.getLeadId());
                    if(!checked) {
                        return false ;
                    }
                }
                return true ;
            }catch (Exception e){
                return false ;
            }
        }
        return false ;
    }

    @Override
    public AddressInformationDTO getAddressInformationById(long id) {
        AddressInformation addressInformation = addressInformationRepository.findById(id).orElse(null);
        if(addressInformation != null){
            return AddressInformationDTO.builder()
                    .addressInformationId(addressInformation.getAddressInformationId())
                    .street(addressInformation.getStreet())
                    .city(addressInformation.getCity())
                    .province(addressInformation.getProvince())
                    .postalCode(addressInformation.getPostalCode())
                    .country(addressInformation.getCountry())
                    .build();
        }
        return null;
    }

    @Override
    public LeadSalutionDTO getLeadSalutionById(long id) {
        LeadSalution leadSalution = leadSalutionRepository.findById(id).orElse(null);
        if(leadSalution != null){
            return LeadSalutionDTO.builder()
                    .leadSalutionId(leadSalution.getLeadSalutionId())
                    .leadSalutionName(leadSalution.getLeadSalutionName())
                    .build();
        }
        return null;
    }

    @Override
    public List<LeadSalutionDTO> geLeadSalution() {
        List<LeadSalution> leadSalutions = leadSalutionRepository.findAll();

        return leadSalutions.stream().map(leadConverter::entityToLeadSalutionDTO).toList();
    }

    @Override
    public ByteArrayInputStream getExportFileData(String userId) throws IOException {
        List<Leads> leads = leadsRepository.findByUserId(userId);
        ByteArrayInputStream byteArrayInputStream = excelUploadService.dataToExcel(leads);
        return byteArrayInputStream;
    }

    @Override
    public boolean addUserToLead(String userId,Long leadId,List<LeadsUserDTO> leadsUserDTOS) {
        try {
            Leads leads = leadsRepository.findById(leadId).orElse(null);
            if (leads==null) throw new RuntimeException("Cannot find lead");
//            if (!Objects.equals(userId, leads.getCreatedBy())) throw new RuntimeException("Cannot add user to lead");
            checkRelationLeadAndUser(userId,leadId);

            List<String> listUser = new ArrayList<>();
            for (LeadsUserDTO dto : leadsUserDTOS){
                Specification<Leads> spec = new Specification<Leads>() {
                    @Override
                    public Predicate toPredicate(Root<Leads> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        Join<Leads, Users> join = root.join("users", JoinType.INNER);
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.equal(join.get("userId"), dto.getUserId()));
                        predicates.add(criteriaBuilder.equal(root.get("leadId"), leadId));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                    }
                };
                boolean existed = leadsRepository.exists(spec);
                if(existed) continue;
                LeadsUser leadsUser = LeadsUser.builder()
                        .userId(dto.getUserId())
                        .leadId(leadId)
                        .build();
                leadsUserRepository.save(leadsUser);
                listUser.add(dto.getUserId());
            }

            leadsClientService.createNotification(userId,"You have been assigned to the new lead."
                    ,leadId,1L,listUser);
            return true;
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<UserResponse> getListUserInLead(Long leadId) {
        try{
            List<UserResponse> responses = new ArrayList<>();
            Optional<Leads> leads = this.leadsRepository.findById(leadId);
            if(leads.isEmpty()) throw new RuntimeException("Can not find leads");
            Specification<LeadsUser> spec = new Specification<LeadsUser>() {
                @Override
                public Predicate toPredicate(Root<LeadsUser> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("leadId"), leadId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<LeadsUser> users = leadsUserRepository.findAll(spec);
            for(LeadsUser user:users){
                UserDtoProto proto = leadsClientService.getUser(user.getUserId());
                if(proto.getUserId().isEmpty()) continue;
                UserResponse userResponse = UserResponse.builder()
                        .userId(proto.getUserId())
                        .userName(proto.getUserName())
                        .firstName(proto.getFirstName())
                        .lastName(proto.getLastName())
                        .email(proto.getEmail())
                        .build();
                responses.add(userResponse);
            }
            return responses;
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean addUserToListLead(String userId, AssignUserDTO assignUserDTO) {
        try {
            for(Long leadId : assignUserDTO.getLeadIds()){
                Leads leads = leadsRepository.findById(leadId).orElse(null);
                if (leads==null) throw new RuntimeException("Cannot find lead");
//            if (!Objects.equals(userId, leads.getCreatedBy())) throw new RuntimeException("Cannot add user to lead");
                checkRelationLeadAndUser(userId,leadId);

                List<String> listUser = new ArrayList<>();
                for (LeadsUserDTO dto : assignUserDTO.getLeadsUserDTOS()){
                    Specification<Leads> spec = new Specification<Leads>() {
                        @Override
                        public Predicate toPredicate(Root<Leads> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                            Join<Leads, Users> join = root.join("users", JoinType.INNER);
                            List<Predicate> predicates = new ArrayList<>();
                            predicates.add(criteriaBuilder.equal(join.get("userId"), dto.getUserId()));
                            predicates.add(criteriaBuilder.equal(root.get("leadId"), leadId));
                            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                        }
                    };
                    boolean existed = leadsRepository.exists(spec);
                    if(existed) continue;
                    LeadsUser leadsUser = LeadsUser.builder()
                            .userId(dto.getUserId())
                            .leadId(leadId)
                            .build();
                    leadsUserRepository.save(leadsUser);
                    listUser.add(dto.getUserId());
                }

                leadsClientService.createNotification(userId,"You have been assigned to the new lead."
                        ,leadId,1L,listUser);
            }
            return true;
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public long createLead(String userId,LeadDTO leadDTO) {
        try {
            Leads lead = leadConverter.LeadDtoToLeadEntity(leadDTO);
            lead.setCreateDate(LocalDateTime.now());
            lead.setEditDate(LocalDateTime.now());
            lead.setCreatedBy(userId);
            lead.setEditBy(userId);
            lead.setUserId(userId);
            lead.setIsDelete(0);
            if(lead.getAddressInformation()==null||lead.getAddressInformation().getAddressInformationId()==null){
                AddressInformation addressInformation = new AddressInformation();
                addressInformationRepository.save(addressInformation);
                lead.setAddressInformation(addressInformation);
            }
            leadsRepository.save(lead);
            //Thêm quan hệ cho user và lead
            LeadsUser leadsUser = LeadsUser.builder()
                    .leadId(lead.getLeadId())
                    .userId(userId)
                    .build();
            leadsUserRepository.save(leadsUser);
            return lead.getLeadId();
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public LeadResponse getLeadDetail(Long leadId) {
        Leads lead = leadsRepository.findById(leadId).orElseThrow(() -> new RuntimeException("User not found"));
        if(lead.getIsDelete()==1) return null ;
        return leadConverter.entityToLeadResponse(lead);
    }

    @Override
    public LeadResponse getLeadDetailWithUser(String userId, Long leadId) {
        try{
            Specification<LeadsUser> spec = new Specification<LeadsUser>() {
                @Override
                public Predicate toPredicate(Root<LeadsUser> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("leadId"), leadId));
                    predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            boolean exists = leadsUserRepository.exists(spec);
            if(!exists) throw new RuntimeException("Cannot get detail lead");
            Leads lead = leadsRepository.findById(leadId).orElseThrow(() -> new RuntimeException("User not found"));
            if(lead.getIsDelete()==1) return null ;
            return leadConverter.entityToLeadResponse(lead);
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }


    private Map<String, Object> getPatchData(Object obj) {
        Class<?> objClass = obj.getClass();
        Field[] fields = objClass.getDeclaredFields();
        Map<String, Object> patchMap = new HashMap<>();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (value != null) {
                    patchMap.put(field.getName(), value);
                }
            } catch (IllegalAccessException e) {
                log.info(e.getMessage());
            }
        }
        return patchMap;
    }

    private void checkRelationLeadAndUser (String userId,Long leadId){
        Specification<LeadsUser> spec = new Specification<LeadsUser>() {
            @Override
            public Predicate toPredicate(Root<LeadsUser> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("leadId"), leadId));
                predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
        boolean exists = leadsUserRepository.exists(spec);
        if(!exists) throw new RuntimeException("Did not been assigned to this lead ");
    }

}
