package fpt.capstone.SalesInnovate.iLead.model.filter;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="filter_type")
public class FilterType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "filter_type_id")
    private Long filterTypeId ;
    @Column(name = "filter_type_name")
    private String filterTypeName;
}
