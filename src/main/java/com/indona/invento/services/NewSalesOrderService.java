package com.indona.invento.services;

import com.indona.invento.dto.NewSalesOrderDTO;
import com.indona.invento.entities.NewSalesOrder;

public interface NewSalesOrderService {
    NewSalesOrder createNewSalesOrder(NewSalesOrderDTO dto);

    NewSalesOrder updateSalesOrder(String soNumber, NewSalesOrderDTO dto);

    NewSalesOrder getSalesOrderBySoNumber(String soNumber);


}
