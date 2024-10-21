package fpt.capstone.SalesInnovate.iLead.model.filter;

import fpt.capstone.SalesInnovate.iLead.model.LeadSalution;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="filter_store")
public class FilterStore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "filter_store_id")
    private Long filterStoreId ;
    @Column(name = "filter_name")
    private String filterName;
    @Column(name = "filter_search")
    private String filterSearch;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE,
            CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "filter_type_id")
    private FilterType filterType;
}
