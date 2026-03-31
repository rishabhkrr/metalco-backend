package com.indona.invento.services;

import com.indona.invento.dto.NewCustomerDto;
import com.indona.invento.entities.NewCustomerDetails;
import com.indona.invento.entities.NewSalesOrder;

import java.util.List;

public interface NewCustomerService {
    NewCustomerDetails createNewCustomer(NewCustomerDto dto);
    List<NewCustomerDetails> getAllCustomers();
    NewCustomerDetails getCustomerById(Long id);
    NewCustomerDetails deleteCustomer(Long id);
    NewCustomerDetails updateCustomer(Long id, NewCustomerDto dto);
    void deleteAllCustomers();
}
