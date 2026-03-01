package com.revplay.service;

import com.revplay.dto.SongCreateRequest;
import com.revplay.dto.SongResponse;
import com.revplay.dto.SongUpdateRequest;

public interface SongService {

    SongResponse createSong(SongCreateRequest request);

    SongResponse updateSong(Long songId, SongUpdateRequest request);

    void deleteSong(Long songId);

    SongResponse toggleVisibility(Long songId);
}