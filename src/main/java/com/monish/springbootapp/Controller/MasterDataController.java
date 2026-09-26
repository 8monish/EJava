package com.monish.springbootapp.Controller;

import com.monish.springbootapp.model.*;
import com.monish.springbootapp.repository.*;
import com.monish.springbootapp.service.AccountingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MasterDataController {

    private final ContactRepository contactRepository;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;
    private final JournalRepository journalRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final AnalyticAccountRepository analyticAccountRepository;
    private final AccountingService accountingService;

    public MasterDataController(ContactRepository contactRepository,
                                ProductRepository productRepository,
                                AccountRepository accountRepository,
                                JournalRepository journalRepository,
                                JournalEntryRepository journalEntryRepository,
                                AnalyticAccountRepository analyticAccountRepository,
                                AccountingService accountingService) {
        this.contactRepository = contactRepository;
        this.productRepository = productRepository;
        this.accountRepository = accountRepository;
        this.journalRepository = journalRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.analyticAccountRepository = analyticAccountRepository;
        this.accountingService = accountingService;
    }

    // 1. Contact Master
    @GetMapping("/contacts")
    public List<Contact> getContacts(@RequestParam(required = false) String type) {
        if (type != null && !type.isBlank()) {
            return contactRepository.findByContactType(type.toUpperCase());
        }
        return contactRepository.findAll();
    }

    @PostMapping("/contacts")
    public ResponseEntity<Contact> createContact(@RequestBody Contact contact) {
        Contact saved = contactRepository.save(contact);
        return ResponseEntity.ok(saved);
    }

    // 2. Product Master
    @GetMapping("/products")
    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product saved = productRepository.save(product);
        return ResponseEntity.ok(saved);
    }

    // 3. Chart of Accounts Master
    @GetMapping("/accounts")
    public List<Account> getAccounts(@RequestParam(required = false) String type) {
        if (type != null && !type.isBlank()) {
            return accountRepository.findByAccountType(type.toUpperCase());
        }
        return accountRepository.findAll();
    }

    @PostMapping("/accounts")
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        Account saved = accountRepository.save(account);
        return ResponseEntity.ok(saved);
    }

    // 4. Journals
    @GetMapping("/journals")
    public List<Journal> getJournals() {
        return journalRepository.findAll();
    }

    // 5. Journal Entries
    @GetMapping("/journal-entries")
    public List<JournalEntry> getJournalEntries() {
        return journalEntryRepository.findAllByOrderByEntryDateDescIdDesc();
    }

    @PostMapping("/journal-entries")
    public ResponseEntity<JournalEntry> createJournalEntry(@RequestBody JournalEntry entry) {
        JournalEntry posted = accountingService.postJournalEntry(entry);
        return ResponseEntity.ok(posted);
    }

    // 6. Analytic Accounts (Lighting Sectors)
    @GetMapping("/analytic-accounts")
    public List<AnalyticAccount> getAnalyticAccounts() {
        return analyticAccountRepository.findAll();
    }

    @PostMapping("/analytic-accounts")
    public ResponseEntity<AnalyticAccount> createAnalyticAccount(@RequestBody AnalyticAccount analyticAccount) {
        AnalyticAccount saved = analyticAccountRepository.save(analyticAccount);
        return ResponseEntity.ok(saved);
    }
}
