package com.bravson.socialalert.app.repositories;

import java.util.UUID;

import com.bravson.socialalert.app.entities.AbuseReport;
import com.bravson.socialalert.app.infrastructure.CustomBaseRepository;

public interface AbuseReportRepository extends CustomBaseRepository<AbuseReport, UUID> {

}
