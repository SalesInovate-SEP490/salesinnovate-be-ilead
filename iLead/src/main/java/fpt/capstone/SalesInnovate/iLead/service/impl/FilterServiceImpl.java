package fpt.capstone.SalesInnovate.iLead.service.impl;

import fpt.capstone.SalesInnovate.iLead.model.LeadsUser;
import fpt.capstone.SalesInnovate.iLead.model.filter.FilterStore;
import fpt.capstone.SalesInnovate.iLead.model.filter.FilterStoreUser;
import fpt.capstone.SalesInnovate.iLead.model.filter.FilterType;
import fpt.capstone.SalesInnovate.iLead.repository.filter.FilterStoreRepository;
import fpt.capstone.SalesInnovate.iLead.repository.filter.FilterStoreUserRepository;
import fpt.capstone.SalesInnovate.iLead.repository.filter.FilterTypeRepository;
import fpt.capstone.SalesInnovate.iLead.service.FilterService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@AllArgsConstructor
public class FilterServiceImpl implements FilterService {
    private final FilterStoreRepository filterStoreRepository;
    private final FilterStoreUserRepository filterStoreUserRepository;
    private final FilterTypeRepository filterTypeRepository;

    @Override
    @Transactional
    public boolean saveFilter(String userId, String filterName, String search,Long type) {
        try{
            FilterStore filterStore = FilterStore.builder()
                    .filterName(filterName)
                    .filterSearch(search)
                    .filterType(filterTypeRepository.findById(type).orElse(null))
                    .build();
            filterStoreRepository.save(filterStore);

            FilterStoreUser storeUser = FilterStoreUser.builder()
                    .filterStoreId(filterStore.getFilterStoreId())
                    .userId(userId)
                    .build();
            filterStoreUserRepository.save(storeUser);
            return true;
        }catch (Exception e){
            throw  new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<FilterStore> getListFilter(String userId,Long filterType) {
        try{
            Specification<FilterStoreUser> spec = new Specification<FilterStoreUser>() {
                @Override
                public Predicate toPredicate(Root<FilterStoreUser> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<FilterStoreUser> exists = filterStoreUserRepository.findAll(spec);
            List<FilterStore> listFilter = new ArrayList<>();
            for(FilterStoreUser storeUser: exists){
                FilterStore filterStore = filterStoreRepository.findById(storeUser.getFilterStoreId()).orElse(null);
                if(filterStore!=null&& Objects.equals(filterStore.getFilterType().getFilterTypeId(), filterType)){
                    listFilter.add(filterStore);
                }
            }
            return listFilter;
        }catch (Exception e){
            throw  new RuntimeException(e.getMessage());
        }
    }

    @Override
    public FilterStore getFilter(Long filterId) {
        try{
            return filterStoreRepository.findById(filterId).orElse(null);
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean assignFilter(String userId,Long filterStoreId,List<String> userIds) {
        try{
            //Check xem user co quyen  hay khong
            Specification<FilterStoreUser> spec = new Specification<FilterStoreUser>() {
                @Override
                public Predicate toPredicate(Root<FilterStoreUser> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("filterStoreId"), filterStoreId));
                    predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            boolean exists = filterStoreUserRepository.exists(spec);
            if(!exists) throw  new RuntimeException("You are not assigned, so do not assign to others.");
            for (String i : userIds){
                FilterStoreUser storeUser = FilterStoreUser.builder()
                        .filterStoreId(filterStoreId)
                        .userId(i)
                        .build();
                filterStoreUserRepository.save(storeUser);
            }
            return true;
        }catch (Exception e){
            throw  new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean deleteFilter(String userId,Long filterStoreId) {
        try{
            //Check xem user co quyen  hay khong
            Specification<FilterStoreUser> spec = new Specification<FilterStoreUser>() {
                @Override
                public Predicate toPredicate(Root<FilterStoreUser> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("filterStoreId"), filterStoreId));
                    predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<FilterStoreUser> exists = filterStoreUserRepository.findAll(spec);
            if(exists.isEmpty()) throw  new RuntimeException("You are not assigned, so do not delete filter.");

            Specification<FilterStoreUser> spec2 = new Specification<FilterStoreUser>() {
                @Override
                public Predicate toPredicate(Root<FilterStoreUser> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("filterStoreId"), filterStoreId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<FilterStoreUser> filterStoreUsers = filterStoreUserRepository.findAll(spec2);
            if(filterStoreUsers.size()>1){
                filterStoreUserRepository.delete(exists.get(0));
            }else if(filterStoreUsers.size()==1){
                filterStoreUserRepository.delete(exists.get(0));
                filterStoreRepository.deleteById(filterStoreId);
            }
            return true;
        }catch (Exception e){
            throw  new RuntimeException(e.getMessage());
        }
    }
}
