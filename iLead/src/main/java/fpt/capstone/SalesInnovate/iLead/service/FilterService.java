package fpt.capstone.SalesInnovate.iLead.service;

import fpt.capstone.SalesInnovate.iLead.model.filter.FilterStore;

import java.util.List;

public interface FilterService {
    boolean saveFilter (String userId,String filterName,String search,Long type);
    List<FilterStore> getListFilter (String userId,Long filterType);
    FilterStore getFilter (Long filterId);
    boolean assignFilter (String userId,Long filterId,List<String> userIds);
    boolean deleteFilter (String userId,Long filterStoreId);
}
