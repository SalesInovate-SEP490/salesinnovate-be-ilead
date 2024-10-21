package fpt.capstone.SalesInnovate.iLead.repository.filter;

import fpt.capstone.SalesInnovate.iLead.model.filter.FilterStore;
import fpt.capstone.SalesInnovate.iLead.model.filter.FilterType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FilterTypeRepository extends JpaRepository<FilterType,Long>, JpaSpecificationExecutor<FilterType> {
}
