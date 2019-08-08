package com.supervision.census.service;

import com.supervision.census.model.CensusProjectCost;

import java.util.Date;
import java.util.List;

public interface CensusService {
    Object listCensusProjectCosts(int page, int pageSize);

    Object listCensusProjectCostsByDate(Date start, Date end, int page, int pageSize);
}
