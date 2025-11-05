package com.jorgeandreu.orders.application.exception;

import com.jorgeandreu.orders.domain.model.SearchCriteria;

public class OrderSearchBadRequest extends RuntimeException{
    public OrderSearchBadRequest(SearchCriteria command) { super("Bad request: " + command); }
}
