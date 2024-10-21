package fpt.capstone.SalesInnovate.iLead.controller;

import fpt.capstone.SalesInnovate.iLead.dto.request.LeadsUserDTO;
import fpt.capstone.SalesInnovate.iLead.dto.response.ResponseData;
import fpt.capstone.SalesInnovate.iLead.dto.response.ResponseError;
import fpt.capstone.SalesInnovate.iLead.service.FilterService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/filter")
public class FilterController {
    private final FilterService filterService ;

    @PostMapping("/save-filter")
    public ResponseData<?> saveFilter(@RequestParam String filterName,@RequestParam String search,@RequestParam Long type) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            filterService.saveFilter(userId,filterName, search,type);
            return new ResponseData<>(HttpStatus.CREATED.value(), "Save Filter success", 1);
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "Save Filter fail");
        }
    }

    @GetMapping("/filter-list")
    public ResponseData<?> getListFilter(@RequestParam Long filterType) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return new ResponseData<>(1, HttpStatus.OK.value(), filterService.getListFilter(userId,filterType));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/get")
    public ResponseData<?> getFilter(@RequestParam Long filterId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return new ResponseData<>(1, HttpStatus.OK.value(), filterService.getFilter(filterId));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PostMapping("/assign/{filterId}")
    public ResponseData<?> assignFilter(@PathVariable Long filterId,@RequestBody List<String> userIds) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return filterService.assignFilter(userId,filterId,userIds) ?
                    new ResponseData<>(1, HttpStatus.OK.value(), "assign users success") :
                    new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "assign users fail");
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @DeleteMapping("/delete/{filterStoreId}")
    public ResponseData<?> deleteFilter(@PathVariable Long filterStoreId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return filterService.deleteFilter(userId,filterStoreId) ?
                    new ResponseData<>(1, HttpStatus.OK.value(), "delete filter success") :
                    new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "delete filter fail");
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }
}
