package fpt.capstone.SalesInnovate.iLead.repository.filter;

import fpt.capstone.SalesInnovate.iLead.model.filter.FilterStore;
import fpt.capstone.SalesInnovate.iLead.model.filter.FilterStoreUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FilterStoreUserRepository extends JpaRepository<FilterStoreUser,Long>, JpaSpecificationExecutor<FilterStoreUser> {
}
