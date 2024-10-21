package fpt.capstone.SalesInnovate.iLead.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class PageResponse<T> implements Serializable {
    private int page;
    private int size;
    private int total;
    private long totalItem;
    private T items;
}

