package com.example.stock.web;

import com.example.stock.Service.CompanyService;
import com.example.stock.model.Company;
import com.example.stock.model.constants.CacheKey;
import com.example.stock.persist.entity.CompanyEntity;
import lombok.AllArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

@RestController
@RequestMapping("/company")
@AllArgsConstructor
public class CompanyController {
    private final CompanyService companyService;
    private final CacheManager redisCacheManager;

    @GetMapping("/autocomplete")
    public ResponseEntity<?> autoComplete(@RequestParam String keyword){
        List<String> result = this.companyService.getCompanyNamesByKeyword(keyword);
        return ResponseEntity.ok(result);
    }

//    @GetMapping("/autocomplete")
//    public ResponseEntity<?> autoComplete(@RequestParam String keyword){
//        List<String> result = this.companyService.autocomplete(keyword);
//        return ResponseEntity.ok(result);
//    }

    @GetMapping
    @PreAuthorize("hasRole('READ')") //READ권한을 가진 유저만 사용가능
    public ResponseEntity<?> searchCompany(final Pageable pageable){
        Page<CompanyEntity> companies = companyService.getAllCompany(pageable);

        return ResponseEntity.ok(companies);
    }

    @PostMapping
    @PreAuthorize("hasRole('WRITE')") //WRITE권한을 가진 유저만 사용가능
    public ResponseEntity<?> addCompany(@RequestBody Company request){
        String ticker = request.getTicker().trim();
        if(ObjectUtils.isEmpty(ticker)){
            throw new RuntimeException("ticker is empty");
        }

        Company company = this.companyService.save(ticker);
        this.companyService.addAutocompleteKeyword(company.getName());
        return ResponseEntity.ok(company);
    }

    @DeleteMapping("/{ticker}")
    @PreAuthorize("hasRole('WRITE')")
    public ResponseEntity<?> deleteCompany(@PathVariable String ticker){
        String companyName = this.companyService.deleteCompany(ticker);
        //캐시에서도 데이터를 지워줘야한다
        this.clearFinanceCache(companyName);
        return ResponseEntity.ok(companyName);
    }

    public void clearFinanceCache(String companyName){
        this.redisCacheManager.getCache(CacheKey.KEY_FINANCE).evict(companyName);
    }
}
