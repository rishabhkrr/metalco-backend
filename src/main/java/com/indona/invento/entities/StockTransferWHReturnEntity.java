package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "stock_transfer_wh_return")
public class StockTransferWHReturnEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mrNumber;

    private String lineNumber;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ReturnEntryEntity> returnEntries = new ArrayList<>();

    public void addReturnEntry(ReturnEntryEntity entry) {
        entry.setParent(this);
        this.returnEntries.add(entry);
    }

    public void removeReturnEntry(ReturnEntryEntity entry) {
        entry.setParent(null);
        this.returnEntries.remove(entry);
    }

}
