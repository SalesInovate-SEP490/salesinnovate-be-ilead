package fpt.capstone.SalesInnovate.iLead.repository.filter;

import fpt.capstone.SalesInnovate.iLead.model.Leads;
import fpt.capstone.SalesInnovate.iLead.model.filter.FilterStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FilterStoreRepository extends JpaRepository<FilterStore,Long>, JpaSpecificationExecutor<FilterStore> {
}
