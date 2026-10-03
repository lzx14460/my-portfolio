package com.example.system.service;

import com.example.system.dto.SportRecordDTO;
import com.example.system.vo.SportRecordVO;
import java.util.List;

public interface SportRecordService {

    SportRecordVO addSportRecord(Long userId, SportRecordDTO dto);

    List<SportRecordVO> getSportRecords(Long userId);

    boolean deleteSportRecord(Long id, Long userId);

    void syncFromReservation(Long userId, Long facilityId, String sportDate, Integer duration);
}