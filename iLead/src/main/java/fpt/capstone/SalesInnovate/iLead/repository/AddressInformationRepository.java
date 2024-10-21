package fpt.capstone.SalesInnovate.iLead.repository;

import fpt.capstone.SalesInnovate.iLead.model.AddressInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressInformationRepository extends JpaRepository<AddressInformation,Long> {
}
