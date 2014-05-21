package com.bravson.socialalert.app.repositories;

import java.util.UUID;

import com.bravson.socialalert.app.entities.SearchHistory;
import com.bravson.socialalert.app.infrastructure.CustomBaseRepository;

public interface SearchHistoryRepository extends CustomBaseRepository<SearchHistory, UUID> {

}
