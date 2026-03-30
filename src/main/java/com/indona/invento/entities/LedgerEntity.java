package com.indona.invento.entities;


import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity; 
import jakarta.persistence.GeneratedValue; 
import jakarta.persistence.GenerationType; 
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor; 
import lombok.Data;
import lombok.NoArgsConstructor; 
 
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "LEDGER")
public class LedgerEntity {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="ledger_id")
    private Long id;
	
	@Column(name="entity_id")
    private Long dealerId;
	
	@Column(name="transaction_type")
    private String type;
	
	@Column(name="expense_name")
    private String expenseName;
	
	@Column(name="expense_user")
    private String expenseUser;
	
	@Column(name="ref_no")
    private String refNo;
	
	@Column(name="receipt_no")
    private String receiptNo;
	
	@Column(name="credit")
    private String credit;
	
	@Column(name="debit")
    private String debit;
	
	@Column(name="file_url")
    private String file;
	
	@Column(name="created_by")
    private String userName;
	
	@Column(name="store")
    private Long store;
	
	@Column(name="datetime")
    private Date dateTime;
	 
	@PrePersist
	public void addTimestamp() {
		dateTime = new Date();
	}
	
}